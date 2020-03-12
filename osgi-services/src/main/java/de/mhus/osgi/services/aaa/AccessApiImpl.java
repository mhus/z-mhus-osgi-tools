/**
 * Copyright 2018 Mike Hummel
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.services.aaa;

import java.util.Locale;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.security.AaaContext;
import de.mhus.lib.core.security.AaaUtil;
import de.mhus.lib.core.security.AccessApi;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AccountGuest;
import de.mhus.lib.core.security.AccountSource;
import de.mhus.lib.core.security.AuthorizationSource;
import de.mhus.lib.core.security.ModifyAccountApi;
import de.mhus.lib.core.security.ModifyAuthorizationApi;
import de.mhus.lib.core.security.ModifyCurrentAccountApi;
import de.mhus.lib.core.security.ModifyTrustApi;
import de.mhus.lib.core.security.TicketUtil;
import de.mhus.lib.core.security.Trust;
import de.mhus.lib.core.security.TrustSource;
import de.mhus.lib.core.util.SoftHashMap;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MException;
import de.mhus.lib.errors.MRuntimeException;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.osgi.services.aaa.util.AccountFile;

// @Component
public class AccessApiImpl extends MLog implements AccessApi {

    private static AaaContextImpl ROOT_CONTEXT = new RootContext();
    private static AaaContextImpl GUEST_CONTEXT = new GuestContext();
    protected SoftHashMap<String, Account> accountCache = new SoftHashMap<String, Account>();
    protected SoftHashMap<String, Trust> trustCache = new SoftHashMap<String, Trust>();

    private AccountSource accountSource;
    private TrustSource trustSource;
    private AuthorizationSource authorizationSource;
    private boolean fallbackToGuest;
    protected static AccessApiImpl instance;

    @Activate
    public void activate(ComponentContext ctx) {
        instance = this;
    }

    @Deactivate
    public void deactivate(ComponentContext ctx) {
        instance = null;
    }

    @Override
    public void process(AaaContext context) {
        if (context == null) return;
        ContextPool.getInstance().set((AaaContextImpl) context, true);
    }

    @Override
    public AaaContext processUserSession(String user, Locale locale) {
        log().d("user session", user);
        Account info = null;
        try {
            info = getAccount(user);
        } catch (MException e) {
            log().w(user, e);
        }
        if (info == null) throw new AccessDeniedException("null", user);
        if (!info.isValid()) throw new AccessDeniedException("invalid", user);

        return process(info, null, false, locale);
    }

    @Override
    public AaaContext process(String ticket, Locale locale) {

        if (ticket == null) throw new AccessDeniedException("null");
        boolean admin = false;
        String account = null;
        Account info = null;
        Trust trustInfo = null;

        String[] parts = ticket.split(TicketUtil.SEP);

        if (parts.length > 0 && parts[0].equals(TicketUtil.ACCOUNT)) {

            // ACCOUNT AUTH

            String pass = null;
            if (parts.length > 2) {
                account = parts[1];
                pass = parts[2];
            }
            if (parts.length > 3) admin = parts[3].equals(TicketUtil.ADMIN);

            if (account == null || pass == null)
                throw new AccessDeniedException("account or password not set");

            log().d("account", account);

            try {
                info = getAccount(account);
            } catch (MException e) {
                log().w(account, e);
            }
            if (info == null) throw new AccessDeniedException("null", account);
            if (!info.isActive()) throw new AccessDeniedException("disabled", account);
            if (!info.isValid()) throw new AccessDeniedException("invalid", account);
            if (!info.validatePassword(MPassword.decode(pass)))
                throw new AccessDeniedException("password", account);

        } else if (parts.length > 0 && parts[0].equals(TicketUtil.TRUST)) {

            // TRUST AUTH

            String trust = null;
            String secret = null;
            if (parts.length > 3) {
                trust = parts[1];
                secret = parts[2];
                account = parts[3];
            }
            if (parts.length > 4) admin = parts[4].equals(TicketUtil.ADMIN);

            trustInfo = getTrust(trust);
            if (trustInfo == null) throw new AccessDeniedException("null", account);
            if (!trustInfo.validateWithPassword(MPassword.decode(secret)))
                throw new AccessDeniedException("password", account);
            if (!trustInfo.isValid()) throw new AccessDeniedException("invalid", account);
            try {
                info = getAccountUnsecure(account);
            } catch (MException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (info == null) throw new AccessDeniedException("null", account);
            if (!info.isValid()) throw new AccessDeniedException("invalid", account);
            if (!info.isActive()) throw new AccessDeniedException("disabled", account);

        } else throw new AccessDeniedException("unknown ticket type", parts[0]);

        return process(info, trustInfo, admin, locale);
    }

    @Override
    public AaaContext process(Account info, Trust trust, boolean admin, Locale locale) {
        AaaContextImpl c = null;
        try {
            c = new AaaContextImpl(info, trust, admin, locale);
        } catch (MException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (c == null) throw new AccessDeniedException("null");
        ContextPool.getInstance().set(c, true);
        return c;
    }

    protected synchronized Trust getTrust(String trust) {

        if (trust == null) throw new AccessDeniedException("null");

        //		AaaContext c = getCurrentOrGuest();
        //		if (!c.isAdminMode())
        //			throw new AccessDeniedException("admin only");

        Trust out = trustCache.get(trust);
        if (out != null) {
            if (out.isChanged()) out = null;
        }

        if (out == null && trustSource != null) {
            out = trustSource.findTrust(trust);
            if (out != null) {
                trustCache.put(trust, out);
            } else {
                log().w("trust not found", trust);
            }
        }
        if (out != null) return out;

        throw new AccessDeniedException("trust not found", trust);
    }

    @Override
    public synchronized Account getAccount(String account) throws MException {

        if (account == null) throw new AccessDeniedException("null");

        AaaContext c = getCurrent();
        if (c != null && !c.isAdminMode() && !c.getAccountId().equals(account))
            throw new AccessDeniedException("admin only");

        return getAccountUnsecure(account);
    }

    protected synchronized Account getAccountUnsecure(String account) throws MException {

        if (account == null || account.equals("?") || account.equals("")) {
            if (fallbackToGuest) return GUEST_CONTEXT.getAccount();
            else throw new AccessDeniedException("invalid account name", account);
        }

        if (account.equals("root")) return ROOT_CONTEXT.getAccount();

        Account out = accountCache.get(account);
        if (out != null) {
            if (out instanceof AccountFile)
                if (((AccountFile) out).isChanged()) {
                    out = null;
                    accountCache.remove(account);
                }
        }
        if (out == null && accountSource != null) {
            out = accountSource.findAccount(account);
        }
        if (out != null) return out;

        if (isFallbackToGuest()) return GUEST_CONTEXT.getAccount();

        throw new NotFoundException("account not found", account);
    }

    @Override
    public AaaContext release(String ticket) {
        AaaContextImpl current = (AaaContextImpl) getCurrentOrGuest();
        if (MString.isEmpty(ticket)) return current;
        String account = null;

        String[] parts = ticket.split(TicketUtil.SEP);
        if (parts.length > 0 && parts[0].equals(TicketUtil.ACCOUNT)) {
            if (parts.length > 2) {
                account = parts[1];
            }
        } else if (parts.length > 0 && parts[0].equals(TicketUtil.TRUST)) {
            if (parts.length > 3) {
                account = parts[3];
            }
        } else throw new AccessDeniedException("unknown ticket type", parts[0]);

        log().d("release", account);

        Account info = null;
        try {
            info = getAccountUnsecure(account);
        } catch (MException e) {
            throw new MRuntimeException("can't get account to release", account, e);
        }
        return release(info);
    }

    @Override
    public AaaContext release(Account info) {
        AaaContextImpl current = (AaaContextImpl) getCurrentOrGuest();
        String account = info.getName();
        ContextPool pool = ContextPool.getInstance();
        synchronized (pool) {
            current = pool.getCurrent();
            try {
                if (MString.isEmpty(account)
                        || current == null
                        || current.getAccount() == null
                        || !account.equals(current.getAccount().getName())) return current;
            } catch (NullPointerException e) {
                e.printStackTrace();
                return current;
            }
            AaaContextImpl parent = current.getParent();
            pool.set(parent, false);
            return parent;
        }
    }

    @Override
    public AaaContext release(AaaContext context) {
        if (context == null) return null;
        ContextPool pool = ContextPool.getInstance();
        synchronized (pool) {
            AaaContextImpl parent = ((AaaContextImpl) context).getParent();
            pool.set(parent, false);
            return parent;
        }
    }

    @Override
    public void resetContext() {
        ContextPool pool = ContextPool.getInstance();
        synchronized (pool) {
            pool.set(null, false);
        }
    }

    @Override
    public AaaContext getCurrent() {
        AaaContextImpl out = ContextPool.getInstance().getCurrent();
        return out;
    }

    @Override
    public Account getCurrentAccount() throws MException {
        return getCurrentOrGuest().getAccount();
    }

    @Override
    public AaaContext processAdminSession() {

        //		String ticket = TicketUtil.ACCOUNT + TicketUtil.SEP + ROOT_CONTEXT.getAccountId() +
        // TicketUtil.SEP + TicketUtil.SEP + TicketUtil.ADMIN;
        RootContext context = new RootContext();
        ContextPool.getInstance().set(context, true);

        return context;
    }

    @Override
    public boolean validatePassword(Account account, String password) {
        if (accountSource == null) return false;
        return account.validatePassword(password);
    }

    @Override
    public String createTrustTicket(String name, AaaContext user) {
        if (trustSource == null) return null;
        Trust trust = getTrust(name);
        if (trust == null) return null;

        if (user == null) return null;

        String sec = trust.encodeWithPassword();

        return TicketUtil.TRUST
                + TicketUtil.SEP
                + name
                + TicketUtil.SEP
                + sec
                + TicketUtil.SEP
                + user.getAccountId()
                + TicketUtil.SEP
                + (user.isAdminMode() ? TicketUtil.ADMIN : "");
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
    public void addAccountSource(AccountSource source) {
        this.accountSource = source;
    }

    public void removeAccountSource(AccountSource source) {
        this.accountSource = null;
    }

    public void setAccountSource(AccountSource source) {
        this.accountSource = source;
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
    public void addTrustSource(TrustSource source) {
        this.trustSource = source;
    }

    public void removeTrustSource(TrustSource source) {
        this.trustSource = null;
    }

    public void setTrustSource(TrustSource source) {
        this.trustSource = source;
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
    public void addAuthorizationSource(AuthorizationSource source) {
        this.authorizationSource = source;
    }

    public void removeAuthorizationSource(AuthorizationSource source) {
        this.authorizationSource = null;
    }

    public void setAuthorizationSource(AuthorizationSource source) {
        this.authorizationSource = source;
    }

    @Override
    public boolean hasGroupAccess(Account account, String aclName, String action, String def) {
        if (account == null || authorizationSource == null || aclName == null) return false;

        Boolean res =
                authorizationSource.hasResourceAccess(
                        account, aclName + (action == null ? "" : "_" + action));
        if (res != null) return res;

        // action mapping
        if (action == null) return false;
        if (action.equals(Account.ACT_READ)) {
            res =
                    authorizationSource.hasResourceAccess(
                            account, aclName + "_" + Account.ACT_MODIFY);
            if (res != null) return res;
        }
        return AaaUtil.hasAccess(account, def);
    }

    @Override
    public boolean hasResourceAccess(
            Account account, String resourceName, String id, String action, String def) {
        // TODO Maybe another way ...
        return hasGroupAccess(account, "res_" + resourceName + "_" + id, action, def);
    }

    @Override
    public String getGroupAccessAcl(Account account, String aclName, String action, String def) {
        if (account == null || authorizationSource == null || aclName == null) return "";

        String acl =
                authorizationSource.getResourceAccessAcl(
                        account, aclName + (action == null ? "" : "_" + action));
        if (acl != null) return acl;

        // action mapping
        if (action == null) return "";
        if (action.equals(Account.ACT_READ)) {
            acl =
                    authorizationSource.getResourceAccessAcl(
                            account, aclName + "_" + Account.ACT_MODIFY);
            if (acl != null) return acl;
        }

        return def;
    }

    @Override
    public String getResourceAccessAcl(
            Account account, String resourceName, String id, String action, String def) {
        // TODO Maybe another way ...
        return getGroupAccessAcl(account, "res_" + resourceName + "_" + id, action, def);
    }

    @Override
    public String createUserTicket(String username, String password) {
        return TicketUtil.ACCOUNT
                + TicketUtil.SEP
                + username.replace(TicketUtil.SEP_CHAR, '_')
                + TicketUtil.SEP
                + password.replace(TicketUtil.SEP_CHAR, '_');
    }

    @Override
    public AaaContext getCurrentOrGuest() {
        AaaContextImpl current = ContextPool.getInstance().getCurrent();
        if (current == null) current = GUEST_CONTEXT;
        return current;
    }

    @Override
    public AaaContext getGuestContext() {
        return GUEST_CONTEXT;
    }

    @Override
    public AccountGuest getGuestAccount() {
        return (AccountGuest) GUEST_CONTEXT.getAccount();
    }

    @Override
    public boolean hasGroupAccess(
            Account account, Class<?> who, String acl, String action, String def) {
        return hasGroupAccess(account, who.getCanonicalName() + "_" + acl, action, def);
    }

    @Override
    public ModifyAccountApi getModifyAccountApi() {
        if (!AaaUtil.isCurrentAdmin()) throw new AccessDeniedException();
        if (accountSource == null) return null;
        return accountSource.getModifyApi();
    }

    @Override
    public ModifyAuthorizationApi getModifyAuthorizationApi() {
        if (!AaaUtil.isCurrentAdmin()) throw new AccessDeniedException();
        if (authorizationSource == null) return null;
        return authorizationSource.getModifyApi();
    }

    @Override
    public ModifyTrustApi getModifyTrustApi() {
        if (!AaaUtil.isCurrentAdmin()) throw new AccessDeniedException();
        if (trustSource == null) return null;
        return trustSource.getModifyApi();
    }

    @Override
    public ModifyCurrentAccountApi getModifyCurrentAccountApi() throws MException {
        // do not check for admin, but set to current account
        if (accountSource == null) return null;
        Account current = getCurrentAccount();
        if (current.isSynthetic() || !current.isValid()) return null; // not supported
        return new ModifyCurrentAccount(current, accountSource);
    }

    public boolean isFallbackToGuest() {
        return fallbackToGuest;
    }

    public void setFallbackToGuest(boolean fallbackToGuest) {
        this.fallbackToGuest = fallbackToGuest;
    }
}

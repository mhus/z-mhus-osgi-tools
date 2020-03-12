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

import de.mhus.lib.core.M;
import de.mhus.lib.core.security.AaaContext;
import de.mhus.lib.core.security.AccessApi;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.Trust;
import de.mhus.lib.errors.MException;

public class AaaContextImpl implements AaaContext {

    protected AaaContextImpl parent;
    private Account account;
    protected boolean adminMode = false;
    private Trust trust;
    protected Locale locale;

    public AaaContextImpl(Account account) {
        this.account = account;
    }

    public AaaContextImpl(Account account, Trust trust, boolean admin, Locale locale)
            throws MException {
        this.account = account;
        this.trust = trust;
        if (admin) {
            AccessApi aa = M.l(AccessApi.class);
            if (aa.hasGroupAccess(account, Account.MAP_ADMIN, null, null)) adminMode = true;
        }
    }

    public AaaContextImpl getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return account + (adminMode ? "(admin)" : "") + "@AaaContext";
    }

    @Override
    public Account getAccount() {
        return account;
    }

    @Override
    public boolean isAdminMode() {
        return adminMode;
    }

    public void setParent(AaaContextImpl parent) {
        this.parent = parent;
    }

    @Override
    public String getAccountId() {
        try {
            return getAccount().getName();
        } catch (NullPointerException e) {
        }
        return null;
    }

    @Override
    public <T> T getCached(String key) {
        if (key == null) return null;
        return CoreContextCacheService.get(this, key);
    }

    @Override
    public void setCached(String key, long ttl, Object item) {
        if (key == null || item == null) return;
        CoreContextCacheService.set(this, key, ttl, item);
    }

    @Override
    public Trust getTrust() {
        return trust;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void close() {
        if (account == null) return;
        AccessApi api = M.l(AccessApi.class);
        if (api == null) return;
        api.release(this);
        account = null;
    }
}

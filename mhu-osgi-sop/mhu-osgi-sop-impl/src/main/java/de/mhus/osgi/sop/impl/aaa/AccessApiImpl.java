package de.mhus.osgi.sop.impl.aaa;

import java.util.WeakHashMap;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.AccountSource;
import de.mhus.lib.core.security.AuthorizationSource;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.aaa.AccountGuest;
import de.mhus.osgi.sop.api.aaa.Trust;
import de.mhus.osgi.sop.api.aaa.TrustSource;
import de.mhus.osgi.sop.api.util.TicketUtil;
import de.mhus.osgi.sop.impl.AaaContextImpl;
import de.mhus.osgi.sop.impl.ContextPool;
import de.mhus.osgi.sop.impl.aaa.util.AccountFile;

//@Component
public class AccessApiImpl extends MLog implements AccessApi {

	private static AaaContextImpl ROOT_CONTEXT = new RootContext();
	private static AaaContextImpl GUEST_CONTEXT = new GuestContext();
	protected WeakHashMap<String, Account> accountCache = new WeakHashMap<String, Account>();
	protected WeakHashMap<String, Trust> trustCache = new WeakHashMap<String, Trust>();

	private AccountSource accountSource;
	private TrustSource trustSource;
	private AuthorizationSource authorizationSource;
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
		ContextPool.getInstance().set((AaaContextImpl) context);
	}
	
	@Override
	public AaaContext process(String ticket) {
		
		if (ticket == null)
			throw new AccessDeniedException("null");
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
			if (parts.length > 3)
				admin = parts[3].equals(TicketUtil.ADMIN);
			
			if (account == null || pass == null)
				throw new AccessDeniedException("account or password not set");

			log().d("account",account);
			
			try {
				info = getAccount(account);
			} catch (MException e) {
				log().w(account,e);
			}
			if (info == null)
				throw new AccessDeniedException("null",account);
			if (!info.validatePassword(MPassword.decode(pass)))
				throw new AccessDeniedException("password",account);
			if (!info.isValid())
				throw new AccessDeniedException("invalid",account);
			
		} else
		if (parts.length > 0 && parts[0].equals(TicketUtil.TRUST)) {
			
			// TRUST AUTH
			
			String trust = null;
			String secret = null;
			if (parts.length > 3) {
				trust = parts[1];
				secret = parts[2];
				account = parts[3];
			}
			if (parts.length > 4)
				admin = parts[4].equals(TicketUtil.ADMIN);

			trustInfo = getTrust(trust);
			if (trustInfo == null)
				throw new AccessDeniedException("null",account);
			if (!trustInfo.validatePassword(MPassword.decode(secret)))
				throw new AccessDeniedException("password",account);
			if (!trustInfo.isValid())
				throw new AccessDeniedException("invalid",account);
			try {
				info = getAccountUnsecure(account);
			} catch (MException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (info == null)
				throw new AccessDeniedException("null",account);
			if (!info.isValid())
				throw new AccessDeniedException("invalid",account);
			
		} else
			throw new AccessDeniedException("unknown ticket type",parts[0]);
				
		return process(info, trustInfo, admin);
	}
	
	@Override
	public AaaContext process(Account info, Trust trust, boolean admin) {
		AaaContextImpl c = null;
		try {
			c = new AaaContextImpl(info,trust,admin);
		} catch (MException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (c == null) 
			throw new AccessDeniedException("null");
		ContextPool.getInstance().set(c);
		return c;
	}

	public synchronized Trust getTrust(String trust) {
		
		if (trust == null)
			throw new AccessDeniedException("null");

		AaaContext c = getCurrentOrGuest();
		if (!c.isAdminMode())
			throw new AccessDeniedException("admin only");

		Trust out = trustCache.get(trust);
		if (out != null) {
			if ( out.isChanged() ) out = null;
		}

		if (out == null && trustSource != null) {
			out = trustSource.findTrust(trust);
			if (out != null) {
				trustCache.put(trust, out);
			} else {
				log().i("trust not found",trust);
			}
		}
		if (out != null)
			return out;

		throw new AccessDeniedException("trust not found",trust);

	}
	
	@Override
	public synchronized Account getAccount(String account) throws MException {
		
		if (account == null)
			throw new AccessDeniedException("null");

		AaaContext c = getCurrent();
		if (c != null && !c.isAdminMode() && !c.getAccountId().equals(account))
			throw new AccessDeniedException("admin only");
		
		return getAccountUnsecure(account);
		
	}
	
	protected synchronized Account getAccountUnsecure(String account) throws MException {
		
		if (account.equals("root"))
			return ROOT_CONTEXT.getAccount();
		
		Account out = accountCache.get(account);
		if (out != null) {
			if (out instanceof AccountFile)
				if ( ((AccountFile)out).isChanged() ) {
					out = null;
					accountCache.remove(account);
				}
		}
		if (out == null && accountSource != null) {
			out = accountSource.findAccount(account);
		}
		if (out != null)
			return out;
		
		throw new AccessDeniedException("account not found",account);
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
		} else
		if (parts.length > 0 && parts[0].equals(TicketUtil.TRUST)) {
			if (parts.length > 3) {
				account = parts[3];
			}
		} else
			throw new AccessDeniedException("unknown ticket type",parts[0]);

		log().d("release",account);
		
		Account info = null;
		try {
			info = getAccount(account);
		} catch (MException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				if (MString.isEmpty(account) || current == null || !account.equals(current.getAccount().getName()) ) return current;
			} catch (NullPointerException e) {
				e.printStackTrace();
				return current;
			}
			AaaContextImpl parent = current.getParent();
			pool.set(parent);
			return parent;
		}
	}

	@Override
	public AaaContext release(AaaContext context) {
		if (context == null) return null;
		ContextPool pool = ContextPool.getInstance();
		synchronized (pool) {
			AaaContextImpl parent = ((AaaContextImpl)context).getParent();
			pool.set(parent);
			return parent;
		}
	}
	
	@Override
	public void resetContext() {
		ContextPool pool = ContextPool.getInstance();
		synchronized (pool) {
			pool.set(null);
		}
	}
	
	@Override
	public AaaContext getCurrent() {
		AaaContextImpl out = ContextPool.getInstance().getCurrent();
		return out;
	}

	@Override
	public Account getCurrenAccount() throws MException {
		return getCurrentOrGuest().getAccount();
	}
	
	@Override
	public AaaContext processAdminSession() {
		
//		String ticket = TicketUtil.ACCOUNT + TicketUtil.SEP + ROOT_CONTEXT.getAccountId() + TicketUtil.SEP + TicketUtil.SEP + TicketUtil.ADMIN;
		ContextPool.getInstance().set(ROOT_CONTEXT);
		
		return ROOT_CONTEXT;
	}

	@Override
	public boolean validatePassword(Account account, String password) {
		if (accountSource == null) return false;
		return account.validatePassword(password);
	}

	@Override
	public String createTrustTicket(AaaContext user) {
		if (trustSource == null) return null;
		return trustSource.createTrustTicket(user);
	}

	@Reference(optional=true,dynamic=true)
	public void setAccountSource(AccountSource source) {
		this.accountSource = source;
	}
	public void unsetAccountSource(AccountSource source) {
		this.accountSource = null;
	}
	
	@Reference(optional=true,dynamic=true)
	public void setTrustSource(TrustSource source) {
		this.trustSource = source;
	}
	
	public void unsetTrustSource(TrustSource source) {
		this.trustSource = null;
	}

	@Reference(optional=true,dynamic=true)
	public void setAuthorizationSource(AuthorizationSource source) {
		this.authorizationSource = source;
	}
	
	public void unsetAuthorizationSource(AuthorizationSource source) {
		this.authorizationSource = null;
	}

	@Override
	public boolean hasGroupAccess(Account account, String acl, String action) {
		if (account == null || authorizationSource == null || acl == null ) return false;
		
		Boolean res = authorizationSource.hasResourceAccess(account,acl + (action == null ? "" : "_" + action));
		if (res != null) return res;
		
		// action mapping
		if (action == null) return false;
		if (action.equals(Account.ACT_READ)) {
			res = authorizationSource.hasResourceAccess(account,acl + "_" + Account.ACT_MODIFY);
			if (res != null) return res;
		}
		
		return false;
	}
	
	@Override
	public boolean hasResourceAccess(Account account, String resourceName, String id, String action) {
		// TODO Maybe another way ...
		return hasGroupAccess(account, "res_" + resourceName + "_" + id, action);
	}

	@Override
	public String createUserTicket(String username, String password) {
		return TicketUtil.ACCOUNT + TicketUtil.SEP + username.replace(TicketUtil.SEP_CHAR, '_') + TicketUtil.SEP + password.replace(TicketUtil.SEP_CHAR, '_') ;
	}

	@Override
	public AaaContext getCurrentOrGuest() {
		AaaContextImpl current = ContextPool.getInstance().getCurrent();
		if (current == null)
			current = GUEST_CONTEXT;
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
	public boolean hasGroupAccess(Account account, Class<?> who, String acl, String action) {
		return hasGroupAccess(account, who.getCanonicalName() + "_" + acl, action);
	}
	
}

package de.mhus.osgi.sop.api.aaa;

import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SApi;

/**
 * Basic concept of authorization:
 * 
 * - The user/subject has a set of groups and a name.
 * - The object can be authorized by an acl set
 * - There are a set of ace's for every action
 * - check if the user has access by processing the ace entries
 * 
 * @author mikehummel
 *
 */
public interface AccessApi extends SApi {

	// access
	
	AaaContext process(String ticket);
	AaaContext release(String ticket);
	AaaContext process(Account ac, Trust trust, boolean admin);
	AaaContext release(Account ac);
	AaaContext release(AaaContext context);
	void resetContext();
	
	AaaContext getCurrentOrGuest();
	
	Account getCurrenAccount() throws MException;
	
	Account getAccount(String account) throws MException;
	
	AaaContext processAdminSession();
	boolean validatePassword(Account account, String password);

	String createTrustTicket(AaaContext user);

	/**
	 * Check if a resource access is granted to the account
	 * 
	 * Check if the account has access. The list is the rule set.
	 * Rules:
	 * - '*'access to all
	 * - 'user:' prefix to allow user
	 * - 'notuser:' prefix to deny user
	 * - 'notgrout:' prefix to deny group
	 * - group name to allow group
	 * 
	 * @param account
	 * @param resourceName Name of the resource
	 * @param id The id of the object
	 * @param action The action to do or null for general access
	 * @return x
	 */
	boolean hasGroupAccess(Account account, String acl, String action);
	
	boolean hasGroupAccess(Account account, Class<?> who, String acl, String action);
	
	boolean hasResourceAccess(Account account, String resourceName, String id, String action);
	String createUserTicket(String username, String password);
	AaaContext getGuestContext();
	
	void process(AaaContext context);
	
	/**
	 * Return the current context or null if not set.
	 * 
	 * @return Context or null
	 */
	AaaContext getCurrent();
	
	AccountGuest getGuestAccount();

}

package de.mhus.osgi.sop.api.aaa;

import java.util.List;

import de.mhus.lib.core.security.Account;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.sop.api.SApi;

public interface AccessApi extends SApi {

	// access
	
	AaaContext process(String ticket);
	AaaContext release(String ticket);
	AaaContext process(Account ac, Trust trust, boolean admin);
	AaaContext release(Account ac);
	AaaContext release(AaaContext context);
	void resetContext();

	AaaContext getCurrent();
	
	Account getCurrenAccount() throws MException;
	
	Account getAccount(String account) throws MException;
	
	String processAdminSession();
	boolean validatePassword(Account account, String password);

	String createTrustTicket(AaaContext user);

	/**
	 * Check if a resource access is granted to the account
	 * 
	 * @param account
	 * @param resourceName Name of the resource
	 * @param resourceId The id of the resource or null for general access
	 * @param action The action to do or null for general access
	 * @return x
	 */
	boolean hasResourceAccess(Account account, String resourceName, String resourceId, String action);

	/**
	 * Check if the account has access. The list is the rule set.
	 * Rules:
	 * - '*'access to all
	 * - 'user:' prefix to allow user
	 * - 'notuser:' prefix to deny user
	 * - 'notgrout:' prefix to deny group
	 * - group name to allow group
	 * @param account
	 * @param mapDef
	 * @return x
	 */
	boolean hasGroupAccess(Account account, List<String> mapDef);

	/**
	 * Check if the account has access. Comma separated list of rules.
	 * 
	 * @param account
	 * @param mapDef
	 * @return x
	 */
	boolean hasGroupAccess(Account account, String mapDef);


}

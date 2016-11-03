package de.mhus.osgi.sop.api.util;

import de.mhus.lib.core.MPassword;

public class TicketUtil {

	public static final String ACCOUNT = "acc";
	public static final String TRUST = "tru";
	
	public static final String SEP = ",";
	public static final char SEP_CHAR = ',';
	public static final String ADMIN = "admin";

	/**
	 * Accepts the account information as it is and do not switch violating characters. 
	 * 
	 * @param trust
	 * @param trustSecret
	 * @return x
	 */
	public static String createRawTrustTicket(String trust, String trustSecret, String accountInfo) {
		return TRUST + SEP + trust + SEP + MPassword.encode(trustSecret) + SEP + accountInfo;
	}
	
	public static String createTrustTicket(String trust, String trustSecret, String user) {
		if (trust == null) trust = "";
		if (trustSecret == null) trustSecret = "";
		if (user == null) user = "";
		if (trust.indexOf(SEP) > -1) trust = trust.replace(',', '_');
		if (trustSecret.indexOf(SEP) > -1) trustSecret = trustSecret.replace(',', '_');
		if (user.indexOf(SEP) > -1) user = user.replace(',', '_');
		
		return TRUST + SEP + trust + SEP + MPassword.encode(trustSecret) + SEP + user;

	}
	
	public static String createTicket(String user, String pass) {
		if (user == null) user = "";
		if (pass == null) pass = "";
		if (user.indexOf(SEP) > -1) user = user.replace(',', '_');
		if (pass.indexOf(SEP) > -1) pass = pass.replace(',', '_');
		return ACCOUNT + SEP + user + SEP + MPassword.encode(pass);
	}
	
	public static String setAdmin(String ticket, boolean admin)  {
		if (ticket == null) return null;
		if (admin && !ticket.endsWith(SEP + ADMIN) && ticket.endsWith(SEP))
			ticket = ticket + ADMIN;
		else
		if (!admin && ticket.endsWith(SEP + ADMIN))
			ticket = ticket.substring(0, ticket.length() - ADMIN.length());
		return ticket;
	}
	
}

package de.mhus.osgi.sop.api.aaa;

import java.util.List;

import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.security.Account;
import de.mhus.lib.core.security.Rightful;

public class AaaUtil {
	
	private static Log log = Log.getLog(AaaUtil.class);
	
	public static boolean hasAccess(Rightful accessControl, List<String> acl) {
		if (accessControl == null || acl == null )
			return false;

		try {
			for (String line : acl) {
				if (line.startsWith("not:")) {
					line = line.substring(4);
					if (accessControl.hasGroup(line)) return false;
				} else
				if (line.startsWith("notuser:")) {
					line = line.substring(8);
					if (accessControl.getName().equals(line)) return false;
				} else
				if (line.startsWith("user:")) {
					line = line.substring(5);
					if (accessControl.getName().equals(line)) return true;
				} else
				if (line.equals("*") || accessControl.hasGroup(line)) return true;
			}
		} catch (Throwable t) {
			log.d(acl, accessControl, t);
		}
		return false;
		
	}

	public static boolean hasAccess(Rightful accessControl, String[] acl) {
		if (accessControl == null || acl == null )
			return false;

		try {
			for (String line : acl) {
				if (line.startsWith("not:")) {
					line = line.substring(4);
					if (accessControl.hasGroup(line)) return false;
				} else
				if (line.startsWith("notuser:")) {
					line = line.substring(8);
					if (accessControl.getName().equals(line)) return false;
				} else
				if (line.startsWith("user:")) {
					line = line.substring(5);
					if (accessControl.getName().equals(line)) return true;
				} else
				if (line.equals("*") || accessControl.hasGroup(line)) return true;
			}
		} catch (Throwable t) {
			log.d(acl, accessControl, t);
		}
		return false;
		
	}

	public static boolean hasAccess(Account account, String acl) {
		String[] parts = acl.split(",");
		return hasAccess(account, parts);
	}

}

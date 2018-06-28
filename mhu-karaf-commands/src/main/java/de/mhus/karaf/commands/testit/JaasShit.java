package de.mhus.karaf.commands.testit;

import java.security.Principal;
import java.util.List;
import java.util.Map.Entry;

import javax.security.auth.login.AppConfigurationEntry;

import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.config.JaasRealm;

import de.mhus.karaf.commands.utils.KarafJaasUtil;
import de.mhus.lib.core.util.EnumerationIterator;

public class JaasShit implements ShitIfc {

	@Override
	public void printUsage() {
		System.out.println(
				" user realmname username\n"
				+ " realm realmname\n"
				+ " group realmname groupname"
				);
	}

	@Override
	public Object doExecute(String cmd, String[] parameters) throws Exception {
		if (cmd.equals("realm")) {
			JaasRealm realm = KarafJaasUtil.getRealm(parameters[0]);
			System.out.println(realm);
			if (realm != null) {
				System.out.println("Name: " + realm.getName());
				System.out.println("Rank: " + realm.getRank());
				System.out.println("Class: " + realm.getClass());
				for (AppConfigurationEntry entry : realm.getEntries()) {
					System.out.println(">>> Entry: " + entry.getLoginModuleName());
					System.out.println("  Class: " + entry.getClass());
					System.out.println("  Flags: " + entry.getControlFlag());
					System.out.println("  Options:");
					for (Entry<String, ?> option : entry.getOptions().entrySet())
						System.out.println("    " + option.getKey() + " = " + option.getValue());
					System.out.println("<<<");
				}
			}
			return null;
		}
		if (cmd.equals("user")) {
			UserPrincipal user = KarafJaasUtil.getUser(parameters[0], parameters[1]);
			System.out.println(user);
			
			List<GroupPrincipal> groups = KarafJaasUtil.getGroupsForUser(parameters[0], user);
			for (GroupPrincipal group : groups) {
				System.out.println("  Group: " + group.getName());
			}
			return null;
		}
		if (cmd.equals("group")) {
			GroupPrincipal group = KarafJaasUtil.getGroup(parameters[0], parameters[1]);
			System.out.println("Group: " + group.getName());
			System.out.println("Members:");
			for (Principal member : new EnumerationIterator<Principal>(group.members())) {
				System.out.println("  " + member.getName());
			}
		}
		return null;
	}

}

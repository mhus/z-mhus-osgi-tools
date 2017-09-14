package de.mhus.osgi.sop.impl.aaa;

import java.util.Map.Entry;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import de.mhus.lib.core.MApi;
import de.mhus.lib.core.MPassword;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.lib.core.security.Account;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.adb.AdbApi;
import de.mhus.osgi.sop.api.adb.DbSchemaService;
import de.mhus.osgi.sop.impl.AaaContextImpl;

@Command(scope = "sop", name = "access", description = "Access actions")
@Service
public class AccessCmd implements Action {

	@Argument(index=0, name="cmd", required=true, description=
			"Command login <account>,"
			+ " logout, id, info,"
			+ " cache, cache.clear, local.cache.clear, local.cache.cleanup,"
			+ " synchronize <account>,"
			+ " validate <account> <password>,"
			+ " synchronizer <type>,"
			+ " access <account> <name> [<action>]"
			+ " reloadconfig,"
			+ " md5 <password>,"
			+ " admin,"
			+ " idtree", multiValued=false)
	String cmd;
	
	@Argument(index=1, name="parameters", required=false, description="More Parameters", multiValued=true)
    String[] parameters;
	
	@Option(name="-a", aliases="--admin", description="Connect user as admin",required=false)
	boolean admin = false;

	@Override
	public Object execute() throws Exception {

		AccessApi api = MApi.lookup(AccessApi.class);
		if (api == null) {
			System.out.println("SOP API not found");
			return null;
		}
		
		if (cmd.equals("validate")) {
			Account account = api.getAccount(parameters[0]);
			System.out.println("Result: " + api.validatePassword(account, parameters[1] ) );
		} else
		if (cmd.equals("login")) {
			Account ac = api.getAccount(parameters[0]);
			AaaContext cur = api.process(ac, null, admin);
			System.out.println(cur);
		} else
		if (cmd.equals("admin")) {
			RootContext context = new RootContext();
			context.setAdminMode(admin);
			api.process(context);
			System.out.println(context);
		} else
		if (cmd.equals("logout")) {
			AaaContext cur = api.getCurrentOrGuest();
			cur = api.release(cur.getAccount());
			System.out.println(cur);
		} else
		if (cmd.equals("id")) {
			AaaContext cur = api.getCurrentOrGuest();
			System.out.println(cur);
		} else
		if (cmd.equals("idtree")) {
			AaaContextImpl cur = (AaaContextImpl) api.getCurrentOrGuest();
			while (cur != null) {
				System.out.println(cur);
				cur = cur.getParent();
			}
			System.out.println(cur);
		} else
		if (cmd.equals("root")) {
			api.resetContext();
			AaaContext cur = api.processAdminSession();
			System.out.println(cur);
		} else
		if (cmd.equals("group")) {
			Account ac = api.getAccount(parameters[0]);
			return ac.hasGroup(parameters[1]);
		} else
		if (cmd.equals("access")) {
			Account ac = api.getAccount(parameters[0]);
			if (parameters.length > 2)
				return api.hasGroupAccess(ac, parameters[1], parameters[2]);
			else
				return api.hasGroupAccess(ac, parameters[1], null);
		} else
		if (cmd.equals("info")) {
			Account ac = api.getAccount(parameters[0]);
			System.out.println(ac);
		} else
		if(cmd.equals("controllers")) {
			ConsoleTable table = new ConsoleTable();
			table.setHeaderValues("Type","Controller","Bundle");
			AdbApi adb = MApi.lookup(AdbApi.class);
			for (Entry<String, DbSchemaService> entry : adb.getController()) {
				Bundle bundle = FrameworkUtil.getBundle(entry.getValue().getClass());
				table.addRowValues(entry.getKey(), entry.getValue().getClass(), bundle.getSymbolicName() );
			}
			table.print(System.out);
		} else
		if (cmd.equals("cache")) {
			AaaContextImpl context = (AaaContextImpl) api.getCurrentOrGuest();
			System.out.println("Cache Size: " + context.cacheSize());
		} else
		if (cmd.equals("local.cache.clear")) {
			AaaContextImpl context = (AaaContextImpl) api.getCurrentOrGuest();
			System.out.println("Cache Size: " + context.cacheSize());
			context.clearCache();
			System.out.println("Cache Size: " + context.cacheSize());
		} else
		if (cmd.equals("local.cache.cleanup")) {
			AaaContextImpl context = (AaaContextImpl) api.getCurrentOrGuest();
			System.out.println("Cache Size: " + context.cacheSize());
			context.cleanupCache();
			System.out.println("Cache Size: " + context.cacheSize());
		} else
		if (cmd.equals("md5")) {
			System.out.println( MPassword.encodePasswordMD5(parameters[0]) );
		} else
			System.out.println("Command not found: " + cmd);
			
		
		return null;
	}

}

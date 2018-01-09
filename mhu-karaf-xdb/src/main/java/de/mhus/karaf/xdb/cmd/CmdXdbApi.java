package de.mhus.karaf.xdb.cmd;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.karaf.xdb.adb.AdbXdbApi;

@Command(scope = "xdb", name = "api", description = "Show or select the default api")
@Service
public class CmdXdbApi implements Action {

	public static String api = AdbXdbApi.NAME;
	
	@Argument(index=0, name="api", required=false, description="Set default used api", multiValued=false)
    String name;

	@Override
	public Object execute() throws Exception {

		if (name != null) {
			XdbUtil.getApi(name);
			api = name;
		} else {
			for (String n : XdbUtil.getApis()) {
				System.out.println("Available Api: " + n);
			}
		}
		
		System.out.println("Current Xdb Api: " + api);
		return null;
	}

}

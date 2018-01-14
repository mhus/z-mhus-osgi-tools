package de.mhus.karaf.xdb.cmd;

import java.io.File;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import de.mhus.karaf.xdb.adb.AdbXdbApi;
import de.mhus.karaf.xdb.model.XdbApi;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.logging.MLogUtil;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.errors.MException;

@Command(scope = "xdb", name = "use", description = "Show or select the default api")
@Service
public class CmdUse implements Action {

	public static String api = AdbXdbApi.NAME;
	public static String service = null;
	public static String datasource = null;
	
	static {
		try {
			File f = getFile();
			if (f.exists()) {
				MUri uri = MUri.toUri(MFile.readFile(f));
				if ("xdb".equals(uri.getScheme())) {
					if (uri.getPathParts().length > 0)
						api = uri.getPathParts()[0];
					if (uri.getPathParts().length > 1)
						service = uri.getPathParts()[1];
					if (uri.getPathParts().length > 2)
						datasource = uri.getPathParts()[2];
				}
				MLogUtil.log().i("XDB loaded",uri);
			}
		} catch (Throwable t) {}
	}
	@Argument(index=0, name="cmd", required=false, description="Command: save, load, set <uri>,apis,services", multiValued=false)
    String cmd = null;

	@Argument(index=1, name="uri", required=false, description="Uri to the current used service, e.g. xdb:api/service[/datasource]", multiValued=false)
    String uriName = null;
	
	@Option(name="-a", description="New api Name",required=false)
	String apiName = null;

	@Option(name="-s", description="Service Name",required=false)
	String serviceName = null;
	
	@Option(name="-d", description="Datasource Name",required=false)
	String dsName = null;
	
	@Override
	public Object execute() throws Exception {

		if ("load".equals(cmd)) {
			File f = getFile();
			if (!f.exists()) {
				System.out.println("File not found " + f);
			} else {
				uriName = MFile.readFile(f);
				cmd = "set";
			}
		}
		
		if ("set".equals(cmd) && uriName != null) {
			MUri uri = MUri.toUri(uriName);
			if (!"xdb".equals(uri.getScheme()))
				throw new MException("scheme is not xdb");
			apiName = uri.getPathParts()[0];
			if (uri.getPathParts().length > 1)
				serviceName = uri.getPathParts()[1];
			if (uri.getPathParts().length > 2)
				dsName = uri.getPathParts()[2];
		}
				
		if (apiName != null) {
			XdbUtil.getApi(apiName);
			api = apiName;
		}
		
		if (serviceName != null) {
			XdbApi a = XdbUtil.getApi(apiName);
			a.getService(serviceName);
			service = serviceName;
		}

		if (dsName != null) {
			datasource = dsName; //check?
		}
		
		System.out.println("Current Service: xdb:" + api + "/" + service + (datasource != null ? "/" + datasource : ""));

		if ("apis".equals(cmd))
			for (String n : XdbUtil.getApis())
				System.out.println("Available Api: " + n);

		if ("services".equals(cmd)) {
			XdbApi a = XdbUtil.getApi(apiName);
			System.out.println("Services in " + api + ":");
			for (String n : a.getServiceNames())
				System.out.println("  " + n);
		}
		
		if ("save".equals(cmd)) {
			File f = getFile();
			String content = "xdb:" + MUri.encode(api) + "/" + MUri.encode(service) + "/" + MUri.encode(datasource);
			MFile.writeFile(f, content);
			System.out.println("Written!");
		}
			
		return null;
	}
	
	private static File getFile() {
		return new File("etc/" + CmdUse.class.getCanonicalName() + ".cfg");
	}


}

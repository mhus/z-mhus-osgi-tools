package de.mhus.osgi.commands.watch;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import de.mhus.lib.core.MString;
import de.mhus.lib.errors.NotFoundException;
import de.mhus.lib.karaf.MOsgi;

public class PersistentWatchConfig implements ManagedService {

	private Dictionary<String, ?> properties;
	private String pwcName;
	private BundleContext context;
	private ServiceRegistration<ManagedService> pwcReg;

	@Override
	public void updated(Dictionary<String, ?> properties)
			throws ConfigurationException {
		this.properties = properties;
	}

	public List<String> readFile() {
		LinkedList<String> out = new LinkedList<>();
		if (properties != null && properties.get("list") != null) {
			for (Enumeration<String> enu = properties.keys();enu.hasMoreElements();) {
				String key = enu.nextElement();
				if (!key.equals("service.pid"))
					for (String part : String.valueOf(properties.get(key)).split(",") ) {
						if (part.trim().length() > 0)
							out.add(part);
					}
			}
		}
		return out;
	}

	public void writeFile(List<String> content) throws IOException, NotFoundException {
		ConfigurationAdmin configurationAdmin = MOsgi.getService(ConfigurationAdmin.class);
		Configuration config = configurationAdmin.getConfiguration(pwcName, null);
		Dictionary<String,Object> props = config.getProperties();		
//		if (props == null) {
		    props = new Hashtable<>(); // every time use an empty one (ceanup)
//		}
		String list = "";
		if (content != null)
			list = MString.join(content.iterator(), ",");
	    props.put("service.pid", pwcName);
		props.put("list", list);
		config.update(props);
		properties = props;
	}

	public void register(BundleContext context) {
		this.context = context;
		Dictionary<String,String> props = new Hashtable<>();
//	    pwcName = PersistentWatch.class.getCanonicalName();
	    pwcName = context.getBundle().getSymbolicName() + ".watch";
	    props.put("service.pid", pwcName);
	    pwcReg = context.registerService(ManagedService.class, this, props);
	}

	public void unregister() {
		if (context != null)
			pwcReg.unregister();
		context = null;
		pwcReg = null;
	}

}

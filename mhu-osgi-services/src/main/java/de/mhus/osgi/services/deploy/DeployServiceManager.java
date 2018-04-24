package de.mhus.osgi.services.deploy;

import java.io.File;

import org.osgi.framework.ServiceReference;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.MLog;
import de.mhus.lib.core.console.ConsoleTable;
import de.mhus.osgi.services.DeployService;
import de.mhus.osgi.services.MOsgi;
import de.mhus.osgi.services.SimpleServiceIfc;
import de.mhus.osgi.services.MOsgi.Service;
import de.mhus.osgi.services.deploy.BundleDeployer.SENSIVITY;
import de.mhus.osgi.services.util.MServiceTracker;

@Component
public class DeployServiceManager extends MLog implements SimpleServiceIfc {

	MServiceTracker<DeployService> tracker = new MServiceTracker<DeployService>(DeployService.class) {
		
		@Override
		protected void removeService(ServiceReference<DeployService> reference, DeployService service) {
			undeploy(reference, service);
		}
		
		@Override
		protected void addService(ServiceReference<DeployService> reference, DeployService service) {
			deploy(reference, service, SENSIVITY.UPDATE);
		}
	};
	@Override
	public String getSimpleServiceInfo() {
		return "redeploy <bundle>";
	}

	protected void deploy(ServiceReference<DeployService> reference, DeployService service, SENSIVITY sensivity) {
		for (String path : service.getResourcePathes()) {
			log().i("deploy",reference.getBundle().getSymbolicName(),path,sensivity);
			try {
				File dir = BundleDeployer.deploy(reference.getBundle(), path, sensivity);
				service.setDeployDirectory(dir);
			} catch (Throwable e) {
				log().w(reference,path,e);
			}
		}
	}

	protected void undeploy(ServiceReference<DeployService> reference, DeployService service) {
		for (String path : service.getResourcePathes()) {
			log().i("undeploy",reference.getBundle().getSymbolicName(),path);
			try {
				service.setDeployDirectory(null);
				BundleDeployer.delete(reference.getBundle(), path);
			} catch (Throwable e) {
				log().w(reference,path,e);
			}
		}
		
	}

	@Override
	public String getSimpleServiceStatus() {
		return "";
	}

	@Override
	public void doSimpleServiceCommand(String cmd, Object... param) {
		try {
			if (cmd.equals("redeploy")) {
				String bundleName = String.valueOf(param[0]);
				for (Service<DeployService> ref : MOsgi.getServiceRefs(DeployService.class, null)) {
					if (ref.getReference().getBundle().getSymbolicName().equals(bundleName)) {
						System.out.println("Redeploy " + ref);
						undeploy(ref.getReference(), ref.getService());
						deploy(ref.getReference(), ref.getService(), SENSIVITY.CLEANUP);
					}
				}
			} else
			if (cmd.equals("list")) {
				ConsoleTable table = new ConsoleTable();
				table.setHeaderValues("Bundle","Name","Deploy Dir");
				for (Service<DeployService> ref : MOsgi.getServiceRefs(DeployService.class, null)) {
					table.addRowValues(ref.getReference().getBundle().getSymbolicName(),ref.getService().getClass().getCanonicalName(), ref.getService().getDeployDirectory());
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}

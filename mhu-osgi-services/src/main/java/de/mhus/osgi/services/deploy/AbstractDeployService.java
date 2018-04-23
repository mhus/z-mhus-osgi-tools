package de.mhus.osgi.services.deploy;

import java.io.File;

import de.mhus.lib.core.MLog;
import de.mhus.osgi.services.DeployService;

//@Component(provide=DeployService.class)
public abstract class AbstractDeployService extends MLog implements DeployService {

	private File dir;

	@Override
	public void setDeployDirectory(File dir) {
		this.dir = dir;
	}

	@Override
	public File getDeployDirectory() {
		return dir;
	}

}

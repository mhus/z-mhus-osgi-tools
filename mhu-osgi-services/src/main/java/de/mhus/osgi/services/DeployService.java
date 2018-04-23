package de.mhus.osgi.services;

import java.io.File;

//@Component
public interface DeployService {
	
	String[] getResourcePathes();

	void setDeployDirectory(File dir);

	File getDeployDirectory();
	
}

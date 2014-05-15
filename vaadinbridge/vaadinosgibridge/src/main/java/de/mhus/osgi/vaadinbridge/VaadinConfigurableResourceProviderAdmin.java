package de.mhus.osgi.vaadinbridge;

public interface VaadinConfigurableResourceProviderAdmin {

	public String[] getResourcePathes(String bundle);
	public String[] getResourceBundles();
	public void removeResource(String bundle);
	public void addResource(String bundle, String ... pathes);
	public void setDebug(boolean debug);


}

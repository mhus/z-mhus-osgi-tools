package de.mhus.osgi.vaadinbridge;

public interface BundleWatch {

	void refreshAll();
	void setEnabled(boolean enabled);
	boolean isEnabled();
	
}

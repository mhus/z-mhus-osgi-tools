package de.mhus.osgi.sop.api.aaa;

public interface Trust {


	public String getTrust();

	public boolean isValide();

	public boolean validatePassword(String password);

	public boolean isChanged();

}

package de.mhus.osgi.sop.api.aaa;

import java.util.List;
import java.util.UUID;

public interface Trust {


	public String getTrust();

	public boolean isValide();

	public boolean validatePassword(String password);

	public boolean isChanged();

}

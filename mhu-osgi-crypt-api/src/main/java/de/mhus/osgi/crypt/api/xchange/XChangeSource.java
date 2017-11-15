package de.mhus.osgi.crypt.api.xchange;

import de.mhus.osgi.crypt.api.error.UnknownCurrency;

public interface XChangeSource {
	
	String getDisplayName();
	String getName();
	
	TickerData getMarketData(String cur, String fiat) throws UnknownCurrency;
	
	boolean isAvailable();
	boolean isDisabled();
	boolean isActive();
	void setDisabled(boolean disabled);
	void check();
	long getFailTime();
	String getFailReason();
	
	
}

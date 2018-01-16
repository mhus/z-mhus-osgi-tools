package de.mhus.osgi.crypt.api.xchange;

import java.math.BigDecimal;
import java.util.Date;

public interface TickerData {

	BigDecimal getLast();

	BigDecimal getBid();

	BigDecimal getAsk();

	BigDecimal getHigh();

	BigDecimal getLow();

	BigDecimal getVolume();

	Date getTimestamp();

	String getCurrency();

	String getFiat();

}

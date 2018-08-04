package de.mhus.osgi.services.uptime;

public interface UptimeRecord {

	enum STATUS {CURRENT, UNKNOWN, CLOSED }

	long getStart();

	long getStop();

	STATUS getStatus();

	String getPid();

	long getSystemUptime();

	long getUptime();
	
}

package de.mhus.osgi.sop.api.rest;

import java.io.PrintWriter;

public interface RestResult {

	public void write(PrintWriter writer) throws Exception;

	public String getContentType();

}

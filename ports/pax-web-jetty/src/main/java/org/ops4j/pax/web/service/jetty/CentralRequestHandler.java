package org.ops4j.pax.web.service.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CentralRequestHandler {

	boolean doHandleBefore(CentralCallContext context)
			throws IOException, ServletException;
	
	boolean doHandleAfter(CentralCallContext context)
			throws IOException, ServletException;

	boolean isEnabled();

	double getSortHint();
	
}

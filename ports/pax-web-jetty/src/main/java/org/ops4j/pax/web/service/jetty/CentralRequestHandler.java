package org.ops4j.pax.web.service.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CentralRequestHandler {

	boolean doHandle(final String target, final HttpServletRequest baseRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException;
}

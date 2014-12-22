package de.mhus.test.httpfilter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ops4j.pax.web.service.jetty.CentralRequestHandler;

import aQute.bnd.annotation.component.Component;

@Component(immediate=true)
public class MyCentralHandler implements CentralRequestHandler {

	public boolean doHandle(String target, HttpServletRequest baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		System.out.println("C: " + baseRequest.getHeader("host") + " " + target);
		return false;
	}

}

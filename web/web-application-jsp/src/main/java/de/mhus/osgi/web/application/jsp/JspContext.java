package de.mhus.osgi.web.application.jsp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.Constants;
import org.apache.jasper.servlet.JspServlet;
import org.ops4j.pax.web.jsp.JspServletWrapper;
import org.osgi.framework.FrameworkUtil;

import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualization.api.VirtualHost;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;
import de.mhus.osgi.web.virtualization.impl.DefaultServletConfig;
import de.mhus.osgi.web.virtualization.impl.DefaultVirtualHost;

public class JspContext {

	private DefaultVirtualHost host;
	private ServletContext servletContext;
	private DefaultServletConfig config;
	private HashMap<String, JspServletWrapper> wrappers = new HashMap<>();
	private URLClassLoader cl;
	private JspServletWrapper servlet;
	
	public JspContext(VirtualHost host) throws ServletException {
		this.host = (DefaultVirtualHost) host;
		servletContext = new JspDefaultServletContext(this.host);
		config = new DefaultServletConfig(servletContext);
		cl = new URLClassLoader(new URL[0], host.getHostClassLoader());
			
		servlet = new JspServletWrapper(null,cl);
		servlet.init(config);
	}

	public boolean processRequest(CentralCallContext context, ResourceNode res) {

		JspRequestWrapper req = new JspRequestWrapper(context, res, host, config);
		
		try {
			
			req.setAttribute(Constants.JSP_FILE, context.getTarget());
			servlet.service(req, context.getResponse());
			
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}
	
}

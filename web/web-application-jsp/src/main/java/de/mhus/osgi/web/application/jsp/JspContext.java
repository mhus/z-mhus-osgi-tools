package de.mhus.osgi.web.application.jsp;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.EmbeddedServletOptions;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.apache.jasper.servlet.JspServletWrapper;

import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualization.api.VirtualHost;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;
import de.mhus.osgi.web.virtualization.impl.DefaultServletConfig;
import de.mhus.osgi.web.virtualization.impl.DefaultVirtualHost;

public class JspContext {

	private DefaultVirtualHost host;
	private ServletContext servletContext;
	private DefaultServletConfig config;
	private EmbeddedServletOptions options;
	private JspRuntimeContext rctxt;

	public JspContext(VirtualHost host) {
		this.host = (DefaultVirtualHost) host;
		servletContext = this.host.createServletContext();
		config = new DefaultServletConfig(servletContext);
		options = new EmbeddedServletOptions(config, servletContext);
		rctxt = new JspRuntimeContext(servletContext, options);

	}

	public boolean processRequest(CentralCallContext context, ResourceNode res) {
		try {
			serviceJspFile(context.getRequest(), context.getResponse(), (String)res.getProperty("filepath"), false);
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private void serviceJspFile(HttpServletRequest request,
			HttpServletResponse response, String jspUri, boolean precompile)
			throws ServletException, IOException {

		JspServletWrapper wrapper = rctxt.getWrapper(jspUri);
		if (wrapper == null) {
			synchronized (this) {
				wrapper = rctxt.getWrapper(jspUri);
				if (wrapper == null) {
					// Check if the requested JSP page exists, to avoid
					// creating unnecessary directories and files.
					wrapper = new JspServletWrapper(config, options, jspUri, false, rctxt);
					rctxt.addWrapper(jspUri, wrapper);
				}
			}
		}

		try {
			wrapper.service(request, response, precompile);
		} catch (FileNotFoundException fnfe) {
//			handleMissingResource(request, response, jspUri);
		}

	}

}

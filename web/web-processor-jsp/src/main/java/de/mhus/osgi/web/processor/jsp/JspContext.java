package de.mhus.osgi.web.processor.jsp;

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
import org.ops4j.pax.web.jsp.JasperClassLoader;
import org.ops4j.pax.web.jsp.JspServletWrapper;
import org.osgi.framework.FrameworkUtil;

import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualization.api.ProcessorContext;
import de.mhus.osgi.web.virtualization.api.VirtualHost;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;
import de.mhus.osgi.web.virtualization.api.util.ExtendedServletResponse;
import de.mhus.osgi.web.virtualization.impl.DefaultServletConfig;
import de.mhus.osgi.web.virtualization.impl.DefaultVirtualHost;

public class JspContext implements ProcessorContext {

	private DefaultVirtualHost host;
	private ServletContext servletContext;
	private DefaultServletConfig config;
	private HashMap<String, JspServletWrapper> wrappers = new HashMap<>();
	private JspServletWrapper servlet;
	
	public JspContext(VirtualHost host) throws ServletException {
		this.host = (DefaultVirtualHost) host;
		servletContext = new JspDefaultServletContext(this.host);
		config = new DefaultServletConfig(servletContext);
		servlet = new JspServletWrapper(null,new JasperClassLoader( FrameworkUtil.getBundle(getClass()), host.getHostClassLoader()));
		servlet.init(config);
	}

	public boolean processRequest(CentralCallContext context, ResourceNode res) {

		//JspRequestWrapper req = new JspRequestWrapper(context, res, host, config);
		
		HttpServletRequest req = context.getRequest();
		ExtendedServletResponse.inject(context);
		ExtendedServletResponse resp = ExtendedServletResponse.getExtendedResponse(context);
		try {
			resp.setStatus(200);
			resp.setContentType("text/plain");
			req.setAttribute(Constants.JSP_FILE, context.getTarget());
			servlet.service(req, resp);
			resp.flushBuffer();
		} catch (ServletException | IOException e) {
//		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public String getName() {
		return JspApplication.NAME;
	}
	
}

package de.mhus.osgi.web.application.php;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.caucho.quercus.servlet.QuercusServlet;

import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualization.api.VirtualHost;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;
import de.mhus.osgi.web.virtualization.api.util.ExtendedServletResponse;
import de.mhus.osgi.web.virtualization.impl.DefaultServletConfig;
import de.mhus.osgi.web.virtualization.impl.DefaultVirtualHost;

public class PhpContext {

	private DefaultVirtualHost host;
	private ServletContext servletContext;
	private DefaultServletConfig config;
	private QuercusServlet servlet;

	public PhpContext(VirtualHost host) throws ServletException {
		this.host = (DefaultVirtualHost) host;
		servletContext = new PhpDefaultServletContext(this.host);
		config = new DefaultServletConfig(servletContext);
		servlet = new QuercusServlet();
		servlet.init(config);
	}

	public boolean processRequest(CentralCallContext context, ResourceNode res) {
		
		HttpServletRequest req = context.getRequest();
		ExtendedServletResponse.inject(context);
		ExtendedServletResponse resp = ExtendedServletResponse.getExtendedResponse(context);
		try {
			resp.setStatus(200);
			resp.setContentType("text/plain");
			servlet.service(req, resp);
			resp.flushBuffer();
		} catch (ServletException | IOException e) {
			e.printStackTrace();
		}
		
		return true;
	}

}

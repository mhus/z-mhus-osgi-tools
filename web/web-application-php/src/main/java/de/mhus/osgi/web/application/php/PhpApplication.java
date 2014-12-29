package de.mhus.osgi.web.application.php;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualization.api.VirtualApplication;
import de.mhus.osgi.web.virtualization.api.VirtualHost;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;
import de.mhus.osgi.web.virtualization.impl.DefaultServletConfig;
import de.mhus.osgi.web.virtualization.impl.DefaultVirtualHost;
import de.mhus.osgi.web.virtualization.impl.FileResource;

@Component(name="PhpApplication",immediate=true,properties="name=php")
public class PhpApplication implements VirtualApplication {

	private DefaultVirtualHost host;
	private ServletContext servletContext;
	private DefaultServletConfig config;

	@Override
	public boolean processRequest(VirtualHost host, CentralCallContext context) throws Exception {

		String target = context.getTarget();
		if (!target.endsWith(".php")) return false;
		
		ResourceNode res = host.getResource(target);
		if (res == null) return false;
		
//		InputStream is = res.getInputStream();
//		if (is == null) return false;
//		is.close();
	
		if ( res.getProperty(FileResource.KEYS.TYPE.name()) != FileResource.TYPE.FILE) return false;
		
		PhpContext ctx = (PhpContext) host.getAttribute(CENTRAL_CONTEXT_KEY);
		if (ctx == null) {
			context.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return true;
		}
		
		return ctx.processRequest(context,res);
	}

	@Override
	public void configureHost(VirtualHost host, ResourceNode applicationConfig) {
		try {
			PhpContext ctx;
			ctx = new PhpContext(host);
			host.setAttribute(CENTRAL_CONTEXT_KEY, ctx);
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}

}

package de.mhus.osgi.web.application.jsp;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.EmbeddedServletOptions;
import org.apache.jasper.compiler.JspRuntimeContext;

import aQute.bnd.annotation.component.Component;
import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualization.api.VirtualApplication;
import de.mhus.osgi.web.virtualization.api.VirtualHost;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;
import de.mhus.osgi.web.virtualization.impl.DefaultServletConfig;
import de.mhus.osgi.web.virtualization.impl.DefaultVirtualHost;
import de.mhus.osgi.web.virtualization.impl.FileResource;

@Component(name="JspApplication",immediate=true,properties="name=jsp")
public class JspApplication implements VirtualApplication {

	private DefaultVirtualHost host;
	private ServletContext servletContext;
	private DefaultServletConfig config;
	private EmbeddedServletOptions options;

	@Override
	public boolean processRequest(VirtualHost host, CentralCallContext context) throws Exception {

		String target = context.getTarget();
		if (!target.endsWith(".jsp")) return false;
		
		ResourceNode res = host.getResource(target);
		if (res == null) return false;
		
//		InputStream is = res.getInputStream();
//		if (is == null) return false;
//		is.close();
	
		if ( res.getProperty(FileResource.KEYS.TYPE.name()) != FileResource.TYPE.FILE) return false;
		
		JspContext ctx = (JspContext) host.getAttribute(CENTRAL_CONTEXT_KEY);
		if (ctx == null) {
			context.getResponse().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return true;
		}
		
		return ctx.processRequest(context,res);
	}

	@Override
	public void configureHost(VirtualHost host, ResourceNode applicationConfig) {
		JspContext ctx = new JspContext(host);
		host.setAttribute(CENTRAL_CONTEXT_KEY, ctx);
	}

}

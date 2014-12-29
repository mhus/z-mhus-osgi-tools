package de.mhus.osgi.web.processor.jsp;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequestWrapper;

import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;
import de.mhus.osgi.web.virtualization.impl.DefaultServletConfig;
import de.mhus.osgi.web.virtualization.impl.DefaultVirtualHost;

public class JspRequestWrapper extends HttpServletRequestWrapper {

	private CentralCallContext context;
	private DefaultVirtualHost host;
	private ResourceNode res;
	private DefaultServletConfig config;

	public JspRequestWrapper(CentralCallContext context, ResourceNode res,
			DefaultVirtualHost host, DefaultServletConfig config) {
		super(context.getRequest());
		this.context = context;
		this.host = host;
		this.res = res;
		this.config = config;
	}

	@Override
	public String getContextPath() {
		return super.getContextPath();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String path) {
		return super.getRequestDispatcher(path);
	}

	
}

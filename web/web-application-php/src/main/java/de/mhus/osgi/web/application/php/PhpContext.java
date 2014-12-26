package de.mhus.osgi.web.application.php;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualization.api.VirtualHost;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;
import de.mhus.osgi.web.virtualization.impl.DefaultServletConfig;
import de.mhus.osgi.web.virtualization.impl.DefaultVirtualHost;

public class PhpContext {

	private DefaultVirtualHost host;
	private ServletContext servletContext;
	private DefaultServletConfig config;

	public PhpContext(VirtualHost host) {
		this.host = (DefaultVirtualHost) host;
		servletContext = this.host.createServletContext();
		config = new DefaultServletConfig(servletContext);
//		phpBin = "";
	}

	public boolean processRequest(CentralCallContext context, ResourceNode res) {
		return true;
	}

}

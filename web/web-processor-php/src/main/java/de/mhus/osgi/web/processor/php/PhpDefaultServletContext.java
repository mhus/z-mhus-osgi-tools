package de.mhus.osgi.web.processor.php;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import de.mhus.osgi.web.virtualization.impl.DefaultServletContext;
import de.mhus.osgi.web.virtualization.impl.DefaultVirtualHost;

public class PhpDefaultServletContext extends DefaultServletContext {
	
	// http://www.e-pde.gr/docs/jasper-howto.html
	public PhpDefaultServletContext(DefaultVirtualHost host) {
		super(host);
		File tmp = new File( host.getTmpRoot(), "php");
		tmp.mkdirs();
//		param.put("scratchdir", tmp.getAbsolutePath() );
//		param.put("keepgenerated", "true");
		param.put("ini-file","WEB-INF/php.ini");
		param.put("license-directory","WEB-INF/licenses");
		param.put("compile", "true");
	}
	
	
}

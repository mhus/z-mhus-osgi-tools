package de.mhus.osgi.rewriteservlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.GenericServlet;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import aQute.bnd.annotation.component.Component;

/*


 */
@Component(provide = Servlet.class, properties = "alias=/rewrite/*", name="RewriteServlet",servicefactory=true)
public class RewriteServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(RewriteServlet.class.getCanonicalName());
	private Properties props;
	
	public RewriteServlet() {
		props = new Properties();
		File f = new File ("etc/rewriteservlet.properties");
		if (f.exists()) {
			try {
				FileInputStream is = new FileInputStream(f);
				props.load(is);
				is.close();
			} catch (IOException e) {
				log.warning(e.toString());
			}
		}
	}
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		String path = req.getPathInfo();
		
		if (path == null) {
			res.sendError(404);
			return;
		}
		
		path = path.substring(1);
		String config = null;
		int pos = path.indexOf('/');
		if (pos < 0) {
			config = path;
			path = "/";
		} else {
			config = path.substring(0, pos);
			path = path.substring(pos);
		}
		
		String servlet = props.getProperty(config + ".servlet" );
		
		System.out.println(servlet + " " + path);

		if (servlet == null) {
			res.sendError(404);
			return;
		}
		
		try {
			BundleContext bc = FrameworkUtil.getBundle(RewriteServlet.class).getBundleContext();
			for (ServiceReference<Servlet> ref : bc.getServiceReferences(Servlet.class,null)) {
				Object alias = ref.getProperty("alias");
				if (alias != null && String.valueOf(alias).startsWith("/" + servlet)) {
					Servlet inst = bc.getService(ref);
					
					DispatchedHttpServletResponse newResponse = new DispatchedHttpServletResponse(res);
					inst.service(new DispatchedHttpServletRequest(path, req),newResponse);
				    String content = newResponse.getContent();
				    if (content == null) {
				    	return;
				    }
				    
				    for (int cnt = 0; props.getProperty(config + cnt + ".rule") != null; cnt++ ) {
				    	if (path.matches( props.getProperty(config + cnt + ".path") ) )
				    		content = content.replaceAll( props.getProperty(config + cnt + ".rule"), props.getProperty(config + cnt + ".replace") );
				    }
				    
				    res.getWriter().write(content);
					
					return;
				}
			}
		} catch (Throwable t ) {
			t.printStackTrace();
		}

		res.setStatus(404);
	    
	}
	
	
}

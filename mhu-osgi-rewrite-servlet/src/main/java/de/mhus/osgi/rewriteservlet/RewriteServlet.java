/**
 * Copyright 2018 Mike Hummel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.osgi.rewriteservlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
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
		
		log.fine("delegate: " + servlet + " " + path);

		if (servlet == null) {
			res.sendError(404);
			return;
		}
		
		try {
			BundleContext bc = FrameworkUtil.getBundle(RewriteServlet.class).getBundleContext();
			for (ServiceReference<Servlet> ref : bc.getServiceReferences(Servlet.class,null)) {
				Object alias = ref.getProperty("alias");
				if (alias != null && String.valueOf(alias).equals(servlet) || ref.getBundle().getSymbolicName().equals(servlet) ) {
					Servlet inst = bc.getService(ref);
					
					DispatchedHttpServletResponse newResponse = new DispatchedHttpServletResponse(res);
					DispatchedHttpServletRequest newRequest = new DispatchedHttpServletRequest(path, req);
					inst.service(newRequest,newResponse);
				    String content = newResponse.getContent();
				    
					log.fine("executed: " + servlet + " " + path + " " + ref.getBundle().getSymbolicName() + " " + (content == null ? "null" : content.length()) + " " + newResponse.getContentType() );

				    if (content == null) {
				    	return;
				    }
				    
				    for (int cnt = 0; props.getProperty(config + cnt + ".rule") != null; cnt++ ) {
				    	if (path.matches( props.getProperty(config + cnt + ".path", ".*") ) && newResponse.getContentType().matches( props.getProperty(config + cnt + ".contentType", ".*") ) )
				    		content = content.replaceAll( props.getProperty(config + cnt + ".rule"), props.getProperty(config + cnt + ".replace") );
				    }
				    
				    res.getWriter().write(content);
					
				    if (props.getProperty(config + ".debug","").equals("true")) {
				    	log.info("===================");
				    	log.info("Request: " + req.getMethod() + " " + path);
				    	for (Enumeration<String> en = req.getHeaderNames(); en.hasMoreElements();) {
				    		String name = en.nextElement();
				    		log.info("Header: " + name + "=" + req.getHeader(name));
				    	}
				    	if (newRequest.getInputBytes() != null)
				    		log.info("In: " + new String(newRequest.getInputBytes()));
				    	log.info("Out: " + content);
				    	log.info("===================");
				    }
				    
					return;
				}
			}
		} catch (Throwable t ) {
			t.printStackTrace();
		}

		res.setStatus(404);
	    
	}
	
	
}

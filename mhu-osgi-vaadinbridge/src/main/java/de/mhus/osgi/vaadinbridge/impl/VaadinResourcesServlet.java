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
package de.mhus.osgi.vaadinbridge.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.w3c.css.sac.InputSource;

import com.vaadin.sass.SassCompiler;
import com.vaadin.sass.internal.ScssStylesheet;
import com.vaadin.sass.internal.resolver.ScssStylesheetResolver;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.osgi.vaadinbridge.Resource;
import de.mhus.osgi.vaadinbridge.VaadinResourceProvider;

@Component(provide = Servlet.class, properties = { "alias=/VAADIN" }, name="VAADINResources",servicefactory=true)
public class VaadinResourcesServlet extends HttpServlet {

	static {
		new File("vaadincache").mkdirs(); // TODO configurable ...
	}
	
    public static final Object SCSS_MUTEX = new Object();
    private final Map<String, File> scssCache = new HashMap<String, File>();
	private static Logger logger = Logger.getLogger("VaadinResourcesServlet");

	private static final long serialVersionUID = 1L;
	private BundleContext context = null;
	private ServiceTracker<VaadinResourceProvider,VaadinResourceProvider> tracker;
	private ServiceTrackerCustomizer<VaadinResourceProvider, VaadinResourceProvider> customizer;

    @Activate
    public void activate(ComponentContext ctx) {
    	this.context = ctx.getUsingBundle().getBundleContext();
    	customizer = new ServiceTrackerCustomizer<VaadinResourceProvider,VaadinResourceProvider>() {

			@Override
			public VaadinResourceProvider addingService(
					ServiceReference<VaadinResourceProvider> reference) {
				return context.getService(reference);
			}

			@Override
			public void modifiedService(
					ServiceReference<VaadinResourceProvider> reference,
					VaadinResourceProvider service) {
			}

			@Override
			public void removedService(
					ServiceReference<VaadinResourceProvider> reference,
					VaadinResourceProvider service) {
			}
    		
    	};
    	tracker = new ServiceTracker<VaadinResourceProvider, VaadinResourceProvider>(context, VaadinResourceProvider.class, customizer);
    	tracker.open();
    }
    
    @Deactivate
    public void deactivate(ComponentContext ctx) {
    	tracker.close();
    	tracker = null;
    }
    

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Resource res = getResource(req.getPathInfo());
		if (res == null) return;
		
		setMimeType(res, req,resp);
		setLastModified(res, req, resp);
		
		res.writeToStream(resp.getOutputStream());
		
	}

	
	protected void setMimeType(Resource res, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType(res.getMimeType(getServletContext()));
	}
	
	protected void setLastModified(Resource res, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		long date = res.getLastModified();
		if (date <= 0) return;
		resp.setDateHeader("Last-Modified", date);
	}
	

	protected Resource getResource(String name) throws ServletException, IOException {
		
		if (ConfigurableResourceProvider.debug) System.out.println("Servlet PATH get: " + name);
		
		for (Object serviceObj : tracker.getServices()) {
			VaadinResourceProvider service = (VaadinResourceProvider)serviceObj;
			if (service.canHandle(name)) {
				Resource res = service.getResource(name);
				if (res != null && res.getUrl() != null) {
					return res;
				}
			}
		}

		if (name.endsWith(".css")) {
			String newName = name.substring(0, name.length()-3) + "scss";
			for (Object serviceObj : tracker.getServices()) {
				VaadinResourceProvider service = (VaadinResourceProvider)serviceObj;
//				if (service.canHandle(newName)) {
					Resource res = service.getResource(newName);
					if (res != null && res.getUrl() != null) {
						return handleScss(res,name, newName);
					}
//				}
			}

		}

		return null;
		
//		String path = req.getPathInfo();
//		System.out.println("PATH: " + path);
//		if (!path.startsWith("/com")) path = "/VAADIN" + path;
//		if (path.startsWith("/")) path = path.substring(1);
//		System.out.println("RES: " + path);
//		URL res = getResource(path);
//		if (res == null) {
//			if (resp != null) resp.sendError(HttpServletResponse.SC_NOT_FOUND);
//			return null;
//		}
//		return res;
	}




	@SuppressWarnings("deprecation")
	private Resource handleScss(Resource res, String scssFilename, String filename) throws IOException {
		
        synchronized (SCSS_MUTEX) {
            File cacheEntry = scssCache.get(scssFilename);

            if (cacheEntry == null || !cacheEntry.exists()) {
                try {
                	cacheEntry = compileScssOnTheFly(filename, scssFilename, res);
                	scssCache.put(scssFilename, cacheEntry);
                } catch (Exception e) {
                    logger.log(Level.WARNING,
                            "Could not read persisted scss cache", e);
                }
            }

            if (cacheEntry == null) {
                // compilation did not produce any result, but logged a message
                return null;
            }

            // This is for development mode only so instruct the browser to
            // never cache it
//            response.setHeader("Cache-Control", "no-cache");
//            final String mimetype = getService().getMimeType(filename);
//            writeResponse(response, mimetype, cacheEntry.getCss());

           return new Resource(res.getBundle(), cacheEntry.toURL() );
        }

		
	}

    private File compileScssOnTheFly(final String filename,
            final String scssFilename, Resource res) throws Exception {
    	
    	File cache = getScssCacheFile(scssFilename);
    	File css = getCssCacheFile(scssFilename);

		FileOutputStream fos = new FileOutputStream(cache);
		res.writeToStream(fos);
		fos.close();
    	
//    	SassCompiler.main(new String[] { cache.getAbsolutePath(), css.getAbsolutePath() } );

        ScssStylesheet scss = ScssStylesheet.get(cache.getAbsolutePath());
        scss.addResolver(new ScssStylesheetResolver() {
			
			@Override
			public InputSource resolve(ScssStylesheet parentStylesheet,
					String identifier) {

				try {
					int p = filename.lastIndexOf('/');
					if (p != -1) {
						String path = filename.substring(0,p);
						if (identifier.indexOf('.', Math.max( identifier.length()-5, 0)) < 0) identifier = identifier + ".scss";
						identifier = parentStylesheet.getPrefix() + identifier;
						identifier = path + "/" + identifier;
						identifier = cleanupPath(identifier);
					}
					Resource r = getResource(identifier);
					if (r == null || r.getUrl() == null) {
						// try the underscore ...
						p = identifier.lastIndexOf('/');
						if (p < 0) return null;
						identifier = identifier.substring(0, p) + "/_" + identifier.substring(p+1);
						identifier = cleanupPath(identifier);
						r = getResource(identifier);
					}
					if (r == null || r.getUrl() == null) {
						return null;
					}
					File c = getScssCacheFile(identifier);

		    		FileOutputStream fos = new FileOutputStream(c);
		    		r.writeToStream(fos);
		    		fos.close();

					return new InputSource(c.getAbsolutePath());
					
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
				
			}

		});
        scss.compile();
        SassCompiler.writeFile(css.getAbsolutePath(), scss.printState());

    	return css;
    }

    private String cleanupPath(String identifier) {
		while (true) {
			int p = identifier.indexOf("../");
			if (p < 1) return identifier;
			if (identifier.charAt(p-1) != '/') return identifier;
			int p2 = identifier.substring(0, p-1).lastIndexOf('/');
			if (p2 < 0) {
				if (identifier.startsWith("/../VAADIN/"))
					identifier = identifier.substring(p+3 + 6);
				else
					identifier = identifier.substring(p+3);
			} else
				identifier = identifier.substring(0, p2) + identifier.substring(p+2);
		}
    }
    
    private static File getScssCacheFile(String scssFile) {
    	scssFile = scssFile.replace(".", "_");
    	scssFile = scssFile.replace("~", "_");
    	scssFile = scssFile.replace("/", "_");
        return new File("vaadincache/" + scssFile + ".scss");
    }
    private static File getCssCacheFile(String scssFile) {
    	scssFile = scssFile.replace(".", "_");
    	scssFile = scssFile.replace("~", "_");
    	scssFile = scssFile.replace("/", "_");
        return new File("vaadincache/" + scssFile + ".css");
    }

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Resource res = getResource(req.getPathInfo());
		if (res == null) return;
		
		
		setMimeType(res, req, resp);
		setLastModified(res, req, resp);

	}

}
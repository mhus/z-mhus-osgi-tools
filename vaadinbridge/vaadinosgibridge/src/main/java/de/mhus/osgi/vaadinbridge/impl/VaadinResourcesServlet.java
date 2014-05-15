package de.mhus.osgi.vaadinbridge.impl;

import java.io.IOException;

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

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.osgi.vaadinbridge.Resource;
import de.mhus.osgi.vaadinbridge.VaadinResourceProvider;

@Component(provide = Servlet.class, properties = { "alias=/VAADIN" }, name="VAADINResources",servicefactory=true)
public class VaadinResourcesServlet extends HttpServlet {

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
		
		Resource res = getResource(req, resp);
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
	

	protected Resource getResource(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String name = req.getPathInfo();
		if (ConfigurableResourceProvider.debug) System.out.println("Servlet PATH get: " + name);
		
		
		
		for (Object serviceObj : tracker.getServices()) {
			VaadinResourceProvider service = (VaadinResourceProvider)serviceObj;
			if (service.canHandle(name)) {
				Resource res = service.getResource(name);
				if (res != null) {
					return res;
				}
			}
		}
/*		
		if (name.endsWith(".css")) {
			String newName = name.substring(0, name.length()-3) + "scss";
			for (Object serviceObj : tracker.getServices()) {
				VaadinResourceProvider service = (VaadinResourceProvider)serviceObj;
				if (service.canHandle(newName)) {
					URL res = service.getResource(newName);
					if (res != null) {
						return res;
					}
				}
			}

		}
*/		
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




	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Resource res = getResource(req, resp);
		if (res == null) return;
		
		
		setMimeType(res, req, resp);
		setLastModified(res, req, resp);
		
//		InputStream stream = res.openStream();
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		IOUtils.copy(stream, baos);
//		resp.setContentLength(baos.size());

	}



/*
	private synchronized Bundle getTargetBundle() {
		
        // Return target bundle immediately if it's non-null and still installed
        if (targetBundle != null && targetBundle.getState() != Bundle.UNINSTALLED) {
            return targetBundle;
        }
        targetBundle = null;

        // Get exported packages matching the specified name
        ServiceReference ref = context.getServiceReference(PackageAdmin.class.getName());
        if (ref != null) {
            PackageAdmin pkgAdmin = (PackageAdmin) context.getService(ref);
            if (pkgAdmin != null) {
                try {
                    ExportedPackage[] exportedPackages = pkgAdmin.getExportedPackages(importedPkgName);
                    // Find the one that's imported by the calling bundle
                    if (exportedPackages != null) {
                        outer:
                        for (ExportedPackage exportedPackage : exportedPackages) {
                        	System.out.println("EP: " + exportedPackage.getName());
                            Bundle[] importingBundles = exportedPackage.getImportingBundles();
                            for (Bundle bundle : importingBundles) {
                            	System.out.println("IP: " + bundle.getSymbolicName());
//                                if (bundle.getBundleId() == context.getBundle().getBundleId()) {
//                                    targetBundle = exportedPackage.getExportingBundle();
                            	targetBundle = bundle;
                                    break outer;
//                                }
                            }
                        }
                    }
                } finally {
                    context.ungetService(ref);
                }
            }
        }
        return targetBundle;
        
		// return context.getBundle();
    }
    public URL getResource(String name) {
        Bundle bundle = getTargetBundle();
        System.out.println("Bundle: " + bundle);
        return bundle != null ? bundle.getResource(name) : null;
        //return getClass().getClassLoader().getResource(name);
    }
 */

}
package de.mhus.osgi.web.virtualization.impl;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import de.mhus.lib.core.MFile;
import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.osgi.web.virtualization.api.ProcessorMatcher;
import de.mhus.osgi.web.virtualization.api.VirtualApplication;
import de.mhus.osgi.web.virtualization.api.VirtualFileProcessor;
import de.mhus.osgi.web.virtualization.api.VirtualHost;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;

@Component(name="DefaultApplication",immediate=true,properties="name=" + DefaultVirtualHost.DEFAULT_APPLICATION_ID)
public class DefaultApplication implements VirtualApplication {

	protected LinkedList<String> indexes = new LinkedList<>();
	{
		indexes.add("index.html");
	}
	protected HashMap<Integer, String> errorPages = new HashMap<>();
	protected HashMap<String, ProcessorMatcher> processorMapping = new HashMap<>();
	protected HashMap<String, VirtualFileProcessor> processors = new HashMap<>();
	protected ServiceTracker<VirtualFileProcessor, VirtualFileProcessor> processorTracker;
	
	protected boolean dynamicProcessorMapping = true;
	private BundleContext bc;

	@Activate
	public void doActivate(ComponentContext ctx) {
		bc = ctx.getBundleContext();
		processorTracker = new ServiceTracker<>(bc, VirtualFileProcessor.class, new MyCustomizer());
		processorTracker.open();
	}
	
	@Deactivate
	public void doDeactivate(ComponentContext ctx) {
		processorTracker.close();
	}
	
	@Override
	public boolean processRequest(VirtualHost host, CentralCallContext context)
			throws Exception {

		ResourceNode res = host.getResource(context.getTarget());
		if (res != null) {
			
			// lookup for processor
			for (Map.Entry<String, ProcessorMatcher> entry : processorMapping.entrySet()) {
				if (entry.getValue().matches(res)) {
					String processorName = entry.getValue().getProcessor();
					if (processorName != null) {
						VirtualFileProcessor processor = processors.get(processorName);
						if (processor != null) {
							return processor.processRequest(host, res, context);
						}
					}
					// do not deliver source code content
					context.getResponse().sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
					return true;
				}
			}
			
			// not found, deliver as static content
			deliverStaticContent(host, context, res, true);
		}
		return false;
	}

	@Override
	public void configureHost(VirtualHost host, ResourceNode config) throws Exception {

		if (config != null) {
			ResourceNode index = config.getNode("indexes");
			if (index != null) {
				indexes.clear();
				for (ResourceNode i : index.getNodes("index")) {
					indexes.add(i.getExtracted("name"));
				}
			}
			
			ResourceNode error = config.getNode("errors");
			if (error != null) {
				for (ResourceNode page : error.getNodes("page")) {
					errorPages.put(page.getInt("code", -1), page.getExtracted("name"));
				}
			}
			
		}
	}

	public boolean deliverStaticContent(VirtualHost host, CentralCallContext context, ResourceNode res, boolean setResponseOk) throws Exception {
		
		if (res == null) {
//			context.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
			return false;
		}
		
		InputStream is = res.getInputStream();
		if (is == null) {
			for (String in : indexes) {
				ResourceNode inn = res.getNode(in);
				if (inn != null) {
					is = inn.getInputStream();
					if (is != null) {
						res = inn;
						break;
					}
				}
				if (is != null) break;
			}
			if (is == null)
				return false;
		}
		
		long len = res.getLong(FileResource.KEYS.LENGTH.name(), -1);
		if (len >= 0 && len < Integer.MAX_VALUE)
			context.getResponse().setContentLength((int)len);
		context.getResponse().setContentType( host.getMimeTypeFinder().getMimeType( res ) ); //TODO find mime
		if (setResponseOk) context.getResponse().setStatus(HttpServletResponse.SC_OK);
		ServletOutputStream os = context.getResponse().getOutputStream();
		MFile.copyFile(is, os);
		os.flush();
		
		return true; // consumed
	}

	@Override
	public void processError(VirtualHost host, CentralCallContext context, int cs) {
		String errorPagePath = errorPages.get(cs);
		if (errorPagePath == null) 
			errorPagePath = errorPages.get(0);
		if (errorPagePath != null) {
			ResourceNode res = host.getResource(errorPagePath);
			try {
				deliverStaticContent(host, context, res, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private class MyCustomizer implements ServiceTrackerCustomizer<VirtualFileProcessor,VirtualFileProcessor> {

		@Override
		public VirtualFileProcessor addingService(
				ServiceReference<VirtualFileProcessor> reference) {
			
			VirtualFileProcessor service = bc.getService(reference);
			String name = (String) reference.getProperty("name");
			if (name != null) {
				processors.put(name, service);
				if (dynamicProcessorMapping) {
					processorMapping.put(name, service.getDefaultMatcher());
				}
			}
			return service;
		}

		@Override
		public void modifiedService(
				ServiceReference<VirtualFileProcessor> reference,
				VirtualFileProcessor service) {
			String name = (String) reference.getProperty("name");
			if (name != null) {
				if (dynamicProcessorMapping)
					processorMapping.put(name, service.getDefaultMatcher());
			}
		}

		@Override
		public void removedService(
				ServiceReference<VirtualFileProcessor> reference,
				VirtualFileProcessor service) {
			String name = (String) reference.getProperty("name");
			if (name != null) {
				processors.remove(name);
				if (dynamicProcessorMapping)
					processorMapping.remove(name);
			}
		}
		
	}
}

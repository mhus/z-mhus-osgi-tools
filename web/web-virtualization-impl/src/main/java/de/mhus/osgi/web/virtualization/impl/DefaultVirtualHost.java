package de.mhus.osgi.web.virtualization.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Element;

import de.mhus.lib.core.MFile;
import de.mhus.lib.core.MString;
import de.mhus.lib.core.MXml;
import de.mhus.lib.core.config.IConfig;
import de.mhus.lib.core.directory.ResourceNode;
import de.mhus.lib.core.logging.ConsoleFactory;
import de.mhus.lib.errors.MException;
import de.mhus.osgi.web.virtualization.api.central.CentralCallContext;
import de.mhus.osgi.web.virtualization.api.util.AbstractVirtualHost;

public class DefaultVirtualHost extends AbstractVirtualHost {

	private LinkedList<String> names = new LinkedList<>();
	private String applicationId;
	private ResourceNode applicationConfig;
	private File documentRoot;
	private File serverRoot;
	private File logRoot;
	private HashMap<Integer, String> errorPages = new HashMap<>();
	private ConsoleFactory logFactory;
	private String name;
	private FileRootResource documentRootRes;
	
	public DefaultVirtualHost(IConfig config) throws InstantiationException, MException, FileNotFoundException {
		
		for (ResourceNode name : config.getNodes("host")) {
			names.add( name.getExtracted("name"));
		}
		name = names.getFirst();
		
		ResourceNode app = config.getNode("application");
		if (app != null) {
			applicationId = app.getExtracted("id");
			applicationConfig = app.getNode("configuration");
		}

		ResourceNode dir = config.getNode("directories");
		String serverRootStr = dir.getExtracted("serverRoot");
		if (serverRootStr == null) throw new InstantiationException("host root not found");
		serverRoot = new File(serverRootStr);
		
		documentRoot = new File(dir.getExtracted("docuemntRoot", serverRootStr + "/html"));
		logRoot = new File(dir.getExtracted("docuemntRoot", serverRootStr + "/log"));
		
		documentRoot.mkdirs();
		logRoot.mkdirs();

		documentRootRes = new FileRootResource(documentRoot);
		
		logFactory = new ConsoleFactory(new PrintStream(new File(logRoot,"virtual.log")));
		log = logFactory.createInstance(name);
		
		ResourceNode error = config.getNode("errors");
		if (error != null) {
			for (ResourceNode page : error.getNodes("page")) {
				errorPages.put(page.getInt("code", -1), page.getExtracted("name"));
			}
		}
	}

	public static String getTagValue(Element root, String path, String def) {
		String out = def;
		Element tag = MXml.getElementByPath(root, path);
		if (tag != null) {
			out = MXml.getValue( tag, false );
			if (MString.isEmpty(out)) return def;
		}
		return out;
	}
	
	@Override
	public ResourceNode getResource(String target) {
		return documentRootRes.getResource(target);
	}

	@Override
	public void processError(CentralCallContext context) {
		
	}

	public List<String> getHostNames() {
		return names;
	}

	@Override
	public boolean processRequest(CentralCallContext context) throws Exception {
		
		ResourceNode res = getResource(context.getTarget());
		if (res == null) {
//			context.getResponse().sendError(HttpServletResponse.SC_NOT_FOUND);
			return false;
		}
		
		InputStream is = res.getInputStream();
		if (is == null) {
			// TODO find index.html or print directory
			return false;
		}
		
		context.getResponse().setContentType("text/html"); //TODO find mime
		context.getResponse().setStatus(200);
		ServletOutputStream os = context.getResponse().getOutputStream();
		MFile.copyFile(is, os);
		os.flush();
		
		return true; // consumed
	}

}

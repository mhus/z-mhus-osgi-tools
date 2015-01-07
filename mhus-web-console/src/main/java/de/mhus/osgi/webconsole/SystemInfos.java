package de.mhus.osgi.webconsole;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.webconsole.AbstractWebConsolePlugin;
import org.apache.felix.webconsole.WebConsoleConstants;
import org.apache.felix.webconsole.WebConsoleUtil;

import de.mhus.lib.core.MXml;
import aQute.bnd.annotation.component.Component;

@Component(immediate=true,provide=Servlet.class,name="SystemInfo",properties={
	"alias=/system/console/" + SystemInfos.PLUGIN})
public class SystemInfos extends AbstractWebConsolePlugin {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	final static String PLUGIN = "mhusys";
	final static String TITLE = "";

	@Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
    IOException
    {
		RequestWrapper req = new RequestWrapper(request);
		WebConsoleUtil.getVariableResolver(req);
		super.doGet(req, response);
    }
    
	@Override
	protected void renderContent(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		
		PrintWriter w = res.getWriter();
		w.println("<table class='nicetable ui-widget'>");
		w.println("<tr><td colspan='2' class='ui-widget-header'>System Parameters</td></tr>");
		
		for (Object key : System.getProperties().keySet()) {
			w.println("<tr><td>" + MXml.encode(String.valueOf(key)) + "</td><td>" + MXml.encode(System.getProperty(String.valueOf(key))) + "</td></tr>");
		}
		
		w.println("</table>");

		
		w.println("<table class='nicetable ui-widget'>");
		w.println("<tr><td colspan='2' class='ui-widget-header'>Environment</td></tr>");
		
		for (String key : System.getenv().keySet()) {
			w.println("<tr><td>" + MXml.encode(key) + "</td><td>" + MXml.encode(System.getenv().get(key)) + "</td></tr>");
		}
		
		w.println("</table>");
		
	}

	@Override
	public String getLabel() {
		return PLUGIN;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	public URL getResource(String resource) {
		if (resource.equals("/"+PLUGIN))
			return null;
 
		resource = resource.replaceAll("/"+PLUGIN+"/", "");
		return getClass().getResource(resource);
	}

}

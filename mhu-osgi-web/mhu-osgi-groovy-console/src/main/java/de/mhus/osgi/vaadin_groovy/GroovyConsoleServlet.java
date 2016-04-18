package de.mhus.osgi.vaadin_groovy;

import javax.servlet.Servlet;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;



@Component(provide = Servlet.class, properties = { "alias=/system/groovy" }, name="GroovyConsole",servicefactory=true)
@VaadinServletConfiguration(ui=GroovyConsoleUI.class, productionMode=false)
public class GroovyConsoleServlet extends VaadinServlet {

	private static final long serialVersionUID = 1L;
	private BundleContext context;
	
	@Activate
	public void activate(ComponentContext ctx) {
		this.context = ctx.getBundleContext();
	}
	
	public BundleContext getBundleContext() {
		return context;
	}

}

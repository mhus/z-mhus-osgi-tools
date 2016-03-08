package de.mhus.osgi.vaadin_groovy;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.osgi.framework.BundleContext;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

import de.mhus.lib.core.security.LoginCallbackHandler;
import de.mhus.lib.core.security.MSecurity;
import de.mhus.lib.core.util.MNls;
import de.mhus.lib.core.util.MNlsFactory;
import de.mhus.lib.vaadin.aqua.LoginPanel;

@Theme(Reindeer.THEME_NAME)
public class GroovyConsoleUI extends UI {

	private static final long serialVersionUID = 1L;
	private MNls nls;
	private Subject subject;

	@Override
	protected void init(VaadinRequest request) {
		
		nls = new MNlsFactory().create(this);
		VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.setSizeFull();
		layout.setStyleName(Reindeer.LAYOUT_BLUE);

		subject = (Subject) getSession().getAttribute("subject");
		if (subject != null) {
			if (!MSecurity.hasRole(subject, "admin"))
				subject = null;
		}
		
		if (subject == null) {
			buildLoginView(layout);
			return;
		}
		
		buildMainView(layout);
		
	}

	private void buildMainView(VerticalLayout layout) {

		ConsoleView view = new ConsoleView();
		setContent(view);
		view.setStyleName(Reindeer.LAYOUT_BLUE);
		
		view.initUi();

	}
	
	
	protected void buildLoginView(VerticalLayout layout) {
		LoginPanel panel = new LoginPanel();
		panel.setNls(nls.createSubstitute("login"));
		panel.setListener(new LoginPanel.Listener() {
			
			public boolean doLogin(String username, String password) {
				LoginContext lc;
				try {
					String realm = "karaf";
					LoginCallbackHandler handler = new LoginCallbackHandler(username,password);
					lc = new LoginContext(realm, handler);
					lc.login();
					getSession().setAttribute("subject", lc.getSubject());
					
					getUI().close(); // reset teh app
					getUI().getPage().reload();
					return true;
				} catch (LoginException e) {
					e.printStackTrace();
				}

				return false;
			}
		});
		layout.addComponent(panel);
		layout.setComponentAlignment(panel, Alignment.MIDDLE_CENTER);
	}

	public BundleContext getContext() {
        VaadinServlet servlet1 = VaadinServlet.getCurrent();
        if (servlet1 != null && servlet1 instanceof GroovyConsoleServlet) {
        	GroovyConsoleServlet servlet = (GroovyConsoleServlet)servlet1;
        	return servlet.getBundleContext();
        }
        return null;
	}

}

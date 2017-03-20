package de.mhus.osgi.rootservlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import aQute.bnd.annotation.component.Component;

/*

default.redirect=

rule0=.*
rule0.redirect=

 */
@Component(provide = Servlet.class, properties = "alias=/*", name="RootServlet",servicefactory=true)
public class RootServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Properties props;
	private static Logger log = Logger.getLogger(RootServlet.class.getCanonicalName());

	public RootServlet() {
		props = new Properties();
		File f = new File ("etc/rootservlet.properties");
		if (f.exists()) {
			log.info("Load config file " + f);
			try {
				FileInputStream is = new FileInputStream(f);
				props.load(is);
				is.close();
			} catch (IOException e) {
				log.warning(e.toString());
			}
		} else {
			log.warning("Config file not found");
		}
	}
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String path = req.getPathInfo();
		
		String action = "default";
		
		if (path == null) path = "";
		
		for (int cnt = 0; props.getProperty("rule" + cnt) != null; cnt++)
			if (path.matches( props.getProperty("rule" + cnt) ) ) {
				action = "rule" + cnt;
				break;
			}

		log.fine(path + "=" + action);
		
		String redirect = props.getProperty(action + ".redirect");
		if (redirect != null) {
			res.sendRedirect(redirect);
			return;
		}
		
		String msg = props.getProperty(action + ".errormsg");
		int erno = Integer.valueOf( props.getProperty(action + ".error", "404") );
		
		if (msg != null)
			res.setStatus(erno,msg);
		else
			res.setStatus(erno);
	}
	
	
}

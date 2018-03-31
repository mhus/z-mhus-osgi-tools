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

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;

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

    @Activate
    public void activate(ComponentContext ctx) {
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
    
    @Deactivate
    public void deactivate(ComponentContext ctx) {
    }
	
	public RootServlet() {
	}
	
	@SuppressWarnings("deprecation")
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

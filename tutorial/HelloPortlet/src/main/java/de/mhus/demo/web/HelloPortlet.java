package de.mhus.demo.web;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

public class HelloPortlet implements Portlet {

	@Override
	public void init(PortletConfig config) throws PortletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		response.getWriter().print("Portlet alive!");
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}

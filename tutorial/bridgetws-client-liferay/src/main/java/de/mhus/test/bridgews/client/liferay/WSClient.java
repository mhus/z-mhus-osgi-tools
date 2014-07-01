package de.mhus.test.bridgews.client.liferay;

import javax.portlet.PortletException;

import de.mhus.lib.liferay.portlet.LiferayMVCPortlet;

public class WSClient extends LiferayMVCPortlet {

	@Override
	protected void doInit() throws PortletException {
		resourcesHandler.register("service", new AjaxService());
		actionsHandler.register("setJwsUrl", new SetJwsUrlAction());
	}
}

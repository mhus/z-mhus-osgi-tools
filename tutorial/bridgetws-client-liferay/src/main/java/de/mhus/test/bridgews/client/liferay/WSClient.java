package de.mhus.test.bridgews.client.liferay;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;

import de.mhus.lib.liferay.MLiferayUtil;
import de.mhus.lib.liferay.portlet.LiferayMVCPortlet;
import de.mhus.lib.liferay.portlet.RenderRequestWrapper;

public class WSClient extends LiferayMVCPortlet {

	/**
	 * Return a reason why the user is not accepted. Return null if the user has rights.
	 * 
	 * @param request
	 * @return
	 */
	@Override
	public String hasRights(PortletRequest request) {
		try {
			User user = PortalUtil.getUser(request);
			if (user == null) return "login";
			if (!MLiferayUtil.hasRole(user, "Administrator"))
				return "noaccess";
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return "error";
		}
	}
		
	@Override
	protected void doInit() throws PortletException {
		resourcesHandler.register("service", new AjaxService());
		actionsHandler.register("setJwsUrl", new SetJwsUrlAction());
	}
}

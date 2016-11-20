package de.mhus.osgi.sop.api.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.mhus.osgi.sop.api.SApi;

public interface SecurityApi extends SApi {

	void checkHttpRequest(HttpServletRequest req, HttpServletResponse res);
	
}

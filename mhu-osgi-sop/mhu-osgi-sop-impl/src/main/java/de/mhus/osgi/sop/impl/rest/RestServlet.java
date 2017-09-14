package de.mhus.osgi.sop.impl.rest;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MApi;
import de.mhus.lib.core.logging.LevelMapper;
import de.mhus.lib.core.logging.Log;
import de.mhus.lib.core.logging.TrailLevelMapper;
import de.mhus.lib.core.util.Base64;
import de.mhus.lib.core.util.MNls;
import de.mhus.lib.core.util.MUri;
import de.mhus.lib.errors.AccessDeniedException;
import de.mhus.osgi.sop.api.aaa.AaaContext;
import de.mhus.osgi.sop.api.aaa.AccessApi;
import de.mhus.osgi.sop.api.aaa.Trust;
import de.mhus.osgi.sop.api.rest.CallContext;
import de.mhus.osgi.sop.api.rest.HttpRequest;
import de.mhus.osgi.sop.api.rest.Node;
import de.mhus.osgi.sop.api.rest.RestApi;
import de.mhus.osgi.sop.api.rest.RestResult;
import de.mhus.osgi.sop.api.util.SopFileLogger;
import de.mhus.osgi.sop.api.util.TicketUtil;

//@Component(immediate=true,name="RestServlet",provide=Servlet.class,properties="alias=/rest/*")
public class RestServlet extends HttpServlet {

	static Log trace = new SopFileLogger("rest", "rest_trace");

    private static final String METHOD_DELETE = "DELETE";
//    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_GET = "GET";
//    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_TRACE = "TRACE";

    private static final String RESULT_TYPE_JSON = "json";
    private static final String RESULT_TYPE_HTTP = "http";
        
	/**
	 * 
	 */
    private static final Log log = Log.getLog(RestServlet.class);
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private MNls nls = MNls.lookup(this);
	
	private int nextId = 0;

    @Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	
    	resp.setHeader("Access-Control-Allow-Origin", "*");

    	boolean isTrailEnabled = false;
    	try {
	    	String trail = req.getParameter("_trace");
	    	if (trail != null) {
	    		LevelMapper lm = MApi.get().getLogFactory().getLevelMapper();
	    		if (lm != null && lm instanceof TrailLevelMapper) {
	    			isTrailEnabled = true;
	    			((TrailLevelMapper)lm).doConfigureTrail(trail);
	    		}
	    	}
	    	
	    	String errorResultType = req.getParameter("_errorResult");
	    	if (errorResultType == null) errorResultType = RESULT_TYPE_JSON;
	    	
	    	long id = newId();
	    	
	    	
	    	String path = req.getPathInfo();
	    	
	    	if (path == null || path.length() < 1) {
	    		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    		return;
	    	}
	    	List<String> parts = new LinkedList<String>(Arrays.asList(path.split("/")));
	    	if (parts.size() == 0) return;
	    	parts.remove(0); // [empty]
	//    	parts.remove(0); // rest
	    	
	    	String ticket = req.getParameter("_ticket");
	    	if (ticket == null) {
		    	String auth = req.getHeader("Authorization");  
		        // Do we allow that user?
		    	ticket = getTicket(auth);
		        if (ticket == null) {  
		        	log.i("authorization required",id,auth,req.getRemoteAddr());
		            // Not allowed, so report he's unauthorized  
		            resp.setHeader("WWW-Authenticate", "BASIC realm=\"rest\"");  
		            sendError(errorResultType, id, resp, HttpServletResponse.SC_UNAUTHORIZED,"", null, null);
		            return;
		        }
	    	}
	        if ("true".equals(req.getParameter("_admin")))
	        	ticket = ticket + TicketUtil.SEP + "admin";
	        
	        HashMap<String, Object> context = new HashMap<>();
	        String method = req.getParameter("_method");
	        if (method == null) method = req.getMethod();
	
	        logAccess(id,req.getRemoteAddr(),req.getRemotePort(),ticket,method,req.getPathInfo(),req.getParameterMap());
	
	        @SuppressWarnings("unchecked")
			CallContext callContext = new CallContext(new HttpRequest(req.getParameterMap()), method, context);
	        
	        RestApi restService = MApi.lookup(RestApi.class);
	        
	        RestResult res = null;
	        
	        AccessApi access = MApi.lookup(AccessApi.class);
	        AaaContext user = null;
	        try {
	        	user = access.process(ticket);
	        } catch (AccessDeniedException e) {
//	        	log.d("access denied",ticket,e);
	            resp.setHeader("WWW-Authenticate", "BASIC realm=\"rest\"");  
	            sendError(errorResultType, id, resp, HttpServletResponse.SC_UNAUTHORIZED,e.getMessage(), e, null);
	            return;
	        } catch (Throwable t) {
	        	sendError(errorResultType, id, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage(), t, null );
	        	return;
	        }
	        if (user == null) { // paranoia, should throw an exception in 'process()'
	            resp.setHeader("WWW-Authenticate", "BASIC realm=\"rest\"");  
	            sendError(errorResultType, id, resp, HttpServletResponse.SC_UNAUTHORIZED,"?", null, null);
	            return;
	        }
	        
	        if (user != null) {
	        	Trust trust = user.getTrust();
	        	if (trust != null) {
	        		IProperties trustProp = trust.getProperties();
	        		if (user.isAdminMode() && !trustProp.getBoolean("allowAdmin", true)) {
	    	            sendError(errorResultType, id, resp, HttpServletResponse.SC_UNAUTHORIZED,"admin", null, null);
	    	            return;
	        		}
	        		String hostsStr = trustProp.getString("allowedHosts",null);
	        		if (hostsStr != null) {
	        			String[] hosts = hostsStr.split(",");
	        			String remote = req.getRemoteHost();
	        			boolean allowed = false;
	        			for (String pattern : hosts) {
	        				if (pattern.matches(remote)) {
	        					allowed = true;
	        					break;
	        				}
	        			}
	        			if (!allowed) {
	        	            sendError(errorResultType, id, resp, HttpServletResponse.SC_UNAUTHORIZED,"Host " + remote, null, null);
	        	            return;
	        			}
	        		}
	        	}
	        }
	        
	        try {
		        Node item = restService.lookup(parts, null, callContext);
		        
		    	if (item == null) {
		            sendError(errorResultType, id, resp, HttpServletResponse.SC_NOT_FOUND,"Resource Not Found", null, user.getAccountId());
		    		return;
		    	}
		    	
		        if (method.equals(METHOD_GET)) {
		        	res = item.doRead(callContext);
		        } else 
		        if (method.equals(METHOD_POST)) {

		        	if (callContext.hasAction())
		        		res = item.doAction(callContext);
		        	else
		        		res = item.doCreate(callContext);
		        } else
		        if (method.equals(METHOD_PUT)) {
		        	res = item.doUpdate(callContext);
		        } else 
		        if (method.equals(METHOD_DELETE)) {
		        	res = item.doDelete(callContext);
		        } else 
		        if (method.equals(METHOD_TRACE)) {
		        	
		        }
	        
	        if (res == null) {
	            sendError(errorResultType, id, resp, HttpServletResponse.SC_NOT_IMPLEMENTED, null, null, user.getAccountId() );
	            return;
	        }
	        
	        try {
		        if (res != null) {
		        	log.d("result",id,res);
		        	trace.i("result",id,res);
		        	resp.setContentType(res.getContentType());
		        	res.write(resp.getWriter());
		        }
	        } catch (Throwable t) {
	        	log.d(t);
	            sendError(errorResultType, id, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage(), t, user.getAccountId() );
	        	return;
	        }
	        
	        } catch (Throwable t) {
	        	log.d(t);
	        	sendError(errorResultType, id, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage(), t, user.getAccountId() );
	        	return;
	        } finally {
	        	access.release(ticket);
	        }
	        
    	} finally {
    		if (isTrailEnabled) {
	    		LevelMapper lm = MApi.get().getLogFactory().getLevelMapper();
	    		if (lm != null && lm instanceof TrailLevelMapper)
	    			((TrailLevelMapper)lm).doResetTrail();
    		}
    	}
    }
    
    private void logAccess(long id, String remoteAddr, int remotePort,
			String ticket, String method, String pathInfo, @SuppressWarnings("rawtypes") Map parameterMap) {

    	String paramLog = getParameterLog(parameterMap);
    	trace.i("access",id,remoteAddr,remotePort,getTicketLog(ticket), method, pathInfo, paramLog);
    	log.d("access",id,remoteAddr,remotePort,getTicketLog(ticket), method, pathInfo, paramLog );
	}

	private String getParameterLog(Map<?,?> parameterMap) {
		StringBuffer out = new StringBuffer().append('{');
		for (Map.Entry<?,?> entry : parameterMap.entrySet()) {
			out.append(entry.getKey()).append("=[");
			Object val = entry.getValue();
			if (val == null) {
			} else
			if (val.getClass().isArray()) {
				boolean first = true;
				Object[] arr = (Object[])val;
				for (Object o : arr) {
					if (first) first = false; else out.append(',');
					out.append(o);
				}
			} else {
				out.append(val);
			}
			out.append("] ");
		}
		out.append('}');
		return out.toString();
	}

	private String getTicketLog(String ticket) {
//		if (ticket == null || !ticket.startsWith("acc,") || ticket.length() < 10) return ticket;
//		int p = ticket.indexOf(',', 9);
//		if (p < 0) return ticket;
//		return ticket.substring(0,p);
		return ticket;
	}

	private synchronized long newId() {
		return nextId ++;
	}

	private void sendError(String error, long id, HttpServletResponse resp,
			int errNr, String errMsg, Throwable t, String user) throws IOException {
		
		trace.e("error",id, errNr,errMsg, t);
		log.d("error",id, errNr,errMsg, t);
		
        if (error.equals(RESULT_TYPE_HTTP)) {
        	resp.sendError(errNr);  
        	resp.getWriter().print(errMsg);
        	return;
        }

        if (error.equals(RESULT_TYPE_JSON)) {

        	if (errNr == HttpServletResponse.SC_UNAUTHORIZED)
        		resp.setStatus(errNr);
        	else
        		resp.setStatus(HttpServletResponse.SC_OK);
        	
        	PrintWriter w = resp.getWriter();
        	ObjectMapper m = new ObjectMapper();

        	ObjectNode json = m.createObjectNode();
        	json.put("_sequence", id);
        	if (user != null)
        		json.put("_user",  user);
    		LevelMapper lm = MApi.get().getLogFactory().getLevelMapper();
    		if (lm != null && lm instanceof TrailLevelMapper)
    			json.put("_trail",((TrailLevelMapper)lm).getTrailId());
        	json.put("_error", errNr);
        	json.put("_errorMessage", errMsg);
        	resp.setContentType("application/json");
    		m.writeValue(w,json);

        	return;
        }
	}

	private String getTicket(String auth) {

		if (auth == null) return null;
        if (!auth.toUpperCase().startsWith("BASIC ")) {   
            return null;  // we only do BASIC  
        }  
        // Get encoded user and password, comes after "BASIC "  
        String userpassEncoded = auth.substring(6);  
        // Decode it, using any base 64 decoder  
        String userpassDecoded = new String( Base64.decode(userpassEncoded) );
        // Check our user list to see if that user and password are "allowed"
        String[] parts = userpassDecoded.split(":",2);
        
        String account = null;
        String pass = null;
        if (parts.length > 0) account = MUri.decode(parts[0]);
        if (parts.length > 1) pass = MUri.decode(parts[1]);
        	
        return TicketUtil.createTicket(account, pass);
        
	}
	

}

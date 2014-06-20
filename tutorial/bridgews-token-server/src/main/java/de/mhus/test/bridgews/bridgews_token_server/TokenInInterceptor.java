package de.mhus.test.bridgews.bridgews_token_server;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

public class TokenInInterceptor extends AbstractSoapInterceptor {

	public static final String TOKEN = "bridgews.token";

    private static final Set<QName> HEADERS = new HashSet<QName>();
    static {
        HEADERS.add(new QName(TOKEN, "Token"));
    }

    public static ThreadLocal<String> currentToken = new ThreadLocal<>();

    public TokenInInterceptor() {
    	super(Phase.RECEIVE);
    }

	@Override
	public void handleMessage(SoapMessage message) throws Fault {
						
		String token = (String)message.getContextualProperty(TOKEN);
		System.out.println("Incomming Token " + token);
		currentToken.set(token);
		
	}

	@Override
	public void handleFault(SoapMessage message) {
	}
	
	@Override
	public Set<URI> getRoles() {
		return null;
	}

	@Override
	public Set<QName> getUnderstoodHeaders() {
		return HEADERS;
	}

}

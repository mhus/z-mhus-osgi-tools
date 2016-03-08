package de.mhus.test.bridgews.client.standalone;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

public class TokenOutInterceptor extends AbstractSoapInterceptor {

	public static final String TOKEN = "bridgews.token";

    private static final Set<QName> HEADERS = new HashSet<QName>();
    static {
        HEADERS.add(new QName(TOKEN, "Token"));
    }

	private String token;

    public TokenOutInterceptor(String token) {
    	super(Phase.WRITE);
    	this.token = token;
    }
    
	@Override
	public void handleMessage(SoapMessage message) throws Fault {
		
		//XXX not working any more .... maybe sample is not working now
		// message.setContextualProperty(TOKEN, token);
		
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

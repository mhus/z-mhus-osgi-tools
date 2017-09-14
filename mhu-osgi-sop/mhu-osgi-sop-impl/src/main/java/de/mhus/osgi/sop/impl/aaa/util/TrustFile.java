package de.mhus.osgi.sop.impl.aaa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.mhus.lib.core.IProperties;
import de.mhus.lib.core.MProperties;
import de.mhus.lib.core.MXml;
import de.mhus.osgi.sop.api.aaa.Trust;

public class TrustFile implements Trust {

	private Document doc;
	private String trust;
	private boolean valide;
	private File file;
	private long modified;
	private String name;
	
	private String password;
	private MProperties properties = new MProperties();
	
	public TrustFile(File f, String trust) throws ParserConfigurationException, SAXException, IOException {
		FileInputStream is = new FileInputStream(f);
		doc = MXml.loadXml(is);
		is.close();
		this.trust = trust;
		valide = trust != null;
		file = f;
		modified = f.lastModified();
		
		password = MXml.getElementByPath(doc.getDocumentElement(),"password").getAttribute("plain");
		name = MXml.getElementByPath(doc.getDocumentElement(),"information").getAttribute("name");
				
		Element xmlParams = MXml.getElementByPath(doc.getDocumentElement(), "properties");
		for (Element xmlParam : MXml.getLocalElementIterator(xmlParams,"property")) {
			properties.put( xmlParam.getAttribute("name"), xmlParam.getAttribute("value"));
		}
	}

	@Override
	public String getTrust() {
		return trust;
	}

	public boolean isValid() {
		return valide;
	}

	@Override
	public boolean validatePassword(String password) {
		return password.equals(this.password);
	}

	@Override
	public boolean isChanged() {
		return modified != file.lastModified();
	}

	@Override
	public String toString() {
		return trust + " " + name;
	}

	@Override
	public String getName() {
		return trust;
	}

	@Override
	public IProperties getProperties() {
		return properties;
	}

}

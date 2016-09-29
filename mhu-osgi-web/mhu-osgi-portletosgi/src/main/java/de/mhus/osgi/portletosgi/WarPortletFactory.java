package de.mhus.osgi.portletosgi;

import org.w3c.dom.Element;

import de.mhus.lib.core.MXml;

public class WarPortletFactory implements PortletFactory {

	private String name;
	private String displayName;
	private String clazzName;

	public WarPortletFactory(Element elem) {
		name = MXml.getValue(elem, "portlet-name", null);
		displayName = MXml.getValue(elem, "display-name", name);
		clazzName = MXml.getValue(elem, "portlet-class", null);
		
		
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isValid() {
		return name != null;
	}

}

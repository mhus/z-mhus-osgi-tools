package de.mhus.test.ws.ws_model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WSEntity {

	private String name;

	public WSEntity() {
		
	}
	public WSEntity(String name) {
		setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}

package de.mhus.osgi.sop.api.operation;

public class OperationBpmDefinition {

	private String name;
	private String sources;
	private String serviceClass;
	private boolean rest = false;
	
	public OperationBpmDefinition(boolean restEnabled, String name) {
		this.name = name;
		this.rest = restEnabled;
	}
	
	public String getName() {
		return name;
	}

	public String getSources() {
		return sources;
	}

	public OperationBpmDefinition setService(OperationService service) {
		this.serviceClass = service.getClass().getCanonicalName();
		return this;
	}

	public String getServiceClass() {
		return serviceClass;
	}

	public String getSourcesCondition() {
		return sources; //TODO 
	}

	public boolean isRest() {
		return rest;
	}

	public String getRegisterName() {
		return rest ? "bpm." + getName() : getName();
	}
}

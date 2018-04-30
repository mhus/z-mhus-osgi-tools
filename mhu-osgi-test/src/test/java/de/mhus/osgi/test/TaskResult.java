package de.mhus.osgi.test;

public class TaskResult {

	private String name;
	private Throwable error;
	private String output;

	public TaskResult(String name) {
		this.name = name;
	}

	public void setError(Throwable t) {
		error = t;
	}

	public void setOutput(String out) {
		output = out;
	}

	public String getName() {
		return name;
	}

	public Throwable getError() {
		return error;
	}

	public String getOutput() {
		return output;
	}

}

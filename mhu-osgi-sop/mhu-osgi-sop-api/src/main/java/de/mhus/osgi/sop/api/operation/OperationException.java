package de.mhus.osgi.sop.api.operation;

public class OperationException extends Exception {

	private static final long serialVersionUID = 1L;
	private long returnCode;

	public OperationException(long rc) {
		this(rc,"", null);
	}
	public OperationException(long rc, String msg) {
		this(rc, msg, null);
	}
	
	public OperationException(long rc, String msg, Throwable cause) {
		super(msg, cause);
		this.returnCode = rc;
	}

	public long getReturnCode() {
		return returnCode;
	}

	public String toString() {
		return returnCode + " " + super.toString();
	}
	
}

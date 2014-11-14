package de.mhus.osgi.mail.core;

public class QueueNotFoundException extends Exception {

	public QueueNotFoundException() {
		super();
	}

	public QueueNotFoundException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public QueueNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public QueueNotFoundException(String message) {
		super(message);
	}

	public QueueNotFoundException(Throwable cause) {
		super(cause);
	}

}

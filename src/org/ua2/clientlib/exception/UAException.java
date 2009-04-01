package org.ua2.clientlib.exception;

/**
 * This exception is the superclass of all exceptions generated by JEDF clientlib
 * <p>
 * It doesn't include parser exceptions (org.ua2.edf.parser.EDFTypeException) as that should
 * be handled by the library and not passed up to client applications
 * @author brian
 *
 */
public class UAException extends Exception {

	public UAException() {
	}

	public UAException(String message) {
		super(message);
	}

	public UAException(Throwable cause) {
		super(cause);
	}

	public UAException(String message, Throwable cause) {
		super(message, cause);
	}

}

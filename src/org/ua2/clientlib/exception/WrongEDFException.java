package org.ua2.clientlib.exception;

/**
 * This exception can be thrown by anything which expects a particular piece of EDF data, and gets given the wrong data
 * <p>
 * eg. a method which expects an EDF tree describing a folder is given one describing a user.
 * 
 * @author brian
 *
 */
public class WrongEDFException extends UAException {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = -4684349570461633692L;

	public WrongEDFException() {
	}

	public WrongEDFException(String message) {
		super(message);
	}

	public WrongEDFException(Throwable cause) {
		super(cause);
	}

	public WrongEDFException(String message, Throwable cause) {
		super(message, cause);
	}

}

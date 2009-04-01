package org.ua2.clientlib.exception;

/**
 * This exception is thrown if an invalid operation is attempted on a Message, such as
 * replying to a system bulletin
 * @author brian
 *
 */
public class MessageInvalidOperation extends UAException {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = -2589588415595110715L;

	public MessageInvalidOperation() {
	}

	public MessageInvalidOperation(String message) {
		super(message);
	}

	public MessageInvalidOperation(Throwable cause) {
		super(cause);
	}

	public MessageInvalidOperation(String message, Throwable cause) {
		super(message, cause);
	}

}

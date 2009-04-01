package org.ua2.clientlib.exception;

/**
 * This exception is thrown if a Message item has missing or incorrect data when it is submitted
 * @author brian
 *
 */
public class MessageInvalidData extends UAException {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = -4156128140510440937L;

	public MessageInvalidData() {
	}

	public MessageInvalidData(String message) {
		super(message);
	}

	public MessageInvalidData(Throwable cause) {
		super(cause);
	}

	public MessageInvalidData(String message, Throwable cause) {
		super(message, cause);
	}

}

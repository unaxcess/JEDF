package org.ua2.clientlib.exception;

/**
 * This exception is thrown when a method or RPC call fails
 * due to the user becoming unexpectedly disconnected from
 * the UA server. It records an enumerated reason, and a
 * human-readable text message
 * @author brian
 *
 */
public class NoConnectionError extends Exception {
	/**
	 * An enumerated list of reasons why a user is not connected to UA
	 * @author brian
	 *
	 */
	public static final long serialVersionUID = 1;
	
	public enum Reason
	{
		COULDNOTCONNECT		("Could not connect to server"),
		CONNECTIONLOST		("Connection to server lost"),
		LOGGEDOUT			("You have logged out"),
		FORCEDISCONNECT		("You have been logged out");
	
		private String reasonString = "An enumerated list of reasons why a user is not connected to UA";
	
		// This constructor is only used internally. It is used to get the right string for toString()
		Reason(String rs)
		{
			reasonString = rs;
		}
		
		/**
		 * Returns the human-readable disconnection reason string
		 * corresponding to the enumerated disconnection Reason
		 */
		public String toString()
		{
			return reasonString;
		}
	}
	
	public Reason reason;
	
	public NoConnectionError()
	{
		// This constructor doesn't need to do anything, but does need to exist
	}
	
	/**
	 * Constructs a NoConnectionError object
	 * @param reason	NoConnectionError.Reason value
	 */
	public NoConnectionError(Reason reason)
	{
		this.reason = reason;
	}
	
	/**
	 * Returns the human-readable disconnection reason string
	 * corresponding to the enumerated disconnection Reason
	 */
	public String getMessage()
	{
		return reason.toString();
	}
}
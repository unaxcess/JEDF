package org.ua2.clientlib;

import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.ua2.clientlib.exception.NoConnectionError;
import org.ua2.edf.EDFData;

/**
 * UASession manages a user's UAConnection and session, including logging in and out 
 * @author brian
 *
 */
public class UASession
{
	public enum SessionStatus { NOTCONNECTED, CONNECTFAILED, CONNECTED, LOGGEDIN, LOGGEDOUT, LOSTCONNECTION };
	
	SessionStatus sessionstatus = SessionStatus.NOTCONNECTED;
	
	private UA ua;
	
	private String clientname = "JEDF 0.2-dev";
	private String version = "2.6-beta17";

	private EDFData user = null;

	private static final Logger logger = Logger.getLogger(UASession.class);
	
	/**
	 * Create a new UASession, with the UA object set by the single static instance
	 */
	public UASession()
	{
		if(UA.singleInstance())
		{
			ua = UA.getInstance();
		}
		else
		{
			throw new Error("UASession() cannot be constructed in multiple instance mode");
		}
	}

	/**
	 * Create a new UASession, with the UA object supplied
	 * @param instance UA object for this session
	 */
	public UASession(UA instance)
	{
		ua = instance;
	}

		
	public void connect(String host, int port)
	{
		try
		{
			UAConnection connection = new UAConnection();
			
			// Register the UA connection
			ua.register(connection);
			
			connection.connect(host, port);
			
			sessionstatus = SessionStatus.CONNECTED;
			
			// FIXME - debug code
			logger.debug("Connected");
		}
		catch (Exception e)
		{
			System.err.println("Connection failed: " + e.getLocalizedMessage());
			
			sessionstatus = SessionStatus.CONNECTFAILED;
		}
	}
	
	/**
	 * Set the client name to specified value. Where not set, it defaults to "JEDF-<version>"
	 * @param name	Client name.
	 */
	public void setClientName(String name)
	{
		this.clientname = name;
	}
	
	public void setClientProtocol(String version) {
		this.version = version;
	}
	
	public EDFData sendAndRead(EDFData sendData) throws NoConnectionError {
		UAConnection connection = (UAConnection) ua.get(UAConnection.class);
		return connection.sendAndRead(sendData);
	}
	
	public int getUserId() {
		if(user == null) {
			return -1;
		}
		
		return user.getChild("userid").getInteger();
	}
	
	public boolean login(String username, String password) throws NoConnectionError {
		return login(username, password, null, false);
	}
	
	
	/**
	 * Logs in to the server
	 * 
	 * @param username	User's username
	 * @param password	User's plain-text password
	 * @param address 
	 * @return	true if users is successfully logged in, false otherwise
	 * @throws NoConnectionError	Connection to UA server has been lost or is otherwise unavailable
	 */
	public boolean login(String username, String password, InetAddress address, boolean shadow) throws NoConnectionError
	{
		// FIXME - need to handle multiple login attempts
		
		EDFData sendData = new EDFData("request", "user_login");
		sendData.add("name", username);
		sendData.add("password", password);
		sendData.add("client", clientname);
		sendData.add("protocol", version);
		if(address != null) {
			sendData.add("hostname", address.getHostAddress());
			sendData.add("address", address.getHostAddress());
		}
		if(shadow) {
			sendData.add("status", 256);
		}
		
		EDFData readData = sendAndRead(sendData);
		
		if(readData.sValue.equals("user_login"))
		{
			// Login successful
			user  = readData;
			
			sessionstatus = SessionStatus.LOGGEDIN;
			
			return true;
		}
		else
		{
			// Login failed
			return false;
		}
	}
	
	/**
	 * Logs out of the server. This method is assumed to succeed, and returns nothing.
	 * 
	 * @throws NoConnectionError	Connection to UA server has been lost or is otherwise unavailable
	 */
	public void logout() throws NoConnectionError
	{
		sendAndRead(new EDFData("request", "user_logout"));
	}

	
	/**
	 * Gets the login banner
	 * 
	 * @return	String containing formatted plain-text login banner
	 * @throws NoConnectionError	Connection to UA server has been lost or is otherwise unavailable
	 */
	public String loginBanner() throws NoConnectionError
	{
		// FIXME - this is not the best place for this method

		logger.debug("Request for login banner");

		String banner = "No banner";
		
		try
		{
			EDFData bannerresponse = sendAndRead(new EDFData("request", "system_list"));

			banner = bannerresponse.getChild("banner").sValue;
		}
		catch(Exception e)
		{
			logger.error("Cannot get banner", e);
		}
		
		logger.debug("Returning banner");
		
		return banner;
	}
}

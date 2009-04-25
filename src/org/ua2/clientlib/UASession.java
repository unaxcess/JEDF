package org.ua2.clientlib;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.ua2.edf.*;
import org.ua2.clientlib.*;
import org.ua2.clientlib.exception.*;

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
	
	private String clientname = "JEDF 0.1-dev";
	
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
			System.out.println("Connected");
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
		clientname = name;
	}
	
	/**
	 * Logs in to the server
	 * 
	 * @param username	User's username
	 * @param password	User's plain-text password
	 * @return	true if users is successfully logged in, false otherwise
	 * @throws NoConnectionError	Connection to UA server has been lost or is otherwise unavailable
	 */
	public boolean login(String username, String password) throws NoConnectionError
	{
		// FIXME - need to handle multiple login attempts
		
		EDFData sendData;
		EDFData readData;
		
		UAConnection connection = (UAConnection) ua.get(UAConnection.class);
					
		sendData = new EDFData("request", "user_login");
		sendData.add("name", username);
		sendData.add("password", password);
		sendData.add("client", clientname);
		sendData.add("protocol", "2.6-beta17");

		readData = connection.sendAndRead(sendData);
		
		if(readData.sValue.equals("user_login"))
		{
			// Login successful
			
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
		// FIXME - handle logouts better?
		EDFData readData;

		UAConnection connection = (UAConnection) ua.get(UAConnection.class);
		readData = connection.sendAndRead(new EDFData("request", "user_logout"));
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

		System.out.println("Request for login banner");

		UAConnection connection = (UAConnection) ua.get(UAConnection.class);
		
		String banner = "No banner";
		
		try
		{
			EDFData bannerresponse = connection.sendAndRead(new EDFData("request", "system_list"));
			System.out.println("Status: " + connection.statusmessage);

			banner = bannerresponse.getChild("banner").sValue;
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
		}
		
		System.out.println("Returning banner");
		
		return banner;
	}
	
}

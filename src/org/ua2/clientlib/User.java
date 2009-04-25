package org.ua2.clientlib;

import org.ua2.clientlib.exception.NoConnectionError;
import org.ua2.clientlib.exception.WrongEDFException;
import org.ua2.edf.EDFData;

/**
 * This class represents a UA user
 * @author brian
 *
 */
public class User
{
	public static String defaultAccessName[] = { "None", "Guest", "Messages", "Editor", "Witness", "Sysop" };
	
	public int id = -1;				// User ID
	public String name;				// User name
	public int accesslevel = -1;	// Access level
	public String accessname;		// Access name (null for the accesslevel default)
	// TODO - other data
	
	private UA ua;
		

	/**
	 * Construct a blank User object
	 */
	public User()
	{
		if(UA.singleInstance())
		{
			ua = UA.getInstance();
		}
	}

	/**
	 * Construct a user object from an EDF tree
	 * @param edf EDFData tree representing a user
	 * @throws WrongEDFException the EDFData tree didn't contain the correct information to describe a user
	 */
	public User(EDFData edf) throws WrongEDFException
	{
		this();
		
		populateUser(edf);
	}
	
	/**
	 * Creates a User object for the user specified, or a blank object if the user doesn't exist
	 * @param name	Name of the user
	 * @throws NoConnectionError	Not connected to UA
	 * @throws WrongEDFException	Got some sort of error from the server
	 */
	public User(String name) throws NoConnectionError, WrongEDFException
	{
		this();
		
		EDFData user = getUser(name);
		
		if(user != null)
		{
			populateUser(user);
		}
	}
	
	/**
	 * Creates a User object for the user specified, or a blank object if the user doesn't exist
	 * @param name	Name of the user
	 * @param instance	UA object for this UA session
	 * @throws NoConnectionError	Not connected to UA
	 * @throws WrongEDFException	Got some sort of error from the server
	 */
	public User(String name, UA instance) throws NoConnectionError, WrongEDFException
	{
		this();
		
		setUAInstance(instance);
		
		EDFData user = getUser(name);
		
		if(user != null)
		{
			populateUser(user);
		}
	}

	private EDFData getUser(String name) throws NoConnectionError
	{
		EDFData request = new EDFData("request", "user_list");
		request.add("name", name);
		
		UAConnection uacon = (UAConnection) ua.get(UAConnection.class);
		
		EDFData reply = uacon.sendAndRead(request);
		
		// TODO - handle fail replies
		
		EDFData user = reply.getChild("user");
		
		return user;	// Return null if no user
	}
	
	
	private void populateUser(EDFData edf) throws WrongEDFException
	{
		// Check the root element
		if((! edf.name.equalsIgnoreCase("user")) || (edf.type != EDFData.ValueType.INTEGER))
		{
			throw new WrongEDFException("Expected '<user=[number]></>");
		}
		
		id = edf.iValue;

		for(EDFData child : edf.children)
		{
			if(child.name.equals("name"))
			{
				if((child.type != EDFData.ValueType.STRING) || (child.sValue.isEmpty()))
				{
					throw new WrongEDFException("Expected <name=[string]/>");
				}
				
				name = child.sValue;
				continue;
			}
			
			if(child.name.equals("accesslevel"))
			{
				if(child.type != EDFData.ValueType.INTEGER)
				{
					throw new WrongEDFException("Expected <accesslevel=[number]/>");
				}
				
				accesslevel = child.iValue;
				continue;
			}

			// TODO - the other elements of a user
			// TODO - how to handle the fact that sometimes a <user> tree might be minimal (eg from <request="user_list"/>)
			// and other times it might contain full information about a user
		}
		
		if((name == null) || (accesslevel == -1))
		{
			throw new WrongEDFException("<user> is missing a required element");
		}
	}
	
	/**
	 * Set this User's UA instance, if not set
	 * @param instance	an instance of UA
	 */
	public void setUAInstance(UA instance)
	{
		if(ua == null)
		{
			ua = instance;
		}
		else
		{
			throw new Error("Can't set an object's UA instance more than once");
		}
	}	
}

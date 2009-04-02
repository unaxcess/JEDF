package org.ua2.clientlib;

import org.ua2.clientlib.exception.*;
import org.ua2.edf.EDFData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class to handle user - it the list of users on UA
 * @author brian
 *
 */
public class UserList
{
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<String, User>();
	private UA ua;
	
	/**
	 * Creates a UserList with its UA instance set to the single static instance, if there is one, or not set
	 */
	public UserList()
	{
		if(UA.singleInstance())
		{
			ua = UA.getInstance();
		}
	}
	/**
	 * Creates a new UserList
	 * @param ua	an instance of UA
	 */
	public UserList(UA instance)
	{
		setUAInstance(instance);
	}
	
	/**
	 * Set this UserList's UA instance, if not set
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
	/**
	 * Refresh the list of users
	 */
	public void refresh() throws UAException
	{
		ConcurrentHashMap<String, User> newusers = new ConcurrentHashMap<String, User>();
		
		UAConnection connection = (UAConnection) ua.get(UAConnection.class);
		
		EDFData request = new EDFData("request", "user_list");
		EDFData reply = connection.sendAndRead(request);

		// TODO - handle errors (eg, <reply="rq_invalid">)
		for(EDFData userdata : reply.getChildren("user"))
		{
			User user = new User(userdata);
			
			newusers.put(user.name, user);
		}
		
		users = newusers;
	}
	
	public List<User> getUserList()
	{
		ArrayList<User> userlist = new ArrayList<User>(users.values());

		// Ewww, ewww, ewww, ewww, ewww
		// FIXME - this shit is just for testing
		Collections.sort(userlist, new Comparator<User>() {
			public int compare(org.ua2.clientlib.User arg0,
					org.ua2.clientlib.User arg1) {

				return arg0.name.compareToIgnoreCase(arg1.name);
			}
		});
		
		return userlist;
	}
	
	/**
	 * Gets the named user, if they exist, or null
	 * @param name	user name
	 * @return User, or null
	 */
	public User getUser(String name)
	{
		return users.get(name);
	}
}

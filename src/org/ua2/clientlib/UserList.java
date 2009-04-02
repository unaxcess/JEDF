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
	private UAConnection ua;
	
	/**
	 * Creates a new UserList
	 * @param uaconnection	a connection to UA
	 */
	public UserList(UAConnection uaconnection)
	{
		ua = uaconnection;
	}
	
	/**
	 * Refresh the list of users
	 */
	public void refresh() throws UAException
	{
		ConcurrentHashMap<String, User> newusers = new ConcurrentHashMap<String, User>();
		
		EDFData request = new EDFData("request", "user_list");
		EDFData reply = ua.sendAndRead(request);

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

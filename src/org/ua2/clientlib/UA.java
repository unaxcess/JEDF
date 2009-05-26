package org.ua2.clientlib;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all of the various objects a client needs which might talk to each other
 * @author brian
 *
 */
public class UA
{
	private static UA singleinstance;
	
	@SuppressWarnings("unchecked")
	private ConcurrentHashMap<Class, Object> objectlist = new ConcurrentHashMap<Class, Object>();

	/**
	 * Call this if your application only needs a single connection to UA for a single user
	 */
	public void setSingleInstance()
	{
		if(singleinstance == null)
		{
			singleinstance = this;
		}
		else
		{
			throw new Error("Cannot call setSingleInstance() more than once");
		}
	}
	
	/**
	 * Is this application supporting more than one UA connection/user?
	 * @return true if this application supports a single connection/user
	 */
	public static boolean singleInstance()
	{
		return (singleinstance != null);
	}

	/**
	 * Return the single instance, if singleInstance(), or null
	 * @return Single instance of UA object, or null
	 */
	public static UA getInstance()
	{
		return singleinstance;
	}
	
	/**
	 * Register a new object that other classes can find by class name
	 * @param object
	 */
	public void register(Object object)
	{
		objectlist.put(object.getClass(), object);
	}
	
	/**
	 * Unregister a previously registered class
	 * @param class
	 */
	@SuppressWarnings("unchecked")
	public void unregister(Class objClass)
	{
		objectlist.remove(objClass);
	}
	
	/**
	 * Retrieve a registered object by its class
	 * @param class
	 * @return the object, or null if the class isn't registered
	 */
	@SuppressWarnings("unchecked")
	public Object get(Class objClass)
	{
		return objectlist.get(objClass);
	}
}

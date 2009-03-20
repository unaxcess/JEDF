package org.ua2.clientlib;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.ua2.edf.EDFData;

public class AnnounceQueue implements Runnable
{
	private ConcurrentHashMap<String, EDFAnnouncement> subscribers = new ConcurrentHashMap<String, EDFAnnouncement>();

	// ConcurrentLinkedQueue is a thread-safe FIFO
	private ConcurrentLinkedQueue<EDFData> announcementqueue = new ConcurrentLinkedQueue<EDFData>();

	public void subscribe(String message, EDFAnnouncement handler)
	{
		subscribers.put(message, handler);
	}
	
	public void announce(EDFData announcement)
	{
		announcementqueue.add(announcement);
		
		Thread queuerun = new Thread(this);
		queuerun.start();
	}
	
	public void run()
	{
		// Pull one announcement off the announcement list and process it
		EDFData message = announcementqueue.poll();
		
		if(message == null)
		{
			// FIXME - error handling
			System.err.println("Announcement queue thread found empty queue");
			
			return;
		}
		
		if(message.type != EDFData.ValueType.STRING)
		{
			// FIXME - error handling
			System.err.println("Announcement queue thread found bogus non-string announcement");
			
			return;
		}

		EDFAnnouncement handler = subscribers.get(message.sValue);

		// Call the handler's announce method inside this thread
		if(handler != null)
		{
			handler.announce(message);
		}
	}
}

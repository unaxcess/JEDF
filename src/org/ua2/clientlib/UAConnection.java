package org.ua2.clientlib;

import java.io.InputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.ua2.edf.*;
import org.ua2.edf.parser.*;

import org.ua2.clientlib.exception.*;

/**
 * A UAConnection manages an individual user's connection to the UA server
 * <p>
 * It deals with sendign and recieving EDF, but not session management 
 * @author brian
 *
 */
public class UAConnection implements Runnable {

	private Socket connection;
	private InputStream input;
	private DataOutputStream output;
	private EDFParser inputparser;

	public enum ConnectionStatus { NOTCONNECTED, CONNECTFAILED, CONNECTED, LOSTCONNECTION };

	public ConnectionStatus status = ConnectionStatus.NOTCONNECTED;
	public String statusmessage = "Not connected";
	
	// SynchronousQueue is a "queue" which only allows one item on it
	// It blocks on write until something comes along and reads it, and
	// blocks on read until something writes to it.
	private SynchronousQueue<EDFData> reply = new SynchronousQueue<EDFData>();

	private AnnounceQueue announcequeue = new AnnounceQueue();
	
	/**
	 * This code is run in its own thread, started when the UA server
	 * connection is established
	 * <p>
	 * It loops forever (or until there's an error), calling readEDF (which blocks
	 * until an EDF tree is available from the parser), and, depending on the EDF
	 * message, either makes it available as a reply (presumed to be in response to
	 * a request) or pushes it onto the object's announcement FIFO.
	 */
	public void run()
	{
		EDFData inputedf;
		
		// Loop round parsing the input
		while(true)
		{
			inputedf = readEDF();		// FIXME - no null handling

			/* EDF from the server can be one of three things
			 * 	edf - response to connection message <edf="on"/>. Not used here
			 * 	reply - reply to a request
			 * 	announce - unsolicited announcement
			 */

			if(inputedf.name.equals("announce"))
			{
				// Pass to the announcement handler
				announcequeue.announce(inputedf);
				inputedf.print();
			}
			else if(inputedf.name.equals("reply"))
			{
				try
				{
					inputedf.print();

					// FIXME - needs a timeout.
					reply.put(inputedf);
				}
				catch(InterruptedException e)
				{
					// Something went wrong
					setStatus(ConnectionStatus.NOTCONNECTED, "Internal error in server: " + e.getLocalizedMessage());
					closeConnection();
					
					System.err.println("Parser thread interrupted on put(): " + e.getLocalizedMessage());
					e.printStackTrace();
					
					// 
					// Exit this thread (Thread.currentThread().stop() is deprecated)
					return;
				}
			}
			else
			{
				// FIXME - what should we do with unknown EDF messages?
				System.out.println("Unknown EDF response");
				inputedf.print();
			}
		}
		//
		// This point not reached
	}
	
	/**
	 * Sets the status and status message, but only if the status has changed
	 * <p>
	 * The first status message is usually the only useful one. Subsequent messages
	 * are more likely to be 'null' and similar
	 * @param st	ConnectionStatus enumerated status
	 * @param message	message, eg. exception error message
	 */
	private void setStatus(ConnectionStatus st, String message)
	{
		if (st != status)
		{
			status = st;
			statusmessage = message;
		}
	}
	
	/**
	 * Close this UAConnection's open network connection, if any. If 
	 * an exception is thrown, it is ignored
	 */
	private void closeConnection()
	{
		try
		{
			// Close the connection, if it's not already gone away
			connection.close();
		}
		catch (Exception ex)
		{
			// Ignore it
		}
	}
	
	/**
	 * Connect to UA server
	 * @param host	Hostname of UA server
	 * @param port	Port of UA server
	 */
	public boolean connect(String host, int port) throws UnknownHostException, IOException
	{
		try
		{
			connection = new Socket(host, port);
				
			input = connection.getInputStream();
			output = new java.io.DataOutputStream(connection.getOutputStream());
	
			inputparser = new org.ua2.edf.parser.EDFParser(input);
			
			sendEDF(new EDFData("edf", "on"));
			
			readEDF().print();		// FIXME - handle this properly
			System.out.println("Status: " + statusmessage);
			
			// Fire up the reader thread
			(new Thread(this)).start();
		}
		catch (Exception e)
		{
			setStatus(ConnectionStatus.CONNECTFAILED, e.getLocalizedMessage());
			
			return false;
		}
		
		setStatus(ConnectionStatus.CONNECTED, "Connected to UA server");
		
		return true;
	}
	
	/**
	 * Sends an EDF tree to the server
	 * @param edf	EDFData object to send
	 * @return	true if the sending suceeded, false otherwise
	 */
	public boolean sendEDF(EDFData edf)
	{
		try
		{
			output.writeBytes(edf.toString());
		}
		catch (Exception e)
		{
			// FIXME - is this the right connection status?
			// FIXME - throw a NoConnectionError exception
			setStatus(ConnectionStatus.CONNECTFAILED, e.getLocalizedMessage());
			
			return false;
		}
		
		return true;
	}
	
	/**
	 * Reads an EDFData object from the server
	 * @return	EDFData object, or null if there was an error
	 */
	public EDFData readEDF()
	{
		EDFData edf;
		
		try
		{
			edf = inputparser.elementtree();
		}
		catch (Exception e)
		{
			// FIXME - is this the right connection status?
			// FIXME - throw a NoConnectionError exception
			setStatus(ConnectionStatus.CONNECTFAILED, e.getLocalizedMessage());
			
			closeConnection();
			
			return null;
		}
		
		return edf;
	}
	
	public EDFData sendAndRead(EDFData send) throws NoConnectionError
	{
		EDFData replymessage;
		
		// Send data
		sendEDF(send);
		
		// Get reply
		try
		{
			// FIXME - needs a timeout
			replymessage = reply.take();
		}
		catch(InterruptedException e)
		{
			// Something went wrong
			setStatus(ConnectionStatus.NOTCONNECTED, "Internal error in server: " + e.getLocalizedMessage());
			closeConnection();
			
			throw new NoConnectionError(NoConnectionError.Reason.CONNECTIONLOST);
		}
		
		return replymessage;
	}

	public AnnounceQueue getAnnounceQueue()
	{
		return announcequeue;
	}
}

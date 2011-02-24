package org.ua2.clientlib;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.ua2.clientlib.exception.NoConnectionError;
import org.ua2.edf.EDFData;
import org.ua2.edf.parser.EDFParser;
import org.ua2.edf.parser.ParseException;
import org.ua2.edf.parser.TokenMgrError;

/**
 * A UAConnection manages an individual user's connection to the UA server
 * <p>
 * It deals with sending and receiving EDF, but not session management
 * 
 * @author brian
 * 
 */
public class UAConnection implements Runnable {

	private Socket connection;
	private InputStream input;
	private DataOutputStream output;
	private EDFParser inputparser;

	public enum ConnectionStatus {
		NOTCONNECTED, CONNECTFAILED, CONNECTED, LOSTCONNECTION
	};

	public ConnectionStatus status = ConnectionStatus.NOTCONNECTED;
	public String statusmessage = "Not connected";

	// SynchronousQueue is a "queue" which only allows one item on it
	// It blocks on write until something comes along and reads it, and
	// blocks on read until something writes to it.
	private SynchronousQueue<EDFData> reply = new SynchronousQueue<EDFData>();

	private AnnounceQueue announcequeue = new AnnounceQueue();

	private static final Logger logger = Logger.getLogger(UAConnection.class);

	/**
	 * This code is run in its own thread, started when the UA server connection
	 * is established
	 * <p>
	 * It loops forever (or until there's an error), calling readEDF (which
	 * blocks until an EDF tree is available from the parser), and, depending on
	 * the EDF message, either makes it available as a reply (presumed to be in
	 * response to a request) or pushes it onto the object's announcement FIFO.
	 */
	public void run() {
		try {
			EDFData inputedf;

			// Loop round parsing the input
			while (status == ConnectionStatus.CONNECTED) {
				logger.trace("Status " + status);
				inputedf = readEDF();
				if (inputedf == null) {
					break;
				}

				/*
				 * EDF from the server can be one of three things edf - response
				 * to connection message <edf="on"/>. Not used here reply -
				 * reply to a request announce - unsolicited announcement
				 */

				if (inputedf.name.equals("announce")) {
					// Pass to the announcement handler
					announcequeue.announce(inputedf);
					if (logger.isTraceEnabled()) logger.trace("Announcement:\n" + inputedf.format(true));
				} else if (inputedf.name.equals("reply")) {
					try {
						if (logger.isTraceEnabled())
							logger.trace("Reply:\n" + inputedf.format(true));

						// FIXME - needs a timeout.
						reply.put(inputedf);
					} catch (Exception e) {
						// Something went wrong
						setStatus(
								ConnectionStatus.NOTCONNECTED,
								"Internal error in server: "
										+ e.getLocalizedMessage());
						closeConnection();

						logger.error("Parser thread interrupted on put", e);

						//
						// Exit this thread (Thread.currentThread().stop() is
						// deprecated)
						return;
					}
				} else {
					// FIXME - what should we do with unknown EDF messages?
					logger.error("Unknown EDF response:\n"
							+ inputedf.format(true));
				}
			}
			//
			// This point not reached
		} catch (Exception e) {
			logger.error("Stopped loop due to error", e);
		}
	}

	/**
	 * Sets the status and status message, but only if the status has changed
	 * <p>
	 * The first status message is usually the only useful one. Subsequent
	 * messages are more likely to be 'null' and similar
	 * 
	 * @param st
	 *            ConnectionStatus enumerated status
	 * @param message
	 *            message, eg. exception error message
	 */
	private void setStatus(ConnectionStatus st, String message) {
		if (st != status) {
			status = st;
			statusmessage = message;
		}
	}

	/**
	 * Close this UAConnection's open network connection, if any. If an
	 * exception is thrown, it is ignored
	 */
	private void closeConnection() {
		try {
			// Close the connection, if it's not already gone away
			connection.close();
		} catch (Exception ex) {
			// Ignore it
		}
	}

	/**
	 * Connect to UA server
	 * 
	 * @param host
	 *            Hostname of UA server
	 * @param port
	 *            Port of UA server
	 */
	public boolean connect(String host, int port) throws UnknownHostException,
			IOException {
		try {
			connection = new Socket(host, port);

			input = connection.getInputStream();
			output = new java.io.DataOutputStream(connection.getOutputStream());

			inputparser = new org.ua2.edf.parser.EDFParser(input);

			sendEDF(new EDFData("edf", "on"));

			logger.debug("EDF on response:\n" + readEDF().format(true)); // FIXME
																			// -
																			// handle
																			// this
																			// properly
			logger.debug("Status: " + statusmessage);

			// Fire up the reader thread
			(new Thread(this)).start();
		} catch (Exception e) {
			setStatus(ConnectionStatus.CONNECTFAILED, e.getLocalizedMessage());

			return false;
		}

		setStatus(ConnectionStatus.CONNECTED, "Connected to UA server");

		return true;
	}

	/**
	 * Sends an EDF tree to the server
	 * 
	 * @param edf
	 *            EDFData object to send
	 * @return true if the sending suceeded, false otherwise
	 */
	public boolean sendEDF(EDFData edf) {
		try {
			String str = edf.toString();
			if (logger.isTraceEnabled())
				logger.trace("Writing EDF:" + str);
			output.writeBytes(str);
		} catch (Exception e) {
			// FIXME - is this the right connection status?
			// FIXME - throw a NoConnectionError exception
			setStatus(ConnectionStatus.CONNECTFAILED, e.getLocalizedMessage());

			return false;
		}

		return true;
	}

	private void handleError(Throwable t) {
		// FIXME - is this the right connection status?
		// FIXME - throw a NoConnectionError exception
		setStatus(ConnectionStatus.CONNECTFAILED, t.getLocalizedMessage());

		closeConnection();
	}

	void disconnect() {
		setStatus(ConnectionStatus.NOTCONNECTED, "Disconnected");
		closeConnection();
	}

	/**
	 * Reads an EDFData object from the server
	 * 
	 * @return EDFData object, or null if there was an error
	 */
	public EDFData readEDF() {
		EDFData edf = null;

		try {
			edf = inputparser.elementtree();
			if (logger.isTraceEnabled()) logger.trace("Read EDF:\n" + edf.format(true));
			return edf;
		} catch (TokenMgrError e) {
			logger.error("Cannot parse EDF", e);
			handleError(e);
		} catch (ParseException e) {
			logger.error("Cannot parse EDF", e);
			handleError(e);
		} catch (Exception e) {
			logger.error("Exception during EDF read", e);
			handleError(e);
		}

		logger.info("Null return, connection status " + status);
		return null;
	}

	public synchronized EDFData sendAndRead(EDFData send) throws NoConnectionError {
		EDFData replymessage = null;

		// Send data
		if (logger.isTraceEnabled()) logger.trace("Sending:\n" + send.format(true));
		sendEDF(send);

		// Get reply
		try {
			while (replymessage == null && status == ConnectionStatus.CONNECTED) {
				replymessage = reply.poll(5, TimeUnit.SECONDS);
			}
		} catch (Exception e) {
			logger.error("Internal error in server", e);
		}

		if (replymessage == null) {
			// Something went wrong
			setStatus(ConnectionStatus.NOTCONNECTED, "No reply");
			closeConnection();

			throw new NoConnectionError(NoConnectionError.Reason.CONNECTIONLOST);
		}
		
		if(logger.isTraceEnabled()) logger.trace("Sent " + send.getString() + " got " + replymessage.getString());

		if (logger.isTraceEnabled()) logger.trace("Read:\n" + replymessage.format(true));
		return replymessage;
	}

	public AnnounceQueue getAnnounceQueue() {
		return announcequeue;
	}
}

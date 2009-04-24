package org.ua2.clientlib;

import org.ua2.clientlib.exception.*;
import org.ua2.edf.EDFData;

/**
 * This class represents a message on UA, and all that can be done with it
 * @author brian
 *
 */
public class Message
{
	/**
	 * The various message types this class can represent
	 * <p>
	 * NONE is simply a blank/unassigned message		<br>
	 * POST is a post to a folder						<br>
	 * PAGE is an ephemeral message between two users	<br>
	 * BULLETIN is an instance of the (rarely used) system bulletin
	 * @author brian
	 *
	 */
	public enum MessageType { NONE, POST, PAGE, BULLETIN };
	
	public enum Reason
	{
		OK, 				//	Message sent
		FORBIDDEN,			//	User is attempting something beyond their access level
		PAGE_BUSY,			//	Receiving user's pager is busy
		PAGE_UNAVAIL,		//	Receiving user is unpageable
		NOSUCHRECIPIENT,	//	Receiving user (page or Private) does not exist
		POST_NOFOLDER		//	Can't post to nonexistent folder
	}
	
	public MessageType type = MessageType.NONE;
	public int toid = -1;		// User id of To: user, if relevant
	public String to;			// Text string for To: user
	public int fromid = -1;		// User id of From: user, if relevant
	public String from;			// Text string for From: user
	public String subject;
	public String body;
	public int folderid = -1;	// Folder ID, if relevant
	public int date = -1;		// POSIX date
	public int id = -1;			// Message ID, if relevant
	public int inreplyto=  -1;	// Message this is a reply to, if relevant
	// FIXME - posts can have multiple parent posts, including some which are no longer available
	// FIXME - no support for attachments/annotations
	// FIXME - deleted posts are visible to admins
	
	// FIXME - not decided how to deal with this yet
	private UAConnection ua;
	
	/**
	 * Create a blank Message
	 */
	public Message()
	{
	}
	
	/**
	 * Create a Message based on an EDFData tree
	 * <p>
	 * @param edftree	EDF data representing &lt;announcement="user_page"&gt;
	 */
	public Message(EDFData edftree)
	{
		
	}
	
	/**
	 * Set the UA connection
	 * @param connection UAConnection object used to send messages
	 */
	public void setUAConnection(UAConnection connection)
	{
		ua = connection;
	}

	/**
	 * Create a new Message, populating the fields as a suitable reply to the current message
	 * @return new Message object
	 * @throws MessageInvalidOperation 
	 */
	public Message reply() throws MessageInvalidOperation
	{
		if((type != MessageType.PAGE) && (type != MessageType.POST))
		{
			throw new MessageInvalidOperation("Can't reply to something which isn't a post or page");
		}
		
		Message reply = new Message();

		reply.type = type;
		/* FIXME - what to do about this
		reply.from = 
		reply.fromid =
		*/
		reply.to = from;
		reply.toid = fromid;
		reply.subject = subject;
		/*
		reply.date = 
		*/
		reply.inreplyto = id;
		
		return reply;
	}
	
	/**
	 * Post the message
	 * @return Reason code
	 * @throws MessageInvalidOperation attempting to post something which can't be posted
	 * @throws MessageInvalidData some data is missing (eg no subject line) or incorrectly specified
	 * @throws NoConnectionError not connected to UA for some reason
	 */
	public Reason post() throws MessageInvalidOperation, MessageInvalidData, NoConnectionError
	{
		switch(type)
		{
			case NONE:	// Assume this message is postable
				type = MessageType.POST;
				break;
			case POST:	// This is already ok
				break;
			case PAGE:	// Convert this to a Private message
				type = MessageType.POST;
				subject = "Diverted page";
				/* FIXME - need Folder ID */
				break;
			default:	// Can't post this type
				throw new MessageInvalidOperation("Can't post message type");
		}
		
		//TODO - check data
		
		// Check message subject
		if(subject.length() < 1)
		{
			throw new MessageInvalidData("Subject line is missing");
		}
		// TODO - max subject line length
		
		// TODO - username/id validity
		
		// TODO - folderid validity
		
		// TODO - to, toid
		EDFData post = new EDFData("request", "message_add");
		post.add("folderid", folderid);
		post.add("subject", subject);
		post.add("text", body);
		
		if(toid != -1)
		{
			post.add("toid", toid);
		}
		
		EDFData reply = ua.sendAndRead(post);
		
		if(reply.sValue.equals("message_add"))
		{
			// Message added successfully
			return Reason.OK;
		}
		else
		{
			// FIXME - handle other problems too
			// TODO - server returns <reply="rq_invalid><request="message_add"/><scope=3/></reply> if not logged in
			return Reason.POST_NOFOLDER;
		}
	}
	
	public Reason page() throws MessageInvalidOperation, NoConnectionError
	{
		switch(type)
		{
			case NONE:	// Assume this message is pageable
				type = MessageType.PAGE;
				break;
			case POST:	// This is ok
				type = MessageType.PAGE;
				break;
			case PAGE:	// This is ok
				break;
			default:	// Can't page this message type
				throw new MessageInvalidOperation("Can't page message type");
		}
		
		// TODO - check data
		// TODO - recipient username/id validity
		// TODO - subject line
		
		EDFData page = new EDFData("request", "user_contact");

		page.add("toid", toid);
		page.add("text", body);
		
		EDFData reply = ua.sendAndRead(page);
		
		if(reply.sValue.equals("user_contact"))
		{
			return Reason.OK;
		}
		
		if(reply.sValue.equals("user_busy"))
		{
			return Reason.PAGE_BUSY;
		}
		
		if(reply.sValue.equals("user_not_on"))
		{
			return Reason.PAGE_UNAVAIL;
		}
		
		// FIXME - handle other problems too
		// TODO - server returns <reply="rq_invalid><request="message_add"/><scope=3/></reply> if not logged in
		
		throw new Error("Unhandled problem in user_page");
	}
}

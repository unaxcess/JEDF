package org.ua2.edf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.ua2.edf.parser.EDFParser;
import org.ua2.edf.parser.ParseException;
import org.ua2.edf.parser.TokenMgrError;

public class TestFiles extends TestCase {
	public static EDFData parseFile(String filename) throws IOException {
		InputStream stream = null;
		try {
			stream = new FileInputStream(filename);
			EDFParser parser = new EDFParser(stream);
			try {
				return parser.elementtree();
			} catch (TokenMgrError e) {
				fail("Cannot parse " + filename + ". " + e.getMessage());
			} catch (ParseException e) {
				fail("Cannot parse " + filename + ". " + e.getMessage());
			}
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	private void parseRequest(String prefix) throws IOException {
		parseFile(prefix + "_request.edf");
	}

	private void parseReply(String prefix) throws IOException {
		parseFile(prefix + "_reply.edf");
	}

	private void parsePair(String prefix) throws IOException {
		parseRequest("test/" + prefix);
		parseReply("test/" + prefix);
	}

	public void testBulletinList() throws IOException {
		parsePair("bulletin_list");
	}

	public void testChannelList() throws IOException {
		parsePair("channel_list");
	}

	public void testFolderList1() throws IOException {
		parsePair("folder_list1");
	}

	public void testFolderList2() throws IOException {
		parsePair("folder_list2");
	}

	public void testMessageList2() throws IOException {
		parsePair("message_list2");
	}

	public void testServiceList() throws IOException {
		parsePair("service_list");
	}

	public void testSystemList1() throws IOException {
		parsePair("system_list1");
	}

	public void testSystemList2() throws IOException {
		parsePair("system_list2");
	}

	public void testUserList() throws IOException {
		parsePair("user_list1");
	}

	public void testUserList2() throws IOException {
		parsePair("user_list2");
	}

	public void testUserLogin1() throws IOException {
		parsePair("user_login1");
	}

	public void testUserLogin2() throws IOException {
		parsePair("user_login2");
	}
}

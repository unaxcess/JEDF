package org.ua2.edf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;
import org.ua2.edf.parser.EDFParser;
import org.ua2.edf.parser.ParseException;

public class ParserTest {

	@Test
	public void testParseMinimal() throws ParseException {
		Reader reader = new StringReader("<></>");
		EDFParser parser = new EDFParser(reader);

		EDFData data = parser.elementtree();

		assertTrue("".equals(data.getName()));
		assertNull(data.getValue());
	}

	@Test
	public void testParseBlankNameIntValue() throws ParseException {
		Reader reader = new StringReader("<=1/>");
		EDFParser parser = new EDFParser(reader);

		EDFData data = parser.elementtree();

		assertTrue("".equals(data.getName()));
		assertEquals((Integer)1, data.getInteger());
	}

	@Test
	public void testParseBlankNameStrValue() throws ParseException {
		Reader reader = new StringReader("<=\"first\"></>");
		EDFParser parser = new EDFParser(reader);
		EDFData data = parser.elementtree();

		assertTrue("".equals(data.getName()));
		assertEquals("first", data.getString());
	}

	@Test
	public void testParseNameIntValue() throws ParseException {
		Reader reader = new StringReader("<one=1/>");
		EDFParser parser = new EDFParser(reader);
		EDFData data = parser.elementtree();

		assertTrue("one".equals(data.getName()));
		assertEquals((Integer)1, data.getInteger());
	}

	@Test
	public void testParseNameStrValue() throws ParseException {
		Reader reader = new StringReader("<one=\"first\"></>");
		EDFParser parser = new EDFParser(reader);
		EDFData data = parser.elementtree();

		assertTrue("one".equals(data.getName()));
		assertEquals("first", data.getString());
	}

	@Test
	public void testParseNameDataValue() throws ParseException {
		Reader reader = new StringReader("<one=\"first\u0101\"></>");
		EDFParser parser = new EDFParser(reader);
		EDFData data = parser.elementtree();

		assertTrue("one".equals(data.getName()));
		assertEquals("first\u0101", data.getString());
	}

	@Test
	public void testParseMixedCaseNameStrValue() throws ParseException {
		Reader reader = new StringReader("<OneTwoThree=\"first\"></OneTwoThree>");
		EDFParser parser = new EDFParser(reader);
		EDFData data = parser.elementtree();

		assertTrue("OneTwoThree".equals(data.getName()));
		assertEquals("first", data.getString());
	}

	@Test
	public void testParseNameValueNoChildren() throws ParseException {
		Reader reader = new StringReader("<one=1></one>");
		EDFParser parser = new EDFParser(reader);
		EDFData data = parser.elementtree();

		assertTrue("one".equals(data.getName()));
		assertEquals((Integer)1, data.getInteger());
	}

	@Test
	public void testParseMinimalChildren() throws ParseException {
		Reader reader = new StringReader("<><two=\"second\"/><three=3><four=\"fourth\"></></two></>");
		EDFParser parser = new EDFParser(reader);
		EDFData data = parser.elementtree();

		assertTrue("".equals(data.getName()));
		assertNull(data.getValue());

		EDFData two = data.getChild("two");
		assertTrue(two != null);
		assertEquals("second", two.getString());

		EDFData three = data.getChild("three");
		assertTrue(three != null);
		assertEquals((Integer)3, three.getInteger());

		EDFData four = three.getChild("four");
		assertTrue(three != null);
		assertEquals("fourth", four.getString());
	}

	@Test
	public void testParseNameValueChildren() throws ParseException {
		Reader reader = new StringReader("<one=1><two=\"second\"/><three=3><four=\"fourth\"></></two></one>");
		EDFParser parser = new EDFParser(reader);
		EDFData data = parser.elementtree();

		assertTrue("one".equals(data.getName()));
		assertEquals((Integer)1, data.getInteger());

		EDFData two = data.getChild("two");
		assertTrue(two != null);
		assertEquals("second", two.getString());

		EDFData three = data.getChild("three");
		assertTrue(three != null);
		assertEquals((Integer)3, three.getInteger());

		EDFData four = three.getChild("four");
		assertTrue(three != null);
		assertEquals("fourth", four.getString());
	}

}

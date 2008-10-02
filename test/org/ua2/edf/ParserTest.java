package org.ua2.edf;

import static org.junit.Assert.assertTrue;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;
import org.ua2.edf.parser.EDFParser;
import org.ua2.edf.parser.ParseException;

public class ParserTest {
    @Test public void testParse1() {
        Reader reader = new StringReader("<user=1/>");
        EDFParser parser = new EDFParser(reader);
        try {
            EDFData data = parser.elementtree();
            
            String name = data.getName();
            assertTrue(name.equals("user"));
            
            int value = data.getInteger();
            assertTrue(value == 1);
        } catch (ParseException e) {
        }
    }

    @Test public void testParse2() {
        Reader reader = new StringReader("<subject=\"Foobar\"/>");
        EDFParser parser = new EDFParser(reader);
        try {
            EDFData data = parser.elementtree();
            
            String name = data.getName();
            assertTrue(name.equals("subject"));
            
            String value = data.getString();
            assertTrue(value.equals("Foobar"));
        } catch (ParseException e) {
        }
    }

    @Test public void testParse3() {
        Reader reader = new StringReader("<wibble><one=1/><two=2/><three=3/></wibble>");
        EDFParser parser = new EDFParser(reader);
        try {
            EDFData data = parser.elementtree();
            
            assertTrue(data.getChildCount() == 3);

            assertTrue(data.getChildren("two").size() == 1);
        } catch (ParseException e) {
        }
    }
}

package org.ua2.edf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import junit.framework.TestCase;

public class TestMethods extends TestCase {

	private void testFolder(EDFData data, BufferedReader reader) throws IOException {
		List<EDFData> children = data.getChildren("folder");
		for(EDFData child : children) {
			String edfName = child.getChild("name").getString();
			String checkName = reader.readLine(); 
			System.out.println("Asserting " + edfName + " -vs- " + checkName);
			assertEquals(edfName, checkName);
			
			testFolder(child, reader);
		}
	}

	public void testAllFolders() throws IOException {
		EDFData data = TestFiles.parseFile("test/AllFolders.edf");
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("test/AllFolderNames.txt")));
		
		testFolder(data, reader);
	}
}

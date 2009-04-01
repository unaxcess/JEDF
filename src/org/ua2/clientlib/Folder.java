package org.ua2.clientlib;

import java.util.List;
import java.util.ArrayList;

import org.ua2.clientlib.exception.*;
import org.ua2.edf.*;

public class Folder {
	public String name;
	public int id;
	public int unread = 0;

	private ArrayList<Integer> editors = new ArrayList<Integer>();			// User ID of editor
	// TODO - other folder attributes
	
	/**
	 * Creates a folder object with no attributes set
	 */
	public Folder()
	{
	}

	/**
	 * Creates a folder from an EDFData
	 * @param edftree EDFData tree representing a folder
	 * @throws WrongEDFException the EDFData tree didn't contain the correct information to describe a folder

	 */
	public Folder(EDFData edftree) throws WrongEDFException
	{
		// Check the root element
		if((! edftree.name.equalsIgnoreCase("folder")) || (edftree.type != EDFData.ValueType.INTEGER))
		{
			throw new WrongEDFException("Expected '<folder=[number]></>");
		}
		
/*		EDFData nameelement = edftree.getChild("name");
		if(nameelement == null)
		{
			throw new WrongEDFException("Expected <name> element");
		}
		
		if((nameelement.type != EDFData.ValueType.STRING) || (nameelement.sValue.isEmpty()))
		{
			throw new WrongEDFException("Expected <name=[string]/>");
		}
		
		name = nameelement.sValue;
*/
		id = edftree.iValue;

		for(EDFData child : edftree.children)
		{
			if(child.name.equals("name"))
			{
				if((child.type != EDFData.ValueType.STRING) || (child.sValue.isEmpty()))
				{
					throw new WrongEDFException("Expected <name=[string]/>");
				}
				
				name = child.sValue;
				continue;
			}
			
			if(child.name.equals("unread"))
			{
				if(child.type != EDFData.ValueType.INTEGER)
				{
					throw new WrongEDFException("Expected <unread=[number]/>");
				}
				
				unread = child.iValue;
				continue;
			}

			// FIXME - multiple editors possible (eg, Databases)
			if(child.name.equals("editor"))
			{
				if(child.type != EDFData.ValueType.INTEGER)
				{
					throw new WrongEDFException("Expected <editor=[number]/>");
				}
				
				editors.add(child.iValue);
				continue;
			}

			
		}
		
		if((name == null))
		{
			throw new WrongEDFException("<folder> is missing a required element");
		}
		
		// TODO - the other fields
	}
	
	public List<Integer> getEditors()
	{
		return editors;
	}
}

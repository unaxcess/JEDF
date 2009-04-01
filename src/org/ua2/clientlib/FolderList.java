package org.ua2.clientlib;

import org.ua2.clientlib.exception.*;
import org.ua2.edf.EDFData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class to handle folders - it manages, and keeps up-to-date,
 * a list of folders the user has access to
 * @author brian
 *
 */
public class FolderList
{
	private ConcurrentHashMap<String, Folder> folders = new ConcurrentHashMap<String, Folder>();
	private UAConnection ua;
	
	/**
	 * Creates a new FolderList
	 * @param uaconnection	a connection to UA
	 */
	public FolderList(UAConnection uaconnection)
	{
		ua = uaconnection;
	}
	
	/**
	 * Refresh the list of folders
	 */
	public void refresh() throws UAException
	{
		ConcurrentHashMap<String, Folder> newfolders = new ConcurrentHashMap<String, Folder>();
		
		EDFData request = new EDFData("request", "folder_list");
		EDFData reply = ua.sendAndRead(request);

		for(EDFData folderdata : reply.getChildren("folder"))
		{
			Folder folder = new Folder(folderdata);
			
			newfolders.put(folder.name, folder);
		}
		
		folders = newfolders;
	}
	
	public List<Folder> getFolderList()
	{
		ArrayList<Folder> folderlist = new ArrayList<Folder>(folders.values());

		// Ewww, ewww, ewww, ewww, ewww
		// FIXME - this shit is just for testing
		Collections.sort(folderlist, new Comparator<Folder>() {
			public int compare(org.ua2.clientlib.Folder arg0,
					org.ua2.clientlib.Folder arg1) {

				return arg0.name.compareToIgnoreCase(arg1.name);
			}
		});
		
		return folderlist;
	}
	
	/**
	 * Gets the named folder, if it exists, or null
	 * @param name	folder name
	 * @return Folder, or null
	 */
	public Folder getFolder(String name)
	{
		return folders.get(name);
	}
}

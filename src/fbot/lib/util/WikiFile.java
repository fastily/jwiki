package fbot.lib.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Represents a file in your file system. Can be used to represent directories and files. Makes it easy to represent and
 * find files which are okay to upload to Wikis.
 * 
 * @author Fastily
 * 
 */
public class WikiFile
{
	/**
	 * Is this object a valid, existent directory?
	 */
	private boolean isDirectory = false;
	
	/**
	 * Is this object a valid, existent file which is okay to upload?
	 */
	private boolean isUploadable = false;
	
	/**
	 * Does this pathname even exist? If it doesn't it could be either a file
	 */
	private boolean exists = true;
	
	/**
	 * The internal representation of our File
	 */
	private File f;
	
	/**
	 * This file's name, or the last element in it's path.
	 */
	private String name;
	
	/**
	 * Constructor, creates a WikiFile
	 * 
	 * @param f File to create object with.
	 */
	public WikiFile(File f)
	{
		this.f = f;
		name = f.getName();
		
		if (f.isDirectory())
			isDirectory = true;
		else if (f.isFile() && canUpload(name))
			isUploadable = true;
		else
			exists = false;
	}
	
	/**
	 * Constructor, creates a WikiFile. Use full path for best results.
	 * 
	 * @param s The path to the file to use.
	 */
	public WikiFile(String s)
	{
		this(new File(s));
	}
	
	/**
	 * Gets this file's extension, if it is a file. Returns null if the object is a directory, or the empty string if
	 * the file has no extension.
	 * 
	 * @param useDot Should the extension begin with a dot?
	 * @return This file's extension, if possible.
	 */
	public String getExtension(boolean useDot)
	{
		if (isDirectory)
			return null;
		
		int i = name.lastIndexOf('.'); // special case, file has no extension.
		if (i == -1)
			return "";
		
		return name.substring(name.lastIndexOf('.') + (useDot ? 0 : 1));
	}
	
	/**
	 * Gets this file's name, without the pathname.
	 * 
	 * @param withExt Set to true if you want the extension included in the result (if applicable)
	 * @return The name of the file without pathname, as specified.
	 */
	public String getName(boolean withExt)
	{
		if (!withExt)
			return name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name;
		return name;
	}
	
	/**
	 * Gets a File representation of this object.
	 * 
	 * @return A File representation of this object.
	 */
	public File getFile()
	{
		return f;
	}
	
	/**
	 * Lists this directory's files.
	 * 
	 * @param canUploadOnly Set to true if you want only wiki-uploadable files.
	 * @return The list of this directory's files, as specified.
	 */
	public WikiFile[] listFiles(boolean canUploadOnly)
	{
		if (!isDirectory)
			return null;
		ArrayList<WikiFile> fl = new ArrayList<WikiFile>();
		
		if (canUploadOnly)
		{
			for (File x : f.listFiles())
				if (x.isFile() && canUpload(x.getName()))
					fl.add(new WikiFile(x));
		}
		else
		{
			for (File x : f.listFiles())
				if (x.isFile())
					fl.add(new WikiFile(x));
		}
		
		return fl.toArray(new WikiFile[0]);
	}
	
	/**
	 * Returns this WikiFile's subdirectories, if applicable.
	 * 
	 * @return This WikiFile's subdirs, or null if this object is not a directory.
	 */
	public WikiFile[] listDirs()
	{
		if (!isDirectory)
			return null;
		
		ArrayList<WikiFile> wfl = new ArrayList<WikiFile>();
		for (File x : f.listFiles())
			if (x.isDirectory())
				wfl.add(new WikiFile(x));
		return wfl.toArray(new WikiFile[0]);
	}
	
	/**
	 * Recursively gets a directory's files and files in any subdirectories.
	 * 
	 * @param canUploadOnly Set to True if you want Wiki-uploadable files.
	 * @return A list of this directory's files and sub-directories' files as specified.
	 */
	public WikiFile[] listFilesR(boolean canUploadOnly)
	{
		if (!isDirectory)
			return null;
		
		ArrayList<WikiFile> wfl = new ArrayList<WikiFile>();
		wfl.addAll(Arrays.asList(listFiles(canUploadOnly)));
		
		for (WikiFile dir : listDirs())
			wfl.addAll(Arrays.asList(dir.listFilesR(canUploadOnly)));
		
		return wfl.toArray(new WikiFile[0]);
	}
	
	/**
	 * Recursively gets this directory's subdirectories.
	 * 
	 * @return The list of *all* subdirectories in this directory.
	 */
	public WikiFile[] listDirsR()
	{
		if(!isDirectory)
			return null;
		
		HashSet<WikiFile> dl = new HashSet<WikiFile>(); //cumulative
		List<WikiFile> dirs = Arrays.asList(listDirs()); //this node only
		
		Arrays.asList(listDirs());
		for(WikiFile d : dirs)
			dl.addAll(Arrays.asList(d.listDirsR()));
		
		dl.addAll(dirs);
		return dl.toArray(new WikiFile[0]);
	}
	
	
	/**
	 * Gets flag stating whether this is a directory.
	 * 
	 * @return True if this object represents a directory.
	 */
	public boolean isDir()
	{
		return isDirectory;
	}
	
	/**
	 * Gets flag stating whether this is an uploadable file.
	 * 
	 * @return True if this object represents an uploadable file.
	 */
	public boolean canUp()
	{
		return isUploadable;
	}
	
	/**
	 * Gets flag stating whether something exists at this pathname.
	 * 
	 * @return True if this object represents an existing pathname.
	 */
	public boolean doesExist()
	{
		return exists;
	}
	
	/**
	 * Gets the absolute path represented by this object.
	 * 
	 * @return The absolute path of this object.
	 */
	public String getPath()
	{
		try
		{
			return f.getCanonicalPath(); // ideal representation
		}
		catch (Throwable e)
		{
			return f.getAbsolutePath();
		}
	}
	
	/**
	 * Tries to get the parent directory of this WikiFile.
	 * 
	 * @param fullpath Set to true if we want the absolute pathname. Otherwise, just the name of the directory is
	 *            returned.
	 * @return Tehe parent directory of the WikiFile, or null if there is no parent.
	 */
	public String getParent(boolean fullpath)
	{
		return fullpath ? getPath() : f.getParentFile().getName();
	}
	
	/**
	 * Gets a String representation of this object. Pretty much same thing as getPath();
	 * 
	 * @return The absolute path of this object.
	 */
	public String toString()
	{
		return getPath();
	}
	
	/**
	 * Can we upload this title?
	 * 
	 * @param title The title to check
	 * @return True if the title is uploadable.
	 */
	private static boolean canUpload(String title)
	{
		return title.matches("(?i).+?\\.(png|gif|jpg|jpeg|xcf|mid|ogg|ogv|oga|svg|djvu|tiff|tif|pdf|webm|flac|wav)");
	}
	
	/**
	 * Converts File object(s) to a WikiFile object.
	 * 
	 * @param files File objects to convert
	 * @return An array of corresponding WikiFile objects.
	 */
	public static WikiFile[] convertTo(File... files)
	{
		ArrayList<WikiFile> wfl = new ArrayList<WikiFile>();
		for (File f : files)
			wfl.add(new WikiFile(f));
		
		return wfl.toArray(new WikiFile[0]);
	}
	
	/**
	 * Converts WikiFile object(s) to File objects.
	 * 
	 * @param files WikiFile objects to convert
	 * @return An array of corresponding File objects.
	 */
	public static File[] convertFrom(WikiFile... files)
	{
		ArrayList<File> fl = new ArrayList<File>();
		for (WikiFile wf : files)
			fl.add(wf.getFile());
		
		return fl.toArray(new File[0]);
	}
	
}
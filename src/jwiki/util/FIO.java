package jwiki.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Read, write, and file system functions.
 * 
 * @author Fastily
 * 
 */
public class FIO
{
	/**
	 * Constructors disallowed.
	 */
	private FIO()
	{

	}

	/**
	 * Reads contents of an InputStream to a String.
	 * 
	 * @param is The InputStream to read into a String
	 * @return The String we made from the InputStream, or the empty String if something went wrong.
	 */
	public static String inputStreamToString(InputStream is)
	{
		try (BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8")))
		{
			String x = "";

			String line;
			while ((line = in.readLine()) != null)
				x += line + "\n";

			is.close();
			return x.trim();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * Gets the file name pointed to by a path object and returns it as a String. Works for both directories and files.
	 * 
	 * @param p The filename to get a name for.
	 * @return The file's name
	 */
	public static String getFileName(Path p)
	{
		return p.getFileName().toString();
	}

	/**
	 * Gets a file's extension and returns it as a String. The path MUST point to a valid file, or you'll get null.
	 * 
	 * @param p The pathname to get an extension for.
	 * @param useDot Set to true to include a filename dot.
	 * @return The file's extension, or the empty string if the file has no extension.
	 */
	public static String getExtension(Path p, boolean useDot)
	{
		if (Files.isDirectory(p))
			return null;

		String name = p.getFileName().toString();
		int i = name.lastIndexOf('.'); // special case, file has no extension.

		return i == -1 ? "" : name.substring(i + (useDot ? 0 : 1));
	}

	/**
	 * Dumps lines to a file.
	 * 
	 * @param path The path to dump to
	 * @param timestamp If true, include a timestamp header boundary for each set of lines dumped.
	 * @param lines The lines to write out to. These will be separated by the system default line separator.
	 */
	public static void dumpToFile(String path, boolean timestamp, String... lines)
	{
		try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(path), Charset.defaultCharset(), StandardOpenOption.CREATE,
				StandardOpenOption.WRITE, StandardOpenOption.APPEND))
		{			
			if(timestamp)
				bw.write(String.format("=== %s ===%n", LocalDateTime.now().toString()));
			bw.write(FString.fenceMaker(FSystem.lsep, lines));
			bw.close();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Recursively search a directory for files whose names match the specified regex.
	 * 
	 * @param root The root directory to search
	 * @param regex The regex to match files for. This MUST start with the 'regex:' prefix as described <a href=
	 *           "http://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#getPathMatcher%28java.lang.String%29"
	 *           >here</a>.
	 * @return A list of files matching the specified regex.
	 */
	public static ArrayList<Path> findFiles(Path root, String regex)
	{
		UploadFinder uf = new UploadFinder(regex);
		try
		{
			Files.walkFileTree(root, uf);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return new ArrayList<Path>();
		}

		return uf.pl;
	}

	/**
	 * Recursively search a directory for files which can be uploaded to WMF wikis.
	 * 
	 * @param root The root directory to search
	 * @return A list of files that we can upload to Commons
	 */
	public static ArrayList<Path> findFiles(Path root)
	{
		return findFiles(root, "regex:(?i).+?\\.(png|gif|jpg|jpeg|xcf|mid|ogg|ogv|oga|svg|djvu|tiff|tif|pdf|webm|flac|wav)");
	}

	/**
	 * Visits files and records files we're interested in
	 * 
	 * @author Fastily
	 *
	 */
	private static class UploadFinder extends SimpleFileVisitor<Path>
	{

		/**
		 * Matches files we're interested in
		 */
		private PathMatcher pm;

		/**
		 * Stores files we matched
		 */
		private ArrayList<Path> pl = new ArrayList<Path>();

		/**
		 * Constructor, takes a regex representing files to match
		 * 
		 * @param regex Files to match.
		 */
		private UploadFinder(String regex)
		{
			pm = FileSystems.getDefault().getPathMatcher(regex);
		}

		/**
		 * Overrides SimpleFileVisitor's crap implementation of exiting upon failure.
		 */
		public FileVisitResult visitFileFailed(Path file, IOException exc)
		{
			return FileVisitResult.CONTINUE;
		}

		/**
		 * Will visit and record the names of files matching our regex.
		 */
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		{
			if (pm.matches(file))
				pl.add(file);
			return FileVisitResult.CONTINUE;
		}
	}
}
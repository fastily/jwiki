package fbot.lib.commons;

/**
 * Special reason strings for Commons work.
 * 
 * @author Fastily
 * 
 */
public class CStrings
{
	/**
	 * Hiding constructor from javadoc
	 */
	private CStrings()
	{
		
	}
	/**
	 * Uploader requested deletion of file
	 */
	public static final String ur = "Uploader requested deletion of a recently uploaded and unused file";
	
	/**
	 * If you are copyright holder, email OTRS.
	 */
	public static final String baseP = "If you are the copyright holder/author and/or have authorization to publish the file, please email our [[Commons:OTRS|OTRS team]] to get the file restored";
	
	/**
	 * Copyvio
	 */
	public static final String copyvio = "[[Commons:Licensing|Copyright violation]]: " + baseP;
	
	/**
	 * Derivative work
	 */
	public static final String dw = "[[Commons:Derivative works|Derivative]] of non-free content";
	
	/**
	 * Orphaned file talk page
	 */
	public static final String oft = "Orphaned File talk page";
	
	/**
	 * out of scope
	 */
	public static final String oos = "Out of [[Commons:Project scope|project scope]]";
	
	/**
	 * Housekeeping
	 */
	public static final String house = "Housekeeping or non-controversial cleanup";
	
	/**
	 * Fair use material is not allowed
	 */
	public static final String fu = "[[Commons:Fair use|Fair use]] material is not permitted on Wikimedia Commons";
	
	/**
	 * Category name for "Other speedy deletions", without the "Category:" prefix.
	 */
	public static final String osd = "Other speedy deletions";
	
	/**
	 * Category name for "Copyright violations", without the "Category:" prefix.
	 */
	public static final String cv = "Copyright violations";
	
	/**
	 * Reason for empty category.
	 */
	public static final String ec = "Empty [[Commons:Categories|category]]";
	
	/**
	 * Reason for file page with no file uploaded.
	 */
	public static final String nfu = "File page with no file uploaded";
	
	/**
	 * Reason for empty gallery
	 */
	public static final String eg = "Empty or single image gallery; please see [[Commons:Galleries]]";
	
	/**
	 * Reason for test page.
	 */
	public static final String test = "Test page or page with no valid content";
	
	/**
	 * Regex that matches deletion templates on Commons.
	 */
	public static final String delregex = "(?si)\\{\\{(speedy|no permission|no license|no source|copyvio).*?\\}\\}";
	
	/**
	 * Regex that matches DR templates on Commons.
	 */
	public static final String drregex = "(?si)\\{\\{(delete).*?\\}\\}";
	
	/**
	 * User requested in own userspace.
	 */
	public static final String uru = "User requested deletion in own [[Commons:Userpage|userspace]]";
}
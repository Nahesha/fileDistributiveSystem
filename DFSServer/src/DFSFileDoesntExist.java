
/**
 * Used when the client tries to open a file that doesn't exist.
 * 
 * @author merlin
 *
 */
public class DFSFileDoesntExist extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String fileTitle;

	/**
	 * @param fileTitle
	 *            the title of the file we couldn't open
	 */
	public DFSFileDoesntExist(String fileTitle)
	{
		this.fileTitle = fileTitle;
	}

	/**
	 * @see java.lang.Throwable#toString()
	 */
	public String toString()
	{
		return super.toString() + " - Can't locate a file named " + fileTitle;
	}
}

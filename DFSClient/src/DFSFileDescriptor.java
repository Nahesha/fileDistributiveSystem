
/**
 * Keeps track of a file and where we are in reading it
 * 
 * @author merlin
 *
 */
public class DFSFileDescriptor
{
	private int fileID;

	private int currentLocation;

	/**
	 * Remember the file and start a location 0
	 * 
	 * @param fileID
	 *            the file
	 */
	public DFSFileDescriptor(int fileID)
	{
		super();
		this.fileID = fileID;
		this.currentLocation = 0;
	}

	/**
	 * @return the current location we should read from
	 */
	public int getCurrentLocation()
	{
		return currentLocation;
	}

	/**
	 * @return the file we are working with
	 */
	public int getFileID()
	{
		return fileID;
	}

	/**
	 * @param change
	 *            move the current location by this much
	 */
	public void incrementCurrentLocation(int change)
	{
		currentLocation = currentLocation + change;
	}
}

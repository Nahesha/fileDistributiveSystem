import java.util.Scanner;

/**
 * The user has a file open and wants to read from it.
 * 
 * @author merlin
 *
 */
public class ReadCommand implements ClientCommand
{

	private DFSFileDescriptor fd;

	/**
	 * @param fd
	 *            the file we want to read from
	 */
	public ReadCommand(DFSFileDescriptor fd)
	{
		this.fd = fd;
	}

	/**
	 * Ask how many chars to read, make the server read them and output the result.
	 * 
	 * @see ClientCommand#execute()
	 */
	@Override
	public void execute()
	{
		Scanner keyBoard = DFSClient.getKeyboard();
		System.out.println("Please enter the number of characters you need");
		int length = keyBoard.nextInt();
		keyBoard.nextLine();
		System.out.println(
				DFSClient.getClientToServerImpl().readFromFile(fd.getFileID(), fd.getCurrentLocation(), length));
		fd.incrementCurrentLocation(length);

	}

}

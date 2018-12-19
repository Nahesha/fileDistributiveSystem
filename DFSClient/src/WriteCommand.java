import java.util.Scanner;





public class WriteCommand implements ClientCommand
{
	private DFSFileDescriptor fd;
	
	public WriteCommand(DFSFileDescriptor fd)
	{
		this.fd = fd;
	}

	@Override
	public void execute() 
	{
		Scanner keyBoard = DFSClient.getKeyboard();
		System.out.println("Enter the text you want to write to the file.");
		String input = keyBoard.nextLine();
		DFSClient.getClientToServerImpl().writeToFile(fd.getFileID(), fd.getCurrentLocation(), input);
	}

}

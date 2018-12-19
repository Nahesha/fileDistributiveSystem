import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

import DFSApp.ClientToServer;
import DFSApp.ClientToServerPackage.FileLockedForWriteFailure;
import DFSApp.ClientToServerPackage.FileNotFoundFailure;

/**
 * The user wants to open a file for Read. Get the file information from him,
 * open the file, and, if that is successful, change the menu state to be things
 * he can do to that open file
 * 
 * @author merlin
 *
 */
public class OpenForReadCommand implements ClientCommand
{

	private ClientToServer clientServerImpl;
	private String[] openForReadOptions =
	{ "Read", "Close the file" };
	private String[] openForReadCommands =
	{ "ReadCommand", "CloseFile" };

	private ClientCommand[] buildOpenForReadCommands(int fileID)
	{
		ClientCommand[] cmds = new ClientCommand[openForReadOptions.length];
		int i = 0;
		for (String cmdClassName : openForReadCommands)
		{
			try
			{
				cmds[i] = (ClientCommand) Class.forName(cmdClassName).getConstructor(DFSFileDescriptor.class)
						.newInstance(new DFSFileDescriptor(fileID));
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException | ClassNotFoundException e)
			{
				e.printStackTrace();
				System.exit(-1);
			}
			i++;
		}
		return cmds;
	}

	/**
	 * @see ClientCommand#execute()
	 */
	@Override
	public void execute()
	{
		Scanner keyBoard = DFSClient.getKeyboard();
		System.out.println("Please enter the name of the file you need");
		String fileTitle = keyBoard.nextLine();
		System.out.println("Trying to open " + fileTitle);
		clientServerImpl = DFSClient.getClientToServerImpl();
		int openFileForReadResult;
		try
		{
			openFileForReadResult = clientServerImpl.openFileForRead(fileTitle);
			System.out.println("Got file descriptor " + openFileForReadResult);
			DFSClient.setMenu(openForReadOptions, buildOpenForReadCommands(openFileForReadResult));
		} catch (DFSApp.ClientToServerPackage.FileNotFoundFailure e)
		{
			System.out.println("That file does not exist in the system");
		} catch (DFSApp.ClientToServerPackage.FileLockedForWriteFailure e) {
			System.out.println("This file is locked for write.");
		}
	}

}

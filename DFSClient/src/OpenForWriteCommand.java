import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import DFSApp.ClientToServer;
import DFSApp.ServerToServer;
import DFSApp.ClientToServerPackage.FileLockedForWriteFailure;

public class OpenForWriteCommand implements ClientCommand{

	
	private ClientToServer clientServerImpl;
	private ServerToServer serverServerImpl;
	private String[] openForWriteOptions =
	{ "Write", "Close the file" };
	private String[] openForWriteCommands =
	{ "WriteCommand", "CloseFile" };

	private ClientCommand[] buildOpenForWriteCommands(int fileID)
	{
		ClientCommand[] cmds = new ClientCommand[openForWriteOptions.length];
		int i = 0;
		for (String cmdClassName : openForWriteCommands)
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
	
	
	@Override
	public void execute() {
		
		Scanner keyBoard = DFSClient.getKeyboard();
		System.out.println("Please enter the name of the file you need");
		String fileTitle = keyBoard.nextLine();
		System.out.println("Trying to open " + fileTitle);
		clientServerImpl = DFSClient.getClientToServerImpl();
		
	
		int openFileForWriteResult;
		try
		{
			openFileForWriteResult = clientServerImpl.openFileForWrite(fileTitle);
			System.out.println("Got file descriptor " + openFileForWriteResult);
			DFSClient.setMenu(openForWriteOptions, buildOpenForWriteCommands(openFileForWriteResult));
			
			
		} catch (DFSApp.ClientToServerPackage.FileNotFoundFailure e)
		{
			System.out.println("Cannot open file for writing, it doesn't exist: line 53 OpenForWriteCommand");
			
		} catch (FileLockedForWriteFailure e) {
			System.out.println("File is locked for write; you cannot open it while it is locked.");
		}
		
	}

}

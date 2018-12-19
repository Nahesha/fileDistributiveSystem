import DFSApp.ClientToServer;

/**
 * This command closes a file because we no longer need access to it
 * 
 * @author merlin
 *
 */
public class CloseFile implements ClientCommand
{

	private DFSFileDescriptor fd;

	/**
	 * @param fd
	 *            the descriptor for the file we want to close
	 */
	public CloseFile(DFSFileDescriptor fd)
	{
		this.fd = fd;
	}

	/**
	 * @see ClientCommand#execute()
	 */
	@Override
	public void execute()
	{
		ClientToServer clientServerImpl = DFSClient.getClientToServerImpl();
		clientServerImpl.closeFile(fd.getFileID());
		DFSClient.setMenu(DFSClient.MAIN_LIST_OPTIONS, DFSClient.MAIN_LIST_COMMANDS);
	}

}

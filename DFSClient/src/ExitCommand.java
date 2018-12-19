
/**
 * Causes the client to exit
 * 
 * @author merlin
 *
 */
public class ExitCommand implements ClientCommand
{

	/**
	 * Close down our connection to the local server and kill us cleanly
	 * 
	 * @see ClientCommand#execute()
	 */
	@Override
	public void execute()
	{
		DFSClient.getClientToServerImpl().shutdown();
		System.exit(0);
	}

}

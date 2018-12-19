import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import org.omg.CORBA.ORB;

import DFSApp.ServerToServer;
import DFSApp.ServerToServerPOA;
import DFSApp.ServerToServerPackage.FileLockedForWriteFailure;
import DFSApp.ServerToServerPackage.FileNotFoundFailure;

/**
 * This is the code that handles requests from other servers
 * 
 * @author merlin
 *
 */
public class ServerToServerImpl extends ServerToServerPOA {

	private ORB orb;

	/**
	 * @param orb
	 *            the ORB object that servers can connect to
	 */
	public ServerToServerImpl(ORB orb) {
		this.orb = orb;
	}

	/**
	 * @see DFSApp.ServerToServerOperations#lockFileForWrite(java.lang.String)
	 */
	@Override
	public void lockFileForWrite(String fileTitle) 
	{
		FileManager.getSingleton().lockFileForWrite(fileTitle);
	}

	/**
	 * If we have the file and it can be open, the text in that file is returned
	 * 
	 * @throws DFSApp.ServerToServerPackage.FileNotFoundFailure
	 *             if the file is not on this server
	 * @throws DFSApp.ServerToServerPackage.FileLockedForWriteFailure
	 *             if the file is on this server, but locked for write
	 * @see DFSApp.ServerToServerOperations#readFile(java.lang.String)
	 */
	@Override
	public String readFile(String fileTitle) throws DFSApp.ServerToServerPackage.FileNotFoundFailure,
			DFSApp.ServerToServerPackage.FileLockedForWriteFailure {
		try {
			Scanner s = new Scanner(new File(fileTitle));
			StringBuffer contents = new StringBuffer("");
			while (s.hasNext()) {
				contents.append(s.nextLine() + "\n");
			}

			s.close();
			return contents.toString();
		} catch (FileNotFoundException e) {
			throw new FileNotFoundFailure("File not found on remote server");
		}
	}

	/**
	 * @see DFSApp.ServerToServerOperations#readFileAndLockForWrite(java.lang.String)
	 */
	@Override
	public String readFileAndLockForWrite(String fileTitle) throws FileNotFoundFailure, FileLockedForWriteFailure {
		
		System.out.println("Trying to open for write");
		
		return readFile(fileTitle);
	}

	/**
	 * @see DFSApp.ServerToServerOperations#shutdown()
	 */
	@Override
	public void shutdown() {
		// TODO this should look just like it looked in project 3

	}

	@Override
	public String echoYourHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
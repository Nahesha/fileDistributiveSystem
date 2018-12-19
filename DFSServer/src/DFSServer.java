
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import DFSApp.ClientToServer;
import DFSApp.ClientToServerHelper;
import DFSApp.ServerToServer;
import DFSApp.ServerToServerHelper;

/**
 * This is the class that runs on the server
 * 
 * @author merlin
 *
 */
public class DFSServer {
	public static HashMap<String, ServerToServer> hostMap;
	public static String localServer;
	
	/**
	 * @param args
	 *            ignored
	 */
	public static void main(String args[]) {

		System.out.println(args.length);
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);
			// get reference to rootpoa & activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));

			rootpoa.the_POAManager().activate();

			// create servant that will listen to the local client and register it with the
			// ORB
			ClientToServerImpl clientToServerImpl = new ClientToServerImpl(orb);

			startUpListener(orb, rootpoa, clientToServerImpl, "ClientToServer",
					ClientToServerHelper.narrow(rootpoa.servant_to_reference(clientToServerImpl)));
			ServerToServerImpl serverToServerImpl = new ServerToServerImpl(orb);
			startUpListener(orb, rootpoa, serverToServerImpl, "ServerToServer",
					ServerToServerHelper.narrow(rootpoa.servant_to_reference(serverToServerImpl)));
			// TODO this is where we should set up the link to the other servers using code
			// like DFSClient.setUpConnectionToLocalServer
			// wait for invocations from clients
			connectionToServers();
//			if (args.length == 6)
//			{
//				ServerToServer serverSender = startUpSender("lsaremotede.cs.ship.edu");
//				System.out.println("Got this back!:  " + serverSender.echoYourHostName());
//			}
			orb.run();
		}

		catch (Exception e)
		{
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}

		System.out.println("DFSServer Exiting ...");

	}

	private static ServerToServer startUpSender(String remoteHostName)
	{
		String[] args = new String[6];
		args[0] = "-ORBInitialHost";
		args[1] = remoteHostName;
		args[2] = "-ORBInitialPort";
		args[3] = "1054";
		args[4] = "-port";
		args[5] = "1053";
		try
		{
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// get the root naming context
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExt instead of NamingContext. This is
			// part of the Interoperable naming Service.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// resolve the Object Reference in Naming
			String name = "ServerToServer";
			return ServerToServerHelper.narrow(ncRef.resolve_str(name));

			// Use this to test that you have a connection
			// System.out.println(clientToServerImpl.sayHello());
		} catch (Exception e)
		{
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
			System.exit(-1);
		}
		return null;
	}
	
	private static String[] connectionToServers() {
		hostMap = new HashMap<String, ServerToServer>();
		// System.out.println(connectionObject);
		try {
			Scanner scan = new Scanner(new File("../config.txt"));
			localServer = scan.next();
			System.out.println(localServer);

			// Germany
			hostMap.put("Germany", startUpSender(scan.next()));
			// Spain
			hostMap.put("Spain", startUpSender(scan.next()));
			// US
			hostMap.put("US", startUpSender(scan.next()));

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}


	private static void startUpListener(ORB orb, POA rootpoa, org.omg.PortableServer.Servant servantImpl,
			String serviceName, org.omg.CORBA.Object href) throws ServantNotActive, WrongPolicy, InvalidName,
			org.omg.CosNaming.NamingContextPackage.InvalidName, NotFound, CannotProceed {
		// get object reference from the servant
		// org.omg.CORBA.Object ref = ;
		// ClientToServer href = ;

		// get the root naming context
		// NameService invokes the name service
		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
		// Use NamingContextExt which is part of the Interoperable
		// Naming Service (INS) specification.
		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

		// bind the Object Reference in Naming
		NameComponent path[] = ncRef.to_name(serviceName);
		ncRef.rebind(path, href);

		System.out.println(serviceName + " ready and waiting ...");
	}



}
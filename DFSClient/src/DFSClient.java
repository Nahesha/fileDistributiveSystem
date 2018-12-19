
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import DFSApp.ClientToServer;
import DFSApp.ClientToServerHelper;

/**
 * The framework for the DFS client
 * 
 * @author Merlin
 *
 */
public class DFSClient
{
	private static ClientToServer clientToServerImpl;

	private static Scanner keyboard;
	static final String[] MAIN_LIST_OPTIONS =
	{ "Open a file for read", "Open a file for write", "Exit" };

	private static String[] currentMenuOptions = MAIN_LIST_OPTIONS;
// Hey this needs to be in MAIN_LIST_COMANDS
	static ClientCommand[] MAIN_LIST_COMMANDS =
	{ new OpenForReadCommand(), new OpenForWriteCommand(), new ExitCommand() };
	private static ClientCommand[] currentMenuCommands = MAIN_LIST_COMMANDS;

	static ClientToServer getClientToServerImpl()
	{
		return clientToServerImpl;
	}

	/**
	 * @return the Scanner that is attached to the keyboard
	 */
	public static Scanner getKeyboard()
	{
		return keyboard;
	}

	private static int getMenuOptionFromUser(String[] optionList, Scanner keyboard)
	{
		int count = 0;
		for (String o : optionList)
		{
			System.out.println(count + 1 + ": " + o);
			count = count + 1;
		}
		int optionSelected = keyboard.nextInt() - 1;
		keyboard.nextLine();
		return optionSelected;
	}

	/**
	 * Just do each operation once
	 * 
	 * @param args
	 *            ignored
	 */
	public static void main(String args[])
	{
		setUpConnectionToLocalServer(args);
		keyboard = new Scanner(System.in);
		while (true)
		{
			System.out.println("Please pick an option");
			int choice = getMenuOptionFromUser(currentMenuOptions, keyboard);
			ClientCommand cmd = currentMenuCommands[choice];
			cmd.execute();
		}
	}

	/**
	 * Allows a change of menu state
	 * 
	 * @param options
	 *            a list of the prompts that should be given to the user
	 * @param commands
	 *            the commands that should be assocated with each prompt
	 */
	public static void setMenu(String[] options, ClientCommand[] commands)
	{
		currentMenuOptions = options;
		currentMenuCommands = commands;

	}

	private static void setUpConnectionToLocalServer(String[] args)
	{
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
			String name = "ClientToServer";
			clientToServerImpl = ClientToServerHelper.narrow(ncRef.resolve_str(name));

			// Use this to test that you have a connection
			// System.out.println(clientToServerImpl.sayHello());
		} catch (Exception e)
		{
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
			System.exit(-1);
		}
	}

}

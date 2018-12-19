import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import DFSApp.ServerToServer;
import DFSApp.ServerToServerPackage.FileLockedForWriteFailure;
import DFSApp.ServerToServerPackage.FileNotFoundFailure;

/**
 * Manages all of the files that are open on this server
 * 
 * @author merlin
 *
 */
public class FileManager {

	/*
	 * Enumeration to determine which action we would like to use for the file
	 * function.
	 */
	public enum FILE_ACTION {
		WRITE, READ
	};

	private int incrementor = 0;

	public final static boolean IS_READING_FILE = true;
	public final static boolean IS_WRITING_FILE = false;

	private class FileInformation {
		int fileID;
		String fileTitle;
		RandomAccessFile file;
		int numberOfTimesOpen;
		boolean fileLockedForWrite;
		boolean fileMarkedAsDirty;
		private boolean isOpenForRead;

		public FileInformation(String fileTitle, boolean read) throws DFSFileDoesntExist, FileLockedForWriteFailure {
			super();
			synchronized (singleton) {
				this.fileID = nextFileID;
				nextFileID++;
				this.fileTitle = fileTitle;
				this.numberOfTimesOpen = 1;

				try {
					// This is where functionality for file being locked should impact the file
					// being opened
					if (!fileLockedForWrite && !fileMarkedAsDirty) {
						file = new RandomAccessFile(new File(fileTitle), "r"); 
						if (!read) { 
							// Lock the file locally
							lockFile();
							// tell the other servers to lock it also
							notifyServersToLockFile(fileTitle, DFSServer.hostMap);
						}
					} else
						throw new FileLockedForWriteFailure("File is Locked");

				} catch (FileNotFoundException e) {
					try {
						file = checkOtherServersForFile(fileTitle, read);
					} catch (FileLockedForWriteFailure e1) {
						System.out.println("File locked for Write Failure.");
					} catch (FileNotFoundFailure e1) {
						throw new DFSFileDoesntExist("File was not found: " + fileTitle);
					}
				}
			}
		}

		void lockFile() {
			fileLockedForWrite = true;
		}

		void unLockFile() {
			fileLockedForWrite = false;
		}

		void markAsDirty() {
			fileMarkedAsDirty = true;
		}
		void incrementOpenCount() {
			numberOfTimesOpen++;
		}


		public void setOpenForRead(boolean isOpenForRead) {
			this.isOpenForRead = isOpenForRead;
		}

		public void markAsClean() {
			fileMarkedAsDirty = false;
			
		}
	}

	public static RandomAccessFile checkOtherServersForFile(String fileName, boolean read)
			throws FileLockedForWriteFailure, FileNotFoundFailure {
		RandomAccessFile file = null;
		System.out.println("Local server is " + DFSServer.localServer);

		// remove the local server from the host map
		DFSServer.hostMap.remove(DFSServer.localServer);

		// setup an arraylist that will hold the two remote server names
		ArrayList<String> remoteServerList = new ArrayList<String>();

		// iterate through the hostMap and get the server keys (names)
		Set<String> keySet = DFSServer.hostMap.keySet();
		for (String key : keySet) {
			remoteServerList.add(key);
		}

		try {

			String fileAsString;
			if (read)
				fileAsString = DFSServer.hostMap.get(remoteServerList.get(0)).readFile(fileName);
			else {
				fileAsString = DFSServer.hostMap.get(remoteServerList.get(0)).readFileAndLockForWrite(fileName);
				// tells the other servers to lock the file.
				notifyServersToLockFile(fileName, DFSServer.hostMap);
			}
			file = buildFile(fileAsString, fileName);
		} catch (Exception e) {
			String fileAsString;
			if (read)
				fileAsString = DFSServer.hostMap.get(remoteServerList.get(1)).readFile(fileName);
			else {
				fileAsString = DFSServer.hostMap.get(remoteServerList.get(1)).readFileAndLockForWrite(fileName);
				notifyServersToLockFile(fileName, DFSServer.hostMap);
			}
			file = buildFile(fileAsString, fileName);
		}
		return file;
	}

	/**
	 * Method to tell the two remote servers that we want to lock the file.
	 * 
	 * @param fileTitle
	 */
	public static void notifyServersToLockFile(String fileTitle, HashMap<String, ServerToServer> serversToNotify) {
		if (serversToNotify.containsKey(DFSServer.localServer)) {
			serversToNotify.remove(DFSServer.localServer);
			System.out.println("Removed local server from hashmap");
		}
		// iterate through the connections notifying all servers that we are going to
		// write the file
		Set<String> keys = serversToNotify.keySet();
		for (String key : keys) {
			System.out.println("Inside hashmap " + key);
			// tell the other two servers that we want to lock the file
			serversToNotify.get(key).lockFileForWrite(fileTitle);
		}
	}
	
	

	private static RandomAccessFile buildFile(String fileAsString, String fileName) {
		File file = new File(fileName);
		RandomAccessFile randomAccessFile;
		try {
			randomAccessFile = new RandomAccessFile(file, "rw");
			randomAccessFile.write(fileAsString.getBytes());
			System.out.println("File created on local disk: " + fileName);
			return randomAccessFile;
		} catch (IOException e) {
			System.out.println("Exception thrown in buildFile: Line 202");
		}
		return null;
	}

	private static FileManager singleton;

	private static String pathToPublicFiles = "../data/";
	private static String pathToSecretFiles = "../secret/";

	private static int nextFileID = 1;

	/**
	 * @return the only one of these that can exist
	 */
	public static FileManager getSingleton() {
		if (singleton == null) {
			singleton = new FileManager();
		}
		return singleton;
	}

	/**
	 * Used only for testing - throws away the current state of the singleton
	 */
	public static void resetSingleton() {
		singleton = null;
	}

	private HashMap<Integer, FileInformation> mapByFileID;

	private HashMap<String, FileInformation> mapByFileTitle;

	private HashMap<Integer, FileInformation> secretMapByFileID;

	private FileManager() {
		mapByFileID = new HashMap<Integer, FileInformation>();
		mapByFileTitle = new HashMap<String, FileInformation>();
		secretMapByFileID = new HashMap<Integer, FileInformation>();
	}

	private String fullPublicFileTitle(String fileTitle) {
		return pathToPublicFiles + fileTitle;
	}

	private String fullSecretFileTitle(String fileTitle) {
		return pathToSecretFiles + fileTitle;
	}

	/**
	 * Method used to lock a file
	 * 
	 * @param fileTitle
	 */
	public void lockFileForWrite(String fileTitle) {
		FileInformation fileInfo = mapByFileTitle.get(fullPublicFileTitle(fileTitle));
		if(fileInfo !=null)
		{
			fileInfo.lockFile();
		}
		
		if (mapByFileTitle.containsKey(fileTitle)) {
			System.out.println("Checking identity map...");
			// Check the map
			checkMapForFileAndDelete(fileTitle);
		} else {
			System.out.println("Checking local server...");
			// Check local disk
			deleteFileIfLocal(fileTitle);
		}
		
	}

	/**
	 * Checks the local server for the public file and whether it exists or not. If
	 * it does, then the file is deleted. Used in #lockFileForWrite.
	 * 
	 * @param fileTitle
	 */
	private void deleteFileIfLocal(String fileTitle) {
		File file = new File(fullPublicFileTitle(fileTitle));
		if (file.exists()) {
			System.out.println("Local file exists and is being deleted...");
			file.delete();
		}
	}

	private void checkMapForFileAndDelete(String fileTitle) {
		FileInformation fileInfo = mapByFileTitle.get(fileTitle);

		if (fileInfo.numberOfTimesOpen <= 1) {
			System.out.println("File not opened anywhere");
			mapByFileTitle.remove(fileTitle);
			File file = new File(fullPublicFileTitle(fileTitle));
			file.delete();
		} else {
			fileInfo.markAsDirty();
		}
	}

	/**
	 * @param fileID
	 *            the file we care about
	 * @return the number of clients that currently have it open
	 */
	public int getNumberOfTimesOpen(int fileID) {
		FileInformation fileInformation = mapByFileID.get(fileID);
		return fileInformation.numberOfTimesOpen;
	}

	/**
	 * @param fileTitle
	 *            the name of the file
	 * @return the number of clients that currently have it open
	 */
	public int getNumberOfTimesOpen(String fileTitle) {
		FileInformation fileInformation = mapByFileTitle.get(fullPublicFileTitle(fileTitle));
		return fileInformation.numberOfTimesOpen;
	}

	private void insert(FileInformation fileInfo) {
		synchronized (singleton) {
			mapByFileID.put(fileInfo.fileID, fileInfo);
			mapByFileTitle.put(fileInfo.fileTitle, fileInfo);
		}
	}

	/**
	 * Add the secret (read only file) to it's map
	 * 
	 * @param fileInfo
	 */
	private void insertIntoSecretMap(FileInformation fileInfo) {
		synchronized (singleton) {
			secretMapByFileID.put(fileInfo.fileID, fileInfo);
		}
	}

	/**
	 * Opens a file for read access and reads the file in either the publicFile map
	 * or the secretFile Map and returns the secretFile Map's file ID.
	 * 
	 * @param fileTitle
	 *            the name of the file
	 * @return a file identifier you can use to access the file
	 * @throws DFSFileDoesntExist
	 *             if we can't find a file with that name
	 * @throws FileLockedForWriteFailure
	 */
	public int openForRead(String fileTitle) throws DFSFileDoesntExist, FileLockedForWriteFailure {
		// creates the public file information
		FileInformation fileInfo;
		String publicFileTitle = fullPublicFileTitle(fileTitle);

		// create secret file information
		FileInformation secretInfo;
		String secretTitle = fullSecretFileTitle(fileTitle);

		if (mapByFileTitle.containsKey(fileTitle)) {
			fileInfo = mapByFileTitle.get(fileTitle);
			fileInfo.incrementOpenCount();
			System.out.println("I found it in the map");
		} else {
			fileInfo = new FileInformation(publicFileTitle, IS_READING_FILE);
			System.out.println("I created a new file information object");
			insert(fileInfo);
		}

		StringBuffer contents = readPublicFileContents(publicFileTitle);
		String secretFilePath = incrementingSecretFile(secretTitle);

		writesToSecretFile(contents, secretFilePath);
		secretInfo = secretFileInformation(secretFilePath);

		return secretInfo.fileID; // Returns the secretFile file id.
	}
	
	
	/**
	 * Writes the file to the secret directory.
	 */
	private void writesToSecretFile(StringBuffer contents, String secretFilePath) {
		try (Writer writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(secretFilePath), "utf-8"))) {
			writer.write(contents.toString());
		} catch (UnsupportedEncodingException e) {
			System.out.println("Incorrect format for file.");
		} catch (FileNotFoundException e) {
			System.out.println("File not found for secretTitle writer.");
		} catch (IOException e) {
			System.out.println("File Error in BufferedWriter.");
		}
	}

	private FileInformation secretFileInformation(String secretFilePath)
			throws DFSFileDoesntExist, FileLockedForWriteFailure {
		FileInformation secretInfo;
		secretInfo = new FileInformation(secretFilePath, IS_READING_FILE);
		secretInfo.markAsDirty();
		insertIntoSecretMap(secretInfo);
		return secretInfo;
	}
	/**
	 * Array to split the directory + file title so that we can add an incrementing
	 * number to the title.
	 */
	private String incrementingSecretFile(String secretTitle) {
		
		String[] array = secretTitle.split("\\.");
		String secretFilePath = ".." + array[2] + incrementor++ + "." + array[3];
		return secretFilePath;
	}
	
    /**
     * reads the content of public file into secret file
     * @param publicFileTitle
     * @return
     */
	private StringBuffer readPublicFileContents(String publicFileTitle) {
		Scanner s = null;
		try {
			s = new Scanner(new File(publicFileTitle));
		} catch (FileNotFoundException e1) {
			System.out.println("Cannot Scan New File, for openForRead's temp file.");
		}
		StringBuffer contents = new StringBuffer("");
		while (s.hasNext()) {
			contents.append(s.nextLine() + "\n");
		}
		s.close();
		return contents;
	}

	/**
	 * Read from a file that is open
	 * 
	 * @param fileID
	 *            the file
	 * @param filePointer
	 *            offset into the file we should start at
	 * @param chars
	 *            the number of characters we should read
	 * @return the data we read
	 */
	public String readFrom(int fileID, int filePointer, int chars) {
		RandomAccessFile file = secretMapByFileID.get(fileID).file;
		byte[] data = new byte[chars];
		try {
			file.seek(filePointer);
			file.read(data, 0, chars);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return new String(data);
	}

	/**
	 * Setter to specify a file path.
	 * 
	 * @param path
	 */
	void setPathToFiles(String path) {
		pathToPublicFiles = path;
	}

	/**
	 * Method to close the file, remove the fileID passed in from the specified
	 * maps, and marks them for OpenForRead.
	 * 
	 * @param fileID
	 */
	public void closeFile(int fileID) {
		FileInformation fileInfo = mapByFileID.get(fileID);
		FileInformation secretInfo = secretMapByFileID.get(fileID);

		/**
		 * Map for PublicFile
		 */
		if (fileInfo != null) {
			if (fileInfo.fileMarkedAsDirty) {
				fileInfo.setOpenForRead(false);
				deleteFile(fileID);
				System.out.println("File was deleted for mapByFileID.");
			}
			fileInfo.markAsClean();
		}

		/**
		 * Map for SecretFile
		 */
		if (secretInfo != null) {
			if (secretInfo.fileMarkedAsDirty) {
				secretInfo.setOpenForRead(false);
				deleteSecretFile(fileID);
				System.out.println("File was deleted for secretMapByFileID.");
			}
		}
	}

	/**
	 * 
	 * @param fileID
	 * @return
	 */
	public boolean contains(int fileID) {
		return mapByFileID.containsKey(fileID);
	}

	public int openForWrite(String fileTitle) throws DFSFileDoesntExist, FileLockedForWriteFailure {
		FileInformation fileInfo;
		fileTitle = fullPublicFileTitle(fileTitle);
		if (mapByFileTitle.containsKey(fileTitle)) {

			fileInfo = mapByFileTitle.get(fileTitle);
			if (fileInfo.fileLockedForWrite) {
				System.out.println("File locked for write");
				throw new FileLockedForWriteFailure("File is locked for write");
			}
			fileInfo.incrementOpenCount();
		} else {
			fileInfo = new FileInformation(fileTitle, IS_WRITING_FILE);

			insert(fileInfo);
		}

		return fileInfo.fileID;

	}

	public String writeTo(int fileID, int location, String contents) {

		RandomAccessFile file = mapByFileID.get(fileID).file;
		// marking the local file dirty
		// FileInformation OldFileInformation = mapByFileID.get(fileID);

		try {
			file.seek(0);
			String fileTitle = mapByFileID.get(fileID).fileTitle;

			RandomAccessFile newFile = new RandomAccessFile(new File(fileTitle), "rw");
			System.out.println("Input from command Line : " + contents);
			newFile.writeBytes(contents);
			newFile.close();
			mapByFileID.get(fileID).unLockFile();

		} catch (IOException e) {
			System.out.println("Caught Error in writeTo method FileManager");
			e.printStackTrace();
			System.exit(-1);
		}
		return new String("File wrote with your contents");
	}

	/**
	 * Removes the file's fileID from the hard drive and the hashmap for the data
	 * directory.
	 * 
	 * @param fileID
	 */
	private void deleteFile(int fileID) {
		FileInformation fileInformation = mapByFileID.get(fileID);
		File existingFile = new File(fileInformation.fileTitle);

		if (existingFile.exists()) {
			mapByFileID.remove(fileID);
			mapByFileTitle.remove(fileInformation.fileTitle);
			existingFile.delete();
		}
	}

	/**
	 * Removes the file's fileID from the hard drive and the hashmap for the secret
	 * directory.
	 * 
	 * @param fileID
	 */
	private void deleteSecretFile(int fileID) {
		FileInformation fileInformation = secretMapByFileID.get(fileID);
		File existingFile = new File(fileInformation.fileTitle);

		if (existingFile.exists()) {
			secretMapByFileID.remove(fileID);
			existingFile.delete();
		}
	}
}
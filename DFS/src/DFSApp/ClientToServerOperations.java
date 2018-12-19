package DFSApp;


/**
* DFSApp/ClientToServerOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from DFS.idl
* Thursday, December 6, 2018 at 3:37:39 PM Eastern Standard Time
*/

public interface ClientToServerOperations 
{
  String sayHello ();
  int openFileForRead (String fileTitle) throws DFSApp.ClientToServerPackage.FileNotFoundFailure, DFSApp.ClientToServerPackage.FileLockedForWriteFailure;
  String readFromFile (int fileID, int location, int length);
  int openFileForWrite (String fileTitle) throws DFSApp.ClientToServerPackage.FileNotFoundFailure, DFSApp.ClientToServerPackage.FileLockedForWriteFailure;
  String writeToFile (int fileID, int location, String contents);
  void closeFile (int fileID);
  void shutdown ();
} // interface ClientToServerOperations

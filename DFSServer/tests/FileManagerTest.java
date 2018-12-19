import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import DFSApp.ServerToServerPackage.FileLockedForWriteFailure;

/**
 * Test the FileManager class
 * 
 * @author merlin
 *
 */
public class FileManagerTest
{

	/**
	 * Open an existing file and make sure we can read from the beginning and from
	 * the middle
	 * 
	 * @throws DFSFileDoesntExist
	 *             shouldn't
	 * @throws FileLockedForWriteFailure 
	 */
	@Test
	public void canOpenAndReadFromLocalFile() throws DFSFileDoesntExist, FileLockedForWriteFailure
	{
		FileManager map = FileManager.getSingleton();
		int fileID = map.openForRead("test1.txt");
		assertEquals(1, FileManager.getSingleton().getNumberOfTimesOpen("test1.txt"));
		assertEquals("Tes", map.readFrom(fileID, 0, 3));
		assertEquals("File", map.readFrom(fileID, 5, 4));
	}

	/**
	 * If we reset the singleton and then get it, we should get a different object
	 */
	@Test
	public void canResetSingleton()
	{
		FileManager map = FileManager.getSingleton();
		FileManager.resetSingleton();
		FileManager map2 = FileManager.getSingleton();
		assertNotNull(map2);
		assertNotSame(map, map2);
	}

	/**
	 * If we open a file someone already has open, we should get the same file
	 * identifier and the number of times that file has been opened should be 2
	 * 
	 * @throws DFSFileDoesntExist
	 *             shouldn't
	 * @throws FileLockedForWriteFailure 
	 */
	@Test
	public void openingASecondGivesSameFileID() throws DFSFileDoesntExist, FileLockedForWriteFailure
	{
		FileManager map = FileManager.getSingleton();
		int fd = map.openForRead("test1.txt");
		int fd2 = map.openForRead("test1.txt");
		assertEquals(fd, fd2);
		assertEquals(2, FileManager.getSingleton().getNumberOfTimesOpen(fd));

	}

	/**
	 * initialize the singleton and give it the path where JUnit will find the data
	 * files. When the FileManager is used on the server, the path structure will be
	 * different, so the FileManager sets itself up for that. We have to change it
	 * to make the tests find the files
	 */
	@Before
	public void resetTheSingleton()
	{
		FileManager.resetSingleton();
		FileManager.getSingleton().setPathToFiles("data/");
	}

	/**
	 * Make sure the FileManager is a singleton
	 */
	@Test
	public void testIsSingleton()
	{
		FileManager map = FileManager.getSingleton();
		FileManager map2 = FileManager.getSingleton();
		assertNotNull(map);
		assertSame(map, map2);
	}

	/**
	 * If we try to open a file that can't be found, we should get an exception
	 * 
	 * @throws DFSFileDoesntExist
	 *             should
	 * @throws FileLockedForWriteFailure 
	 */
	@Test(expected = DFSFileDoesntExist.class)
	public void whenThereIsNoFile() throws DFSFileDoesntExist, FileLockedForWriteFailure
	{
		FileManager.getSingleton().openForRead("Missing File");
	}
	
	@Test
	public void testCloseDecrementsTheCount() throws DFSFileDoesntExist, FileLockedForWriteFailure
	{
		FileManager map = FileManager.getSingleton();
		int fileID = map.openForRead("test1.txt");
		int fileID2 = map.openForRead("test1.txt");
		map.closeFile(fileID);
		assertEquals(1, map.getNumberOfTimesOpen(fileID));
	}
	
	@Test
	public void testCloseAllRemoves() throws DFSFileDoesntExist, FileLockedForWriteFailure
	{
		FileManager map = FileManager.getSingleton();
		int fileID = map.openForRead("test1.txt");
		assertTrue(map.contains(fileID));
		int fileID2 = map.openForRead("test1.txt");
		map.closeFile(fileID);
		map.closeFile(fileID);
		assertFalse(map.contains(fileID));
	}
}

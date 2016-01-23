package org.tondo.testutils;


import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.BeforeClass;
import org.junit.Test;
import org.tondo.testutils.helpers.FileKeeper;

/**
 * Test for FileKeeper helper.
 * 
 * @author TondoDev
 *
 */
public class FileKeeperTest {

	private static Path resources;
	private static Path workFolder;
	
	@BeforeClass
	public static void initPaths() {
		String currentDir = System.getProperty("user.dir");
		resources = Paths.get(currentDir, "src", "test", "resources", "SampleFiles");
		workFolder = Paths.get(currentDir);
		assertTrue(true);
	}
	
	
	/**
	 * Test for copying resources from resource location and working folder
	 * @throws IOException 
	 */
	@Test
	public void testCopyResource() throws IOException {
		FileKeeper fk = createKeeper();
		Path source = Paths.get("textFile.txt");
		// just for sure if source file exists
		assertTrue(Files.exists(resources.resolve(source)));
		
		// copy file resource to work folder preventing file name
		Path copiedSameName = fk.copyResource(source);
		assertTrue(Files.exists(copiedSameName));
		// destination should be different
		assertNotEquals(source, copiedSameName);
		// but file names should be same
		assertEquals(source.getFileName(), copiedSameName.getFileName());
		// and location must be same as work folder
		assertEquals(workFolder, copiedSameName.getParent());
		// compare size
		Object sourceSize = Files.getAttribute(resources.resolve(source), "size");
		assertNotNull(sourceSize);
		Object targetSize = Files.getAttribute(resources.resolve(source), "size");
		assertNotNull(targetSize);
		assertEquals(sourceSize, targetSize);
		
		
		// copy resource with renaming
		Path target = Paths.get("temp.txt");
		Path copiedRenamed = fk.copyResource(source, target);
		assertNotNull(copiedRenamed);
		assertTrue(Files.exists(copiedRenamed));
		assertNotEquals(target, copiedRenamed);
		assertEquals(target, copiedRenamed.getFileName());
		assertEquals(workFolder, copiedRenamed.getParent());
		
		// from this moment all working files should be deleted
		fk.invalidate();
		assertFalse(fk.isValid());
		
		assertFalse(Files.exists(copiedSameName));
		assertFalse(Files.exists(copiedRenamed));
		// but source files should still exists :)
		assertTrue(Files.exists(resources.resolve(source)));
	}
	
	/**
	 * Test validity lifecycle of file keeper
	 * @throws IOException 
	 */
	@Test
	public void testInvalidation() throws IOException {
		FileKeeper fk = createKeeper();
		assertTrue(fk.isValid());
		
		fk.invalidate();
		assertFalse(fk.isValid());
		
		// any attempt calling method after invalidation (except of isValid())
		// will cause throwing exception
		
		try {
			fk.copyResource(Paths.get("textFile.txt"));
			fail("IllegalStateException exception expected!");
		} catch (IllegalStateException e) {}
	}
	
	
	/**
	 * Test of possibility to prevents created temporary files
	 * after invalidation
	 * @throws IOException 
	 */
	@Test
	public void testPreventingTempFiles() throws IOException {
		FileKeeper fk = createKeeper();
		assertTrue(fk.isValid());
		// by default, removing flag is set to true
		assertTrue(fk.isRemoveFilesWhenFinish());
		
		Path source =  Paths.get("textFile.txt");
		Path dest = Paths.get("copied.txt");
		Path copied = fk.copyResource(source, dest);
		assertNotNull(copied);
		assertTrue(Files.exists(copied));
		
		// set prevention of deleting filies
		fk.setRemoveFilesWhenFinish(false);
		assertFalse(fk.isRemoveFilesWhenFinish());
		
		fk.invalidate();
		
		// copied file still exists after invalidation
		assertTrue(Files.exists(copied));
	}
	
	/**
	 * Test for marking files created in other way than
	 * by FileKeeper to be deleted after invalidation
	 * @throws IOException 
	 */
	@Test
	public void testMarkingFilesForDeletion() throws IOException {
		Path someFile = workFolder.resolve(Paths.get("other.txt"));
		System.out.println(someFile);
		assertFalse(Files.exists(someFile));
		
		Path createdFile = Files.createFile(someFile);
		assertTrue(Files.exists(createdFile));
		assertEquals(someFile, createdFile);
		
		// marking file to be wathced by file keeper
		FileKeeper fk = createKeeper();
		fk.markForWatch(createdFile);
		
		fk.invalidate();
		assertFalse(fk.isValid());

		// file shoud not exists
		assertFalse(Files.exists(createdFile));
	}
	
	/**
	 * Deleting watches files and directories is recursive, so 
	 * it is sufficient to mark only top level directory and all its
	 * content is deleted too.
	 * @throws IOException 
	 */
	@Test
	public void testRecursiveDirectoryDelete() throws IOException {
		Path topLevel = Files.createDirectory(workFolder.resolve("topLevelDir"));
		assertTrue(Files.exists(topLevel));
		
		Path firstChild = Files.createFile(topLevel.resolve("firstChild.txt"));
		assertTrue(Files.exists(firstChild));
		
		Path nestedDir = Files.createDirectory(topLevel.resolve("nested"));
		assertTrue(Files.exists(nestedDir));
		
		Path grandChild = Files.createFile(nestedDir.resolve("grandchild.txt"));
		assertTrue(Files.exists(grandChild));
		
		// attemt to delete non empty directory cause a error
		try {
			Files.delete(topLevel);
			fail("DirectoryNotEmptyException expected!");
		} catch (DirectoryNotEmptyException e) {}
		
		
		FileKeeper fk = createKeeper();
		// marking for match only top level directory
		fk.markForWatch(topLevel);
		fk.invalidate();
		
		// ensure all files and directories are deleted
		assertFalse(Files.exists(topLevel));
		assertFalse(Files.exists(firstChild));
		assertFalse(Files.exists(nestedDir));
		assertFalse(Files.exists(grandChild));
	}
	
	
	
	private FileKeeper createKeeper() {
		return new FileKeeper(resources, workFolder);
	}
}

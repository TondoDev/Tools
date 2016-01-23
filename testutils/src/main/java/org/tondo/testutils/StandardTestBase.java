package org.tondo.testutils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.tondo.testutils.helpers.FileKeeper;

/**
 * 
 * @author TondoDev
 *
 */
public class StandardTestBase {

	private boolean initialized = false;
	private FileKeeper fileKeeper;
	
	private Path resourceLocation;
	private Path tempLocation;
	
	public FileKeeper getFileKeeper() {
		if (this.fileKeeper == null) {
			this.fileKeeper = new FileKeeper(getResourceLocation(), getTempLocation());
		}
		// if null then create instance
		return this.fileKeeper;
	}

	@Before
	public final void baseBefore() {
		if (!this.initialized) {
			this.init();
			this.initialized = true;
		}
	}
	
	@After
	public final void baseAfter() throws IOException {
		if (this.fileKeeper != null) {
			// clear current test resources
			this.fileKeeper.invalidate();
			// reset for next text case
			this.fileKeeper = null;
		}
	}
	
	public void setTempLocation(Path tempLocation) {
		this.tempLocation = tempLocation;
	}
	
	public void setResourceLocation(Path resourceLocation) {
		if (resourceLocation == null) {
			this.resourceLocation = null;
		} else if (resourceLocation.isAbsolute()) {
			this.resourceLocation = resourceLocation;
		} else {
			this.resourceLocation = standardMavenTestResourcesLocation().resolve(resourceLocation);
		}
	}
	
	public Path getTempLocation() {
		if (this.tempLocation == null) {
			this.tempLocation = Paths.get(System.getProperty("user.dir"));
		}
		return tempLocation;
	}
	
	public Path getResourceLocation() {
		if (this.resourceLocation == null) {
			this.resourceLocation = standardMavenTestResourcesLocation();
		}
		
		return resourceLocation;
	}
	
	private Path standardMavenTestResourcesLocation() {
		String currentDir = System.getProperty("user.dir");
		return Paths.get(currentDir, "src", "test", "resources");
	}
	
	/**
	 * Create path object (not real file) under temporary working directory.
	 * In other words resolve() provided path parts with path
	 * representing directory set as tempLocation.
	 * Actual file may not exists.
	 * 
	 * @param part
	 * 	first part to be assigned to temLocation directory
	 * @param parts
	 * 	additional parts to be assigned after first part
	 * @return
	 */
	protected Path inTempDir(String part, String... parts) {
		return getTempLocation().resolve(Paths.get(part, parts));
	}
	
	
	/**
	 * Create path object (not real file) under direcotry set as resourceLocation.
	 * In other words resolve() provided path parts with path
	 * representing directory set as resourceLocation.
	 * Actual file may not exists.
	 * 
	 * @param part
	 * 	first part to be assigned to resourceLocation directory
	 * @param parts
	 * 	additional parts to be assigned after first part
	 * @return
	 */
	protected Path inResourceDir(String part, String... parts) {
		return getResourceLocation().resolve(Paths.get(part, parts));
	}
	
	
	/**
	 * Method to override in inherited classes, when some global initialization
	 * is required in instance level. This is useful when some dependency injection
	 * framework is used and standard <code>@BeforeClass</code> JUnit initialization is 
	 * not possible
	 */
	protected void init() {
		// by default, nothing todo
	}
	
}

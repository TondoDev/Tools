package org.tondo.testutils.helpers;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Class suitable for tests where lot of files are created
 * or copied from template resources. File keeper provides 
 * ability to cleanup all created (or registered files) when is invalidated.
 * FileKeeper works with two special locations: resource base path and working directory path.
 * From resource base location are can be copied resources, and are stored in working directory.
 * Meaning of this, is to prevent example resources to be destroyed by tests, so the are copied
 * to working location, where can be modified, and after test, deleted. <br />
 * 
 * With this class is also possible mark any file which should be deleted after test completion. 
 * Instance of this class can be valid or invalid. Valid state is considered time after creation and 
 * before called <code>invalidate()</code> method. Any method calls (except of <code>isValid()</code>)
 * will throw <code>IllegalStateException</code>. Idea behind this is prevention sharing instance of this
 * class between tests.
 * 
 * @author TondoDev
 *
 */
public class FileKeeper {
	/** Validity flag */
	private boolean valid = true;
	
	/** Flag if marked files will be deleted after keeper invalidation */
	private boolean removeFilesWhenFinish;
	
	/** Warked files to watch */
	private Set<Path> watchedFiles;
	
	/** Path to resoruces base directory */
	private Path resourceBase;
	/** Path to working directory */
	private Path workingLocation;
	
	/**
	 * Constructor
	 * @param resourcesLocation
	 * 		location of template resources used for copy to working directory
	 * 		(in order to modify it), can't be null
	 * @param workingLocation
	 * 		working directory where resouces are copied.
	 */
	public FileKeeper(Path resourcesLocation, Path workingLocation) {
		
		if (resourcesLocation == null || workingLocation == null) {
			throw new NullPointerException("Path for resource location and temporary files location can't be null!");
		}
		
		this.valid = true;
		this.removeFilesWhenFinish = true;
		this.watchedFiles = new LinkedHashSet<>();
		this.resourceBase = resourcesLocation;
		this.workingLocation = workingLocation;
	}
	
	/**
	 * Invalidate this instance of FileKeeper. Invalidation causes deletion of watched files and directories.
	 * Any subsequent method call will raise exception (except of {@link #isValid()}).
	 * Calling this method on already invalidated instance has no effect.
	 * 
	 * @throws IOException
	 */
	public void invalidate() throws IOException {
		this.valid = false;
		
		if (this.removeFilesWhenFinish) {
			this.removeWatchedFiles();
		}
	}
	
	/** 
	 * Predicate if this FileKeeper instance is valid. 
	 */
	public boolean isValid() {
		return valid;
	}
	
	/**
	 * Internal check if FileKeeper instance is valid.
	 * If is not valid {@link IllegalStateException} is thrown.
	 * 
	 * Should be used inside every public API call.
	 */
	private void checkValidity() {
		if (!valid) {
			throw new IllegalStateException("File keeper is not valid any more!");
		}
	}
	
	/**
	 * Copy resource file to destination. Any of provided paths are relative, it is
	 * resolved to paths provided during FileKeeper instantiation.
	 * Target file path is marked for deletion when FileKeeper is invalidated.
	 * @param source
	 * 		source file from which is copied. If relative path is provided, it is resolved to
	 * 		resource base location path
	 * @param destination
	 * 		destination file which will be created. If relative path is provided. it is resolved to
	 * 		working directory path. If directories are present in this path, it must exists on target 
	 * 		filesystem, otherwise exception is thrown
	 * @return
	 * 		Path to created file
	 * @throws IOException
	 */
	public Path copyResource(Path source, Path destination) throws IOException {
		checkValidity();
		checkPathNotNull(source, "source");
		checkPathNotNull(destination, "destination");
		
		Path resolvedDest = destination;
		if (!resolvedDest.isAbsolute()) {
			resolvedDest = this.workingLocation.resolve(destination);
		}
		
		Path resolvedSource = source;
		if (!resolvedSource.isAbsolute()) {
			resolvedSource = this.resourceBase.resolve(source);
		}
		
		Path copied = Files.copy(resolvedSource, resolvedDest, StandardCopyOption.REPLACE_EXISTING);
		this.markForWatch(copied);
		return copied;
	}

	/**
	 * Copy resource from resource base location to working directory, 
	 * with preserving file name of resource in target.
	 * If has more than one element in path, only file name is extracted and
	 * resolved to working directory location.
	 * 
	 * @param resource
	 * 		path to resource to be copiend into working directory
	 * @return
	 * 		Path to created file
	 * @throws IOException
	 */
	public Path copyResource(Path resource) throws IOException {
		checkValidity();
		checkPathNotNull(resource, "resource");
		
		// TODO check when getFileName() can return null
		return this.copyResource(resource, workingLocation.resolve(resource.getFileName()));
	}
	
	/**
	 * Predicate if removing marked files is enabled durring invalidation
	 */
	public boolean isRemoveFilesWhenFinish() {
		checkValidity();
		return removeFilesWhenFinish;
	}
	
	/**
	 * Set if marked files will be deleted during invalidation.
	 * Probably sometimes will be required to examine files after FileKeeper invalidation.
	 * @param removeFilesWhenFinish
	 */
	public void setRemoveFilesWhenFinish(boolean removeFilesWhenFinish) {
		checkValidity();
		this.removeFilesWhenFinish = removeFilesWhenFinish;
	}
	
	/**
	 * Marks file represented by pathToFile argument to be
	 * watched by FileKeeper. Watched files can be deleted during
	 * invalidation process. This method doesn't check if file represented
	 * by pathToFile really exists.
	 * @param pathToFile
	 */
	public void markForWatch(Path pathToFile) {
		checkValidity();
		checkPathNotNull(pathToFile, "pathToFile");
		
		this.watchedFiles.add(pathToFile);
	}
	
	/**
	 * Removing marked files from filesystem.
	 * If file doesn't exist it is ignored and process continue with next file.
	 * If object to delete is directory, at first its content is deleted recursively.
	 * 
	 * @throws IOException
	 */
	private void removeWatchedFiles() throws IOException {
		// quick exit
		if (this.watchedFiles.isEmpty()) {
			return;
		}
		
		// deleting in reversed order
		Path[] arrayOfPaths = new Path[this.watchedFiles.size()];
		arrayOfPaths = this.watchedFiles.toArray(arrayOfPaths);
		for (int i = arrayOfPaths.length -1; i >=0; i--) {
			Path file = arrayOfPaths[i];
			if (!Files.exists(file)) {
				continue; 
			}
			
			try {
				Files.walkFileTree(file, new RecursiveDeleteVisitor());
			} catch (DirectoryNotEmptyException e) {
				System.err.println("Directory " + file + " is not empty!");
			}
		}

		arrayOfPaths = null;
		this.watchedFiles.clear();
	}
	
	/**
	 * Check if path is not null and throws NPE with
	 * corresponding message if is null.
	 * @param path
	 * 		path object to examine
	 * @param varName
	 * 		parameter name used in exception message
	 */
	private void checkPathNotNull(Path path, String varName) {
		if (path == null) {
			throw new NullPointerException("Path argument \""+varName+"\" can't be null!");
		}
	}
}

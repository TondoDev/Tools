package org.tondo.testutils.helpers;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Stateless directory visitor which at first delete directory content and then 
 * delete directory itself.
 * 
 * @author TondoDev
 *
 */
public class RecursiveDeleteVisitor extends SimpleFileVisitor<Path>{

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
			throws IOException {
		Files.deleteIfExists(file);
		return FileVisitResult.CONTINUE;
	}
	
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException {
		
		Files.deleteIfExists(dir);
		return FileVisitResult.CONTINUE;
	}
}

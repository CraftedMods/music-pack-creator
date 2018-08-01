package craftedMods.fileManager.provider;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;

import craftedMods.fileManager.api.FileManager;

@Component
public class FileManagerImpl implements FileManager {

	@Override
	public boolean createDir(Path path) throws IOException {
		Objects.requireNonNull(path);
		if (!Files.exists(path)) {
			Files.createDirectories(path);
			return true;
		}

		return false;
	}

	@Override
	public boolean createDir(String path, String... more) throws IOException {
		return this.createDir(Paths.get(path, more));
	}

	@Override
	public boolean createFile(String path, String... more) throws IOException {
		return this.createFile(Paths.get(path, more));
	}

	@Override
	public boolean createFile(Path path) throws IOException {
		Objects.requireNonNull(path);
		if (!Files.exists(path)) {
			if (path.getParent() != null)
				createDir(path.getParent());
			Files.createFile(path);
			return true;
		}

		return false;
	}

	@Override
	public Path getPathAndCreateFile(String path, String... more) throws IOException {
		Objects.requireNonNull(path);
		Path tmp = Paths.get(path, more);
		this.createFile(tmp);
		return tmp;
	}

	@Override
	public Path getPathAndCreateDir(String path, String... more) throws IOException {
		Objects.requireNonNull(path);
		Path tmp = Paths.get(path, more);
		this.createDir(tmp);
		return tmp;
	}

	@Override
	public void deleteDirAndContent(Path dir) throws IOException {
		Objects.requireNonNull(dir);
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult postVisitDirectory(Path paramT, IOException paramIOException) throws IOException {
				Files.delete(paramT);
				return super.postVisitDirectory(paramT, paramIOException);
			}

			@Override
			public FileVisitResult visitFile(Path paramT, BasicFileAttributes paramBasicFileAttributes)
					throws IOException {
				Files.delete(paramT);
				return super.visitFile(paramT, paramBasicFileAttributes);
			}
		});
	}

	@Override
	public String getSeparator() {
		return FileSystems.getDefault().getSeparator();
	}

	@Override
	public Stream<Path> getPathsInDirectory(Path directory) throws IOException {
		Objects.requireNonNull(directory);
		if (!this.isDirectory(directory))
			throw new NotDirectoryException(directory.toString());
		return Files.walk(directory, 1).skip(1);
	}

	@Override
	public boolean isDirectory(Path path) {
		return Files.isDirectory(path);
	}

	@Override
	public boolean isRegularFile(Path path) {
		return Files.isRegularFile(path);
	}

	@Override
	public boolean exists(Path path) {
		return Files.exists(path);
	}

	@Override
	public InputStream newInputStream(Path path) throws IOException {
		return Files.newInputStream(path);
	}

	@Override
	public OutputStream newOutputStream(Path path) throws IOException {
		return Files.newOutputStream(path);
	}

}

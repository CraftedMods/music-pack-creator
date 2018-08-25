package craftedMods.fileManager.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface FileManager {

	public boolean createDir(String path, String... more) throws IOException;

	public boolean createDir(Path path) throws IOException;

	public boolean createFile(String path, String... more) throws IOException;

	public boolean createFile(Path path) throws IOException;

	public Path getPathAndCreateFile(String path, String... more) throws IOException;

	public Path getPathAndCreateDir(String path, String... more) throws IOException;

	public boolean deleteDirAndContent(Path dir) throws IOException;

	public boolean deleteFile(Path file) throws IOException;

	public String getSeparator();

	public Stream<Path> getPathsInDirectory(Path directory) throws IOException;

	public boolean isDirectory(Path path);

	public boolean isRegularFile(Path path);

	public boolean exists(Path path);

	/**
	 * Returns a new input stream for the specified file. The stream supports the
	 * mark and reset operations.
	 * 
	 * @param path The path relevant file
	 * @return The input stream
	 * @throws IOException When IO-Errors occur
	 */
	public InputStream newInputStream(Path path) throws IOException;

	public OutputStream newOutputStream(Path path) throws IOException;

	public void write(Path path, byte[] data) throws IOException;

	public void copy(Path file1, Path file2) throws IOException;

	public byte[] read(Path path) throws IOException;

	public void rename(Path source, String newName) throws IOException;

}

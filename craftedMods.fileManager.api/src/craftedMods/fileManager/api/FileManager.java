package craftedMods.fileManager.api;

import java.io.*;
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

	public void deleteDirAndContent(Path dir) throws IOException;

	public String getSeparator();

	public Stream<Path> getPathsInDirectory(Path directory) throws IOException;

	public boolean isDirectory(Path path);

	public boolean isRegularFile(Path path);

	public boolean exists(Path path);

	public InputStream newInputStream(Path path) throws IOException;

	public OutputStream newOutputStream(Path path) throws IOException;

}

package craftedMods.fileManager.provider;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import craftedMods.fileManager.api.FileManager;

public class FileManagerImplTest {

	private FileManager fileManager;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setup() {
		this.fileManager = new FileManagerImpl();
	}

	@Test(expected = NullPointerException.class)
	public void testCreateDirNull() throws IOException {
		this.fileManager.createDir((Path) null);
	}

	@Test
	public void testCreateDirExisting() throws IOException {
		Path path = folder.getRoot().toPath().resolve("tmp");
		Files.createDirectory(path);
		Assert.assertFalse(this.fileManager.createDir(folder.newFolder().toPath()));
	}

	@Test
	public void testCreateDir() throws IOException {
		Path path = folder.getRoot().toPath().resolve("tmp");
		Assert.assertTrue(this.fileManager.createDir(path));
		Assert.assertTrue(Files.isDirectory(path));
	}

	@Test
	public void testCreateDirDeep() throws IOException {
		Path path = folder.getRoot().toPath().resolve("tmp").resolve("tmp2").resolve("tmp3");
		Assert.assertTrue(this.fileManager.createDir(path));
		Assert.assertTrue(Files.isDirectory(path));
	}

	@Test(expected = NullPointerException.class)
	public void testCreateDirStringNull() throws IOException {
		this.fileManager.createDir((String) null);
	}

	@Test(expected = NullPointerException.class)
	public void testCreateDirStringArrayNull() throws IOException {
		this.fileManager.createDir("Test", "Test", null);
	}

	@Test
	public void testCreateDirString() throws IOException {
		this.fileManager.createDir(folder.getRoot().toString(), "Test1", "Test2", "Test3");
		Assert.assertTrue(
				Files.isDirectory(folder.getRoot().toPath().resolve("Test1").resolve("Test2").resolve("Test3")));
	}

	@Test(expected = NullPointerException.class)
	public void testCreatFileNull() throws IOException {
		this.fileManager.createFile((Path) null);
	}

	@Test
	public void testCreateFileExisting() throws IOException {
		Path path = folder.getRoot().toPath().resolve("tmp");
		Files.createFile(path);
		Assert.assertFalse(this.fileManager.createFile(folder.newFile().toPath()));
	}

	@Test
	public void testCreateFile() throws IOException {
		Path path = folder.getRoot().toPath().resolve("tmp");
		Assert.assertTrue(this.fileManager.createFile(path));
		Assert.assertTrue(Files.isRegularFile(path));
	}

	@Test
	public void testCreateFileDeep() throws IOException {
		Path path = folder.getRoot().toPath().resolve("tmp").resolve("tmp2").resolve("tmp3").resolve("tmpPi");
		Assert.assertTrue(this.fileManager.createFile(path));
		Assert.assertTrue(Files.isRegularFile(path));
	}

	@Test(expected = NullPointerException.class)
	public void testCreateFileStringNull() throws IOException {
		this.fileManager.createFile((String) null);
	}

	@Test(expected = NullPointerException.class)
	public void testCreateFileStringArrayNull() throws IOException {
		this.fileManager.createFile("Test", "Test", null);
	}

	@Test
	public void testCreateFileString() throws IOException {
		this.fileManager.createFile(folder.getRoot().toString(), "Test1", "Test2", "Test3");
		Assert.assertTrue(
				Files.isRegularFile(folder.getRoot().toPath().resolve("Test1").resolve("Test2").resolve("Test3")));
	}

	@Test(expected = NullPointerException.class)
	public void testGetPathAndCreateDirNullString() throws IOException {
		this.fileManager.getPathAndCreateDir(null, "Test");
	}

	@Test(expected = NullPointerException.class)
	public void testGetPathAndCreateDirNullStringArray() throws IOException {
		this.fileManager.getPathAndCreateDir("Test", null, null, "Test2");
	}

	@Test
	public void testGetPathAndCreateDirWithoutMore() throws IOException {
		String pathString = folder.getRoot().toPath().resolve("Test2").toString();
		Path path = Paths.get(pathString);
		Assert.assertEquals(path, this.fileManager.getPathAndCreateDir(pathString));
		Assert.assertTrue(Files.isDirectory(path));
	}

	@Test
	public void testGetPathAndCreateDirWithMore() throws IOException {
		String pathString = folder.getRoot().toPath().resolve("Test2").toString();
		Path path = Paths.get(pathString, "Trst4,", "Test5");
		Assert.assertEquals(path, this.fileManager.getPathAndCreateDir(pathString, "Trst4,", "Test5"));
		Assert.assertTrue(Files.isDirectory(path));
	}

	@Test(expected = NullPointerException.class)
	public void testGetPathAndCreateFileNullString() throws IOException {
		this.fileManager.getPathAndCreateFile(null, "Test");
	}

	@Test(expected = NullPointerException.class)
	public void testGetPathAndCreateFileNullStringArray() throws IOException {
		this.fileManager.getPathAndCreateFile("Test", null, null, "Test2");
	}

	@Test
	public void testGetPathAndCreateFileWithoutMore() throws IOException {
		String pathString = folder.getRoot().toPath().resolve("Test2").toString();
		Path path = Paths.get(pathString);
		Assert.assertEquals(path, this.fileManager.getPathAndCreateFile(pathString));
		Assert.assertTrue(Files.isRegularFile(path));
	}

	@Test
	public void testGetPathAndCreateFileWithMore() throws IOException {
		String pathString = folder.getRoot().toPath().resolve("Test2").toString();
		Path path = Paths.get(pathString, "Trst4,", "Test5");
		Assert.assertEquals(path, this.fileManager.getPathAndCreateFile(pathString, "Trst4,", "Test5"));
		Assert.assertTrue(Files.isRegularFile(path));
	}

	@Test
	public void testGetSeparator() {
		Assert.assertEquals(FileSystems.getDefault().getSeparator(), fileManager.getSeparator());
	}

	@Test(expected = NullPointerException.class)
	public void testDeleteDirAndContentNullPath() throws IOException {
		fileManager.deleteDirAndContent(null);
	}

	@Test
	public void testDeleteDirAndContent() throws IOException {
		Path path = folder.getRoot().toPath().resolve("tmp").resolve("tmp2").resolve("tmp3");
		this.fileManager.createDir(path);
		this.fileManager.createFile(path.resolveSibling("test1"));
		this.fileManager.createFile(path.resolveSibling("test2"));
		this.fileManager.createFile(path.resolveSibling("test3"));
		this.fileManager.createFile(path.resolveSibling("test4"));
		fileManager.deleteDirAndContent(path);
		Assert.assertFalse(Files.exists(path));
	}

	@Test
	public void testDeleteFile() throws IOException {
		Path file = this.fileManager.getPathAndCreateFile(folder.getRoot().toPath().toString(), "file.tmp");
		Assert.assertTrue(this.fileManager.deleteFile(file));
	}

	@Test(expected = NullPointerException.class)
	public void testDeleteFileNull() throws IOException {
		this.fileManager.deleteFile(null);
	}

	@Test(expected = IOException.class)
	public void testDeleteFileDirectory() throws IOException {
		this.fileManager.deleteFile(folder.getRoot().toPath());
	}

	@Test
	public void testDeleteFileNotExisting() throws IOException {
		Path file = folder.getRoot().toPath().resolve("file.tmp");
		Assert.assertFalse(this.fileManager.deleteFile(file));
	}

	@Test(expected = NullPointerException.class)
	public void testGetPathsInDirectoryNullDirectory() throws IOException {
		fileManager.getPathsInDirectory(null);
	}

	@Test(expected = NotDirectoryException.class)
	public void testGetPathsInDirectoryNoDirectory() throws IOException {
		Path file = folder.getRoot().toPath().resolve("file.txt");
		this.fileManager.createFile(file);
		fileManager.getPathsInDirectory(file);
	}

	@Test
	public void testGetPathsInDirectory() throws IOException {
		for (int i = 0; i < 4; i++) {
			this.fileManager.createFile(folder.getRoot().toPath().toString(), "file_" + i);
		}
		Assert.assertEquals(4l, fileManager.getPathsInDirectory((folder.getRoot().toPath())).count());
	}

}

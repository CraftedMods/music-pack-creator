package craftedMods.fileManager.provider;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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
	public void testDeleteDirAndContentNonExisting() throws IOException {
		Assert.assertFalse(fileManager.deleteDirAndContent(folder.getRoot().toPath().resolve("tmp")));
	}

	@Test
	public void testDeleteDirAndContentNoDir() throws IOException {
		Path path = folder.getRoot().toPath().resolve("file.ogg");
		this.fileManager.createFile(path);
		Assert.assertFalse(fileManager.deleteDirAndContent(path));
	}

	@Test
	public void testDeleteDirAndContent() throws IOException {
		Path path = folder.getRoot().toPath().resolve("tmp").resolve("tmp2").resolve("tmp3");
		this.fileManager.createDir(path);
		this.fileManager.createFile(path.resolveSibling("test1"));
		this.fileManager.createFile(path.resolveSibling("test2"));
		this.fileManager.createFile(path.resolveSibling("test3"));
		this.fileManager.createFile(path.resolveSibling("test4"));
		Assert.assertTrue(fileManager.deleteDirAndContent(path));
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

	@Test
	public void testWriteNonExisting() throws IOException {
		Path file = folder.getRoot().toPath().resolve("file.d");

		this.fileManager.write(file, new String("TEST").getBytes());

		Assert.assertTrue(fileManager.exists(file));
		Assert.assertEquals("TEST", new String(Files.readAllBytes(file)));
	}

	@Test
	public void testWriteExisting() throws IOException {
		Path file = folder.getRoot().toPath().resolve("file.d");

		this.fileManager.createFile(file);

		this.fileManager.write(file, new String("TEST").getBytes());

		Assert.assertEquals("TEST", new String(Files.readAllBytes(file)));
	}

	@Test(expected = IOException.class)
	public void testWriteDirectory() throws IOException {
		Path file = folder.getRoot().toPath().resolve("file");

		this.fileManager.createDir(file);

		this.fileManager.write(file, new String("TEST").getBytes());
	}

	@Test(expected = NullPointerException.class)
	public void testWriteNullFile() throws IOException {
		this.fileManager.write(null, new String("TEST").getBytes());
	}

	@Test(expected = NullPointerException.class)
	public void testWriteNullBytes() throws IOException {
		Path file = folder.getRoot().toPath().resolve("file");

		this.fileManager.createFile(file);

		this.fileManager.write(file, null);
	}

	@Test(expected = NullPointerException.class)
	public void testCopyFirstNull() throws IOException {
		this.fileManager.copy(null, Paths.get("s"));
	}

	@Test(expected = NullPointerException.class)
	public void testCopySecondNull() throws IOException {
		this.fileManager.copy(Paths.get("s"), null);
	}

	@Test
	public void testCopy() throws IOException {
		Path file1 = folder.getRoot().toPath().resolve("file1");
		Path file2 = folder.getRoot().toPath().resolve("file2");

		this.fileManager.createFile(file1);
		this.fileManager.createFile(file2);

		this.fileManager.write(file1, new String("S").getBytes());

		this.fileManager.copy(file1, file2);

		Assert.assertEquals("S", new String(Files.readAllBytes(file2)));
	}

	@Test(expected = NoSuchFileException.class)
	public void testCopySourceNotExisting() throws IOException {
		Path file1 = folder.getRoot().toPath().resolve("file1");
		Path file2 = folder.getRoot().toPath().resolve("file2");

		this.fileManager.createFile(file2);

		this.fileManager.copy(file1, file2);
	}

	@Test
	public void testCopyTargetNotExisting() throws IOException {
		Path file1 = folder.getRoot().toPath().resolve("file1");
		Path file2 = folder.getRoot().toPath().resolve("file2");

		this.fileManager.createFile(file1);

		this.fileManager.write(file1, new String("S").getBytes());

		this.fileManager.copy(file1, file2);

		Assert.assertEquals("S", new String(Files.readAllBytes(file2)));
	}

	@Test(expected = IOException.class)
	public void testCopySourceDir() throws IOException {
		Path file1 = folder.getRoot().toPath().resolve("file1");
		Path file2 = folder.getRoot().toPath().resolve("file2");

		this.fileManager.createDir(file1);
		this.fileManager.createFile(file2);

		this.fileManager.copy(file1, file2);
	}

	@Test(expected = IOException.class)
	public void testCopyTargetDir() throws IOException {
		Path file1 = folder.getRoot().toPath().resolve("file1");
		Path file2 = folder.getRoot().toPath().resolve("file2");

		this.fileManager.createFile(file1);
		this.fileManager.createDir(file2);

		this.fileManager.write(file1, new String("S").getBytes());

		this.fileManager.copy(file1, file2);
	}

	@Test(expected = NoSuchFileException.class)
	public void testReadNonExisting() throws IOException {
		Path file = folder.getRoot().toPath().resolve("file.d");

		this.fileManager.read(file);
	}

	@Test
	public void testReadExisting() throws IOException {
		Path file = folder.getRoot().toPath().resolve("file.d");

		String content = "TEST";

		this.fileManager.createFile(file);

		this.fileManager.write(file, content.getBytes());

		Assert.assertArrayEquals(content.getBytes(), this.fileManager.read(file));
	}

	@Test(expected = IOException.class)
	public void testReadDirectory() throws IOException {
		Path file = folder.getRoot().toPath().resolve("file");

		this.fileManager.createDir(file);

		this.fileManager.read(file);
	}

	@Test(expected = NullPointerException.class)
	public void testReadNullFile() throws IOException {
		this.fileManager.read(null);
	}

	@Test(expected = NullPointerException.class)
	public void testRenameNullFile() throws IOException {
		this.fileManager.rename(null, "");
	}

	@Test(expected = NullPointerException.class)
	public void testRenameNullNewName() throws IOException {
		this.fileManager.rename(Paths.get("s"), null);
	}

	@Test(expected = NoSuchFileException.class)
	public void testRenameNonExistingFile() throws IOException {
		this.fileManager.rename(Paths.get("sertf"), "sd");
	}

	@Test
	public void testRenameEqualFileName() throws IOException {
		Path file = this.folder.getRoot().toPath().resolve("file.f");

		this.fileManager.createFile(file);

		this.fileManager.rename(file, "file.f");

		Assert.assertTrue(this.fileManager.exists(file));
	}

	@Test
	public void testRenameEqualDirName() throws IOException {
		Path file = this.folder.getRoot().toPath().resolve("dir");

		this.fileManager.createDir(file);

		this.fileManager.rename(file, "dir");

		Assert.assertTrue(this.fileManager.exists(file));
	}

	@Test
	public void testRenameNewFileName() throws IOException {
		Path file = this.folder.getRoot().toPath().resolve("file.f");
		Path newFile = file.resolveSibling("file.f2");

		this.fileManager.createFile(file);

		this.fileManager.rename(file, "file.f2");

		Assert.assertTrue(this.fileManager.exists(newFile));
		Assert.assertFalse(this.fileManager.exists(file));
		Assert.assertTrue(this.fileManager.isRegularFile(newFile));
	}

	@Test
	public void testRenameNewDirName() throws IOException {
		Path file = this.folder.getRoot().toPath().resolve("diro");
		Path newFile = file.resolveSibling("dirouz");

		this.fileManager.createDir(file);
		this.fileManager.createDir(newFile);

		this.fileManager.rename(file, "dirouz");

		Assert.assertTrue(this.fileManager.exists(newFile));
		Assert.assertFalse(this.fileManager.exists(file));
		Assert.assertTrue(this.fileManager.isDirectory(newFile));
	}

	@Test
	public void testRenameNewFileNameExisting() throws IOException {
		Path file = this.folder.getRoot().toPath().resolve("file.f");
		Path newFile = file.resolveSibling("file.f2");

		this.fileManager.createFile(file);
		this.fileManager.createFile(newFile);

		this.fileManager.rename(file, "file.f2");

		Assert.assertTrue(this.fileManager.exists(newFile));
		Assert.assertFalse(this.fileManager.exists(file));
		Assert.assertTrue(this.fileManager.isRegularFile(newFile));
	}

	@Test
	public void testRenameNewDirNameExisting() throws IOException {
		Path file = this.folder.getRoot().toPath().resolve("dre");
		Path newFile = file.resolveSibling("bgt");

		this.fileManager.createDir(file);
		this.fileManager.createDir(newFile);

		this.fileManager.rename(file, "bgt");

		Assert.assertTrue(this.fileManager.exists(newFile));
		Assert.assertFalse(this.fileManager.exists(file));
		Assert.assertTrue(this.fileManager.isDirectory(newFile));
	}

	@Test
	public void testNewInputStreamSupportsMark() throws IOException {
		Path path = this.fileManager.getPathAndCreateFile(this.folder.getRoot().toString(), "file.fil");
		try (InputStream in = this.fileManager.newInputStream(path)) {
			Assert.assertTrue(in.markSupported());
		}
	}

}
package craftedMods.lotr.mpc.persistence.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceException;
import org.osgi.service.log.LogService;

import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.compatibility.api.MusicPackProjectCompatibilityManager;
import craftedMods.lotr.mpc.core.api.MusicPackCreator;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectPersistenceManager;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectReader;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectWriter;
import craftedMods.utils.data.PrimitiveProperties;

@RunWith(EasyMockRunner.class)
public class MusicPackProjectPersistenceManagerImplTest extends EasyMockSupport {

	@TestSubject
	public MusicPackProjectPersistenceManagerImpl persistenceManager = new MusicPackProjectPersistenceManagerImpl();

	@Mock(type = MockType.NICE)
	private MusicPackCreator mockMusicPackCreator;

	@Mock
	private MusicPackProjectReader mockMusicPackProjectReader;

	@Mock
	private MusicPackProjectWriter mockMusicPackProjectWriter;

	@Mock(type = MockType.NICE)
	private LogService mockLogService;

	@Mock
	private FileManager mockFileManager;

	@Mock
	private EventManager mockEventManager;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Mock(type = MockType.STRICT)
	private MusicPackProjectCompatibilityManager mockCompatibilityManager;

	private Path workspaceRoot;
	private Path projectsDir;

	@Before
	public void setup() {
		workspaceRoot = folder.getRoot().toPath();
		projectsDir = workspaceRoot.resolve(MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME);

		EasyMock.expect(mockMusicPackCreator.getWorkspaceRoot()).andStubReturn(workspaceRoot);
		EasyMock.expect(mockMusicPackCreator.getVersion()).andStubReturn("0.1.0");
	}

	@Test
	public void testOnActivate() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andReturn(projectsDir);

		this.replayAll();

		persistenceManager.onActivate();

		Assert.assertNotNull(persistenceManager.getManagedMusicPackProjects());
		Assert.assertTrue(persistenceManager.getManagedMusicPackProjects().isEmpty());

		this.verifyAll();
	}

	@Test
	public void testLoadMusicPackProjects() throws IOException {
		Path projectPath1 = projectsDir.resolve("project1");

		Path projectFilePath1 = projectPath1.resolve(MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME);

		InputStream projectStream1 = EasyMock.createMock(InputStream.class);

		MusicPackProject mockMusicPackProject1 = createMockMusicPackProject("0.1.0");

		EasyMock.expect(mockFileManager.getPathAndCreateDir(this.workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andStubReturn(projectsDir);
		EasyMock.expect(mockFileManager.getPathsInDirectory(projectsDir))
				.andReturn(Arrays.stream(new Path[] { projectPath1 })).once();
		EasyMock.expect(mockFileManager.isDirectory(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.exists(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.newInputStream(projectFilePath1)).andReturn(projectStream1).once();

		EasyMock.expect(mockMusicPackProjectReader.readMusicPackProject(projectStream1))
				.andReturn(mockMusicPackProject1).once();

		mockCompatibilityManager.applyPreLoadFixes(projectPath1);
		EasyMock.expectLastCall().once();

		mockCompatibilityManager.applyPostLoadFixes(mockMusicPackProject1, "0.1.0");
		EasyMock.expectLastCall().once();

		this.replayAll();
		EasyMock.replay(mockMusicPackProject1);

		persistenceManager.onActivate();
		Collection<MusicPackProject> projects = persistenceManager.loadMusicPackProjects();

		Assert.assertEquals(1, projects.size());

		MusicPackProject project = projects.iterator().next();

		Assert.assertTrue(project == mockMusicPackProject1);

		Assert.assertTrue(persistenceManager.getManagedMusicPackProjects().containsKey(project));
		Assert.assertEquals(projectPath1, persistenceManager.getManagedMusicPackProjects().get(project));

		this.verifyAll();
	}

	@Test
	public void testLoadNoMusicPackProjects() throws IOException {
		EasyMock.expect(mockFileManager.getPathsInDirectory(projectsDir)).andReturn(Arrays.stream(new Path[] {}))
				.once();
		EasyMock.expect(mockFileManager.isDirectory(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.getPathAndCreateDir(this.workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andStubReturn(projectsDir);

		this.replayAll();

		persistenceManager.onActivate();
		Collection<MusicPackProject> projects = persistenceManager.loadMusicPackProjects();

		Assert.assertEquals(0, projects.size());
		Assert.assertTrue(persistenceManager.getManagedMusicPackProjects().isEmpty());

		this.verifyAll();
	}

	@Test(expected = ServiceException.class)
	public void testLoadMusicPackProjectFatalLoadingError() throws IOException {
		EasyMock.expect(mockFileManager.getPathsInDirectory(projectsDir))
				.andStubThrow(new IOException("Could not get the files in the specified directory"));
		EasyMock.expect(mockFileManager.getPathAndCreateDir(this.workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andStubReturn(projectsDir);

		this.replayAll();

		persistenceManager.onActivate();
		persistenceManager.loadMusicPackProjects();
	}

	@Test
	public void testLoadMusicPackProjectProjectSpecificLoadingError() throws IOException {
		Path projectPath1 = projectsDir.resolve("project1");

		Path projectFilePath1 = projectPath1.resolve(MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME);

		InputStream projectStream1 = EasyMock.createMock(InputStream.class);

		EasyMock.expect(mockFileManager.getPathAndCreateDir(this.workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andStubReturn(projectsDir);
		EasyMock.expect(mockFileManager.getPathsInDirectory(projectsDir))
				.andStubReturn(Arrays.stream(new Path[] { projectPath1 }));
		EasyMock.expect(mockFileManager.isDirectory(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.exists(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.newInputStream(projectFilePath1)).andStubReturn(projectStream1);

		Exception thrownException = new RuntimeException("Couldn't apply compatibility fixes");

		mockCompatibilityManager.applyPreLoadFixes(projectPath1);
		EasyMock.expectLastCall().andThrow(thrownException).once();

		Capture<WriteableEventProperties> propertiesCapture = EasyMock.newCapture();

		EasyMock.expect(mockEventManager.dispatchEvent(
				EasyMock.eq(MusicPackProjectPersistenceManager.LOAD_ALL_PROJECT_ERROR_EVENT),
				EasyMock.capture(propertiesCapture))).andReturn(null).once();

		this.replayAll();

		persistenceManager.onActivate();
		Collection<MusicPackProject> projects = persistenceManager.loadMusicPackProjects();

		Assert.assertEquals(0, projects.size());
		Assert.assertTrue(persistenceManager.getManagedMusicPackProjects().isEmpty());

		WriteableEventProperties value = propertiesCapture.getValue();

		Assert.assertEquals(thrownException,
				value.getProperty(MusicPackProjectPersistenceManager.LOAD_ALL_PROJECT_ERROR_EVENT_EXCEPTION));

		this.verifyAll();
	}

	@Test
	public void testLoadMusicPackProjectNoProjectFile() throws IOException {
		Path projectPath1 = projectsDir.resolve("project1");

		MusicPackProject mockMusicPackProject1 = createMockMusicPackProject("0.1.0");

		EasyMock.expect(mockFileManager.getPathAndCreateDir(this.workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andStubReturn(projectsDir);
		EasyMock.expect(mockFileManager.getPathsInDirectory(projectsDir))
				.andStubReturn(Arrays.stream(new Path[] { projectPath1 }));
		EasyMock.expect(mockFileManager.isDirectory(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.exists(EasyMock.anyObject())).andStubReturn(false);

		mockCompatibilityManager.applyPreLoadFixes(projectPath1);
		EasyMock.expectLastCall().once();

		this.replayAll();
		EasyMock.replay(mockMusicPackProject1);

		persistenceManager.onActivate();
		Collection<MusicPackProject> projects = persistenceManager.loadMusicPackProjects();

		Assert.assertEquals(0, projects.size());
		Assert.assertTrue(persistenceManager.getManagedMusicPackProjects().isEmpty());

		this.verifyAll();
	}

	@Test
	public void testLoadMusicPackProjectNewerVersion() throws IOException {
		Path projectPath1 = projectsDir.resolve("project1");

		Path projectFilePath1 = projectPath1.resolve(MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME);

		InputStream projectStream1 = EasyMock.createMock(InputStream.class);

		MusicPackProject mockMusicPackProject1 = createMockMusicPackProject("0.2.0");

		EasyMock.expect(mockFileManager.getPathAndCreateDir(this.workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andStubReturn(projectsDir);
		EasyMock.expect(mockFileManager.getPathsInDirectory(projectsDir))
				.andStubReturn(Arrays.stream(new Path[] { projectPath1 }));
		EasyMock.expect(mockFileManager.isDirectory(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.exists(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.newInputStream(projectFilePath1)).andStubReturn(projectStream1);

		EasyMock.expect(mockMusicPackProjectReader.readMusicPackProject(projectStream1))
				.andStubReturn(mockMusicPackProject1);

		mockCompatibilityManager.applyPreLoadFixes(projectPath1);
		EasyMock.expectLastCall().once();

		Capture<WriteableEventProperties> propertiesCapture = EasyMock.newCapture();

		EasyMock.expect(
				mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectPersistenceManager.NEWER_SAVE_VERSION_EVENT),
						EasyMock.capture(propertiesCapture)))
				.andReturn(null).once();

		mockCompatibilityManager.applyPostLoadFixes(mockMusicPackProject1, "0.2.0");
		EasyMock.expectLastCall().once();

		this.replayAll();
		EasyMock.replay(mockMusicPackProject1);

		persistenceManager.onActivate();
		persistenceManager.loadMusicPackProjects();

		WriteableEventProperties value = propertiesCapture.getValue();

		Assert.assertEquals("0.2.0",
				value.getProperty(MusicPackProjectPersistenceManager.NEWER_SAVE_VERSION_EVENT_DETECTED_VERSION));
		Assert.assertTrue(mockMusicPackProject1 == value
				.getProperty(MusicPackProjectPersistenceManager.NEWER_SAVE_VERSION_EVENT_MUSIC_PACK_PROJECT));

		this.verifyAll();
	}

	@Test
	public void testLoadMusicPackProjectOlderVersion() throws IOException {
		Path projectPath1 = projectsDir.resolve("project1");

		Path projectFilePath1 = projectPath1.resolve(MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME);

		InputStream projectStream1 = EasyMock.createMock(InputStream.class);

		MusicPackProject mockMusicPackProject1 = createMockMusicPackProject("0.0.1");

		EasyMock.expect(mockFileManager.getPathAndCreateDir(this.workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andStubReturn(projectsDir);
		EasyMock.expect(mockFileManager.getPathsInDirectory(projectsDir))
				.andStubReturn(Arrays.stream(new Path[] { projectPath1 }));
		EasyMock.expect(mockFileManager.isDirectory(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.exists(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.newInputStream(projectFilePath1)).andStubReturn(projectStream1);

		EasyMock.expect(mockMusicPackProjectReader.readMusicPackProject(projectStream1))
				.andStubReturn(mockMusicPackProject1);

		mockCompatibilityManager.applyPreLoadFixes(projectPath1);
		EasyMock.expectLastCall().once();

		Capture<WriteableEventProperties> propertiesCapture = EasyMock.newCapture();

		EasyMock.expect(
				mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectPersistenceManager.OLDER_SAVE_VERSION_EVENT),
						EasyMock.capture(propertiesCapture)))
				.andReturn(null).once();

		mockCompatibilityManager.applyPostLoadFixes(mockMusicPackProject1, "0.0.1");
		EasyMock.expectLastCall().once();

		this.replayAll();
		EasyMock.replay(mockMusicPackProject1);

		persistenceManager.onActivate();
		persistenceManager.loadMusicPackProjects();

		WriteableEventProperties value = propertiesCapture.getValue();

		Assert.assertEquals("0.0.1",
				value.getProperty(MusicPackProjectPersistenceManager.OLDER_SAVE_VERSION_EVENT_DETECTED_VERSION));
		Assert.assertTrue(mockMusicPackProject1 == value
				.getProperty(MusicPackProjectPersistenceManager.OLDER_SAVE_VERSION_EVENT_MUSIC_PACK_PROJECT));

		this.verifyAll();
	}

	@Test
	public void testSaveNonExistingMusicPackProject() throws IOException {
		Path projectPath = projectsDir.resolve("proj_3");

		EasyMock.expect(mockFileManager.exists(EasyMock.anyObject())).andReturn(true).times(3).andReturn(false).once();
		EasyMock.expect(mockFileManager.createDir(projectPath)).andReturn(true).once();

		MusicPackProject project = createMockMusicPackProject("0.1.0");

		saveExistingMusicPackProject(project, projectPath, false, false);

		Assert.assertTrue(persistenceManager.getManagedMusicPackProjects().containsKey(project));
		Assert.assertEquals(projectPath, persistenceManager.getManagedMusicPackProjects().get(project));
	}

	@Test(expected = ServiceException.class)
	public void testSaveNonExistingMusicPackProjectIOException() throws IOException {
		EasyMock.expect(mockFileManager.exists(EasyMock.anyObject())).andReturn(true).times(3).andReturn(false).once();
		EasyMock.expect(mockFileManager.createDir(EasyMock.anyObject()))
				.andStubThrow(new IOException("Couldn't generate the project dir name"));
		saveExistingMusicPackProject(createMockMusicPackProject("0.1.0"), projectsDir.resolve("project1"), false,
				false);
	}

	@Test
	public void testSaveExistingMusicPackProject() throws IOException {
		saveExistingMusicPackProject(createMockMusicPackProject("0.1.0"), projectsDir.resolve("project1"), true, false);
	}

	@Test(expected = NullPointerException.class)
	public void testSaveExistingMusicPackProjectNull() throws IOException {
		persistenceManager.saveMusicPackProject(null);
	}

	@Test(expected = ServiceException.class)
	public void testSaveExistingMusicPackProjectIOException() throws IOException {
		saveExistingMusicPackProject(createMockMusicPackProject("0.1.0"), projectsDir.resolve("project1"), true, true);
	}

	private void saveExistingMusicPackProject(MusicPackProject project, Path projectPath, boolean inject,
			boolean exception) throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(this.workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andStubReturn(projectsDir);

		OutputStream mockOutputStream = EasyMock.createMock(OutputStream.class);

		Path projectFilePath = projectPath.resolve(MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME);

		mockMusicPackProjectWriter.writeMusicPackProject(project, mockOutputStream);

		EasyMock.expectLastCall().once();

		if (!exception) {
			EasyMock.expect(mockFileManager.newOutputStream(projectFilePath)).andReturn(mockOutputStream).once();
		} else {
			EasyMock.expect(mockFileManager.newOutputStream(projectFilePath))
					.andThrow(new IOException("Couldn't open the output stream")).once();
		}

		this.replayAll();
		EasyMock.replay(project);

		persistenceManager.onActivate();
		if (inject)
			persistenceManager.getManagedMusicPackProjects().put(project, projectPath);
		persistenceManager.saveMusicPackProject(project);

		this.verifyAll();
	}

	@Test(expected = NullPointerException.class)
	public void testDeleteMusicPackProjectNull() {
		persistenceManager.deleteMusicPackProject(null);
	}

	@Test
	public void testDeleteNonExistingMusicPackProject() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(this.workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andStubReturn(projectsDir);

		MusicPackProject project = createMockMusicPackProject("1.2.0");

		this.replayAll();

		persistenceManager.onActivate();
		Assert.assertFalse(persistenceManager.deleteMusicPackProject(project));

		this.verifyAll();
	}

	@Test
	public void testDeleteMusicPackProject() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(this.workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andStubReturn(projectsDir);

		MusicPackProject project = createMockMusicPackProject("1.2.0");
		Path projectDir = projectsDir.resolve("project1");

		mockFileManager.deleteDirAndContent(projectDir);

		EasyMock.expectLastCall().once();

		this.replayAll();
		EasyMock.replay(project);

		persistenceManager.onActivate();
		persistenceManager.getManagedMusicPackProjects().put(project, projectDir);
		Assert.assertTrue(persistenceManager.deleteMusicPackProject(project));
		Assert.assertTrue(persistenceManager.getManagedMusicPackProjects().isEmpty());

		this.verifyAll();
	}

	@Test(expected = ServiceException.class)
	public void testDeleteMusicPackProjectIOException() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(this.workspaceRoot.toString(),
				MusicPackProjectPersistenceManagerImpl.PROJECTS_DIR_NAME)).andStubReturn(projectsDir);

		MusicPackProject project = createMockMusicPackProject("1.2.0");

		mockFileManager.deleteDirAndContent(null);

		EasyMock.expectLastCall().andThrow(new IOException("Couldn't delete the project directory"));

		this.replayAll();
		EasyMock.replay(project);

		persistenceManager.onActivate();
		persistenceManager.getManagedMusicPackProjects().put(project, null);
		persistenceManager.deleteMusicPackProject(project);
	}

	private MusicPackProject createMockMusicPackProject(String version) {
		MusicPackProject mockMusicPackProject = EasyMock.createMock(MusicPackProject.class);
		PrimitiveProperties properties = new PrimitiveProperties();
		properties.put(MusicPackProject.PROPERTY_MPC_VERSION, version);

		EasyMock.expect(mockMusicPackProject.getProperties()).andStubReturn(properties);
		EasyMock.expect(mockMusicPackProject.getName()).andStubReturn("proj");

		return mockMusicPackProject;
	}

}
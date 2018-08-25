package craftedMods.lotr.mpc.persistence.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.compatibility.api.MusicPackProjectCompatibilityManager;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectPersistenceManager;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectReader;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectWriter;
import craftedMods.lotr.mpc.persistence.api.TrackStoreManager;
import craftedMods.lotr.mpc.persistence.provider.MusicPackProjectPersistenceManagerImpl.Configuration;
import craftedMods.utils.data.ExtendedProperties;
import craftedMods.utils.data.PrimitiveProperties;
import craftedMods.versionChecker.base.DefaultSemanticVersion;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MusicPackProjectPersistenceManagerImpl.class)
public class MusicPackProjectPersistenceManagerImplTest extends EasyMockSupport {

	@TestSubject
	public MusicPackProjectPersistenceManagerImpl persistenceManager = new MusicPackProjectPersistenceManagerImpl();

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

	@Mock
	private MusicPackProjectManager mockMusicPackProjectManager;

	@Mock
	private TrackStoreManager mockTrackStoreManager;

	private Path projectsDir;

	private Map<MusicPackProject, Path> managedMusicPackProjects;

	@Before
	public void setup() {
		projectsDir = folder.getRoot().toPath().resolve("projects");

		persistenceManager.mpcVersion = DefaultSemanticVersion.of("0.1.0");

		managedMusicPackProjects = new HashMap<>();
		EasyMock.expect(mockMusicPackProjectManager.getManagedMusicPackProjects())
				.andStubReturn(managedMusicPackProjects);
	}

	@Test
	public void testOnActivate() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andReturn(projectsDir);

		this.replayAll();

		persistenceManager.onActivate(createConfig());

		Assert.assertNotNull(managedMusicPackProjects);
		Assert.assertTrue(managedMusicPackProjects.isEmpty());

		this.verifyAll();
	}

	@Test
	public void testLoadMusicPackProjects() throws IOException {
		Path projectPath1 = projectsDir.resolve("project1");

		Path projectFilePath1 = projectPath1.resolve(MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME);

		InputStream projectStream1 = EasyMock.createMock(InputStream.class);

		MusicPackProject mockMusicPackProject1 = createMockMusicPackProject("0.1.0");

		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);
		EasyMock.expect(mockFileManager.getPathsInDirectory(projectsDir))
				.andReturn(Arrays.stream(new Path[] { projectPath1 })).once();
		EasyMock.expect(mockFileManager.isDirectory(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.exists(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.newInputStream(projectFilePath1)).andReturn(projectStream1).once();

		EasyMock.expect(mockMusicPackProjectReader.readMusicPackProject(projectStream1))
				.andReturn(mockMusicPackProject1).once();

		mockCompatibilityManager.applyPreLoadFixes(projectPath1);
		EasyMock.expectLastCall().once();

		Capture<MusicPackProject> postLoadProjectCapture = Capture.newInstance();

		mockCompatibilityManager.applyPostLoadFixes(EasyMock.eq(projectPath1), EasyMock.capture(postLoadProjectCapture),
				EasyMock.eq("0.1.0"));
		EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Object answer() throws Throwable {
				Assert.assertTrue(managedMusicPackProjects.containsKey(postLoadProjectCapture.getValue()));
				return null;
			}
		}).once();

		this.replayAll();

		persistenceManager.onActivate(createConfig());
		Collection<MusicPackProject> projects = persistenceManager.loadMusicPackProjects();

		Assert.assertEquals(1, projects.size());

		MusicPackProject project = projects.iterator().next();

		Assert.assertTrue(project == mockMusicPackProject1);

		Assert.assertTrue(managedMusicPackProjects.containsKey(project));
		Assert.assertEquals(projectPath1, managedMusicPackProjects.get(project));

		MusicPackProject postLoadProject = postLoadProjectCapture.getValue();

		Assert.assertEquals(mockMusicPackProject1, postLoadProject);

		this.verifyAll();
	}

	@Test
	public void testLoadNoMusicPackProjects() throws IOException {
		EasyMock.expect(mockFileManager.getPathsInDirectory(projectsDir)).andReturn(Arrays.stream(new Path[] {}))
				.once();
		EasyMock.expect(mockFileManager.isDirectory(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);

		this.replayAll();

		persistenceManager.onActivate(createConfig());
		Collection<MusicPackProject> projects = persistenceManager.loadMusicPackProjects();

		Assert.assertEquals(0, projects.size());
		Assert.assertTrue(managedMusicPackProjects.isEmpty());

		this.verifyAll();
	}

	@Test(expected = ServiceException.class)
	public void testLoadMusicPackProjectFatalLoadingError() throws IOException {
		EasyMock.expect(mockFileManager.getPathsInDirectory(projectsDir))
				.andStubThrow(new IOException("Could not get the files in the specified directory"));
		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);

		this.replayAll();

		persistenceManager.onActivate(createConfig());
		persistenceManager.loadMusicPackProjects();
	}

	@Test
	public void testLoadMusicPackProjectProjectSpecificLoadingError() throws IOException {
		Path projectPath1 = projectsDir.resolve("project1");

		Path projectFilePath1 = projectPath1.resolve(MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME);

		InputStream projectStream1 = EasyMock.createMock(InputStream.class);

		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);
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

		persistenceManager.onActivate(createConfig());
		Collection<MusicPackProject> projects = persistenceManager.loadMusicPackProjects();

		Assert.assertEquals(0, projects.size());
		Assert.assertTrue(managedMusicPackProjects.isEmpty());

		WriteableEventProperties value = propertiesCapture.getValue();

		Assert.assertEquals(thrownException,
				value.getProperty(MusicPackProjectPersistenceManager.LOAD_ALL_PROJECT_ERROR_EVENT_EXCEPTION));

		this.verifyAll();
	}

	@Test
	public void testLoadMusicPackProjectNoProjectFile() throws IOException {
		Path projectPath1 = projectsDir.resolve("project1");

		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);
		EasyMock.expect(mockFileManager.getPathsInDirectory(projectsDir))
				.andStubReturn(Arrays.stream(new Path[] { projectPath1 }));
		EasyMock.expect(mockFileManager.isDirectory(EasyMock.anyObject())).andStubReturn(true);
		EasyMock.expect(mockFileManager.exists(EasyMock.anyObject())).andStubReturn(false);

		mockCompatibilityManager.applyPreLoadFixes(projectPath1);
		EasyMock.expectLastCall().once();

		this.replayAll();

		persistenceManager.onActivate(createConfig());
		Collection<MusicPackProject> projects = persistenceManager.loadMusicPackProjects();

		Assert.assertEquals(0, projects.size());
		Assert.assertTrue(managedMusicPackProjects.isEmpty());

		this.verifyAll();
	}

	@Test
	public void testLoadMusicPackProjectNewerVersion() throws IOException {
		Path projectPath1 = projectsDir.resolve("project1");

		Path projectFilePath1 = projectPath1.resolve(MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME);

		InputStream projectStream1 = EasyMock.createMock(InputStream.class);

		MusicPackProject mockMusicPackProject1 = createMockMusicPackProject("0.2.0");

		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);
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

		mockCompatibilityManager.applyPostLoadFixes(projectPath1, mockMusicPackProject1, "0.2.0");
		EasyMock.expectLastCall().once();

		this.replayAll();

		persistenceManager.onActivate(createConfig());
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

		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);
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

		mockCompatibilityManager.applyPostLoadFixes(projectPath1, mockMusicPackProject1, "0.0.1");
		EasyMock.expectLastCall().once();

		this.replayAll();

		persistenceManager.onActivate(createConfig());
		persistenceManager.loadMusicPackProjects();

		WriteableEventProperties value = propertiesCapture.getValue();

		Assert.assertEquals("0.0.1",
				value.getProperty(MusicPackProjectPersistenceManager.OLDER_SAVE_VERSION_EVENT_DETECTED_VERSION));
		Assert.assertTrue(mockMusicPackProject1 == value
				.getProperty(MusicPackProjectPersistenceManager.OLDER_SAVE_VERSION_EVENT_MUSIC_PACK_PROJECT));

		this.verifyAll();
	}

	@Test
	public void testLoadMusicPackProjectNonSemanticVersion() throws IOException {
		Path projectPath1 = projectsDir.resolve("project1");

		Path projectFilePath1 = projectPath1.resolve(MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME);

		InputStream projectStream1 = EasyMock.createMock(InputStream.class);

		MusicPackProject mockMusicPackProject1 = createMockMusicPackProject("Music Pack Creator 10.12.14");

		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);
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

		mockCompatibilityManager.applyPostLoadFixes(projectPath1, mockMusicPackProject1, "Music Pack Creator 10.12.14");
		EasyMock.expectLastCall().once();

		this.replayAll();

		persistenceManager.onActivate(createConfig());
		persistenceManager.loadMusicPackProjects();

		WriteableEventProperties value = propertiesCapture.getValue();

		Assert.assertEquals("Music Pack Creator 10.12.14",
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

		Assert.assertTrue(managedMusicPackProjects.containsKey(project));
		Assert.assertEquals(projectPath, managedMusicPackProjects.get(project));
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
		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);

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

		persistenceManager.onActivate(createConfig());
		if (inject)
			managedMusicPackProjects.put(project, projectPath);
		persistenceManager.saveMusicPackProject(project);

		this.verifyAll();
	}

	@Test(expected = NullPointerException.class)
	public void testDeleteMusicPackProjectNull() {
		persistenceManager.deleteMusicPackProject(null);
	}

	@Test
	public void testDeleteNonExistingMusicPackProject() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);

		MusicPackProject project = createMockMusicPackProject("1.2.0");

		this.replayAll();

		persistenceManager.onActivate(createConfig());
		Assert.assertFalse(persistenceManager.deleteMusicPackProject(project));

		this.verifyAll();
	}

	@Test
	public void testDeleteMusicPackProject() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);

		MusicPackProject project = createMockMusicPackProject("1.2.0");
		Path projectDir = projectsDir.resolve("project1");

		EasyMock.expect(mockFileManager.deleteDirAndContent(projectDir)).andReturn(true).once();

		mockTrackStoreManager.deleteTrackStore(project);
		EasyMock.expectLastCall().once();

		this.replayAll();

		persistenceManager.onActivate(createConfig());
		managedMusicPackProjects.put(project, projectDir);
		Assert.assertTrue(persistenceManager.deleteMusicPackProject(project));
		Assert.assertTrue(managedMusicPackProjects.isEmpty());

		this.verifyAll();
	}

	@Test(expected = ServiceException.class)
	public void testDeleteMusicPackProjectIOException() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);

		MusicPackProject project = createMockMusicPackProject("1.2.0");

		EasyMock.expect(mockFileManager.deleteDirAndContent(null))
				.andThrow(new IOException("Couldn't delete the project directory")).once();

		this.replayAll();

		persistenceManager.onActivate(createConfig());
		managedMusicPackProjects.put(project, null);
		persistenceManager.deleteMusicPackProject(project);
	}

	private Configuration createConfig() {
		return new Configuration() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Configuration.class;
			}

			@Override
			public String projectsDirectory() {
				return projectsDir.toString();
			}
		};
	}

	private MusicPackProject createMockMusicPackProject(String version) {
		MusicPackProject mockMusicPackProject = this.createMock(MusicPackProject.class);
		PrimitiveProperties properties = new ExtendedProperties();
		properties.put(MusicPackProject.PROPERTY_MPC_VERSION, version);

		EasyMock.expect(mockMusicPackProject.getProperties()).andStubReturn(properties);
		EasyMock.expect(mockMusicPackProject.getName()).andStubReturn("proj");

		return mockMusicPackProject;
	}

}

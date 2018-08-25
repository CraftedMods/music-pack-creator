package craftedMods.lotr.mpc.persistence.provider;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceException;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.persistence.api.TrackStore;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TrackStoreManagerImpl.class)
public class TrackStoreManagerImplTest extends EasyMockSupport {

	@TestSubject
	public TrackStoreManagerImpl trackStore = new TrackStoreManagerImpl();

	@Mock
	private FileManager mockFileManager;

	@Mock
	private MusicPackProjectManager mockMusicPackProjectManager;

	private Map<MusicPackProject, Path> managedMusicPackProjects;

	private Path projectsDir;

	@Before
	public void setup() {
		projectsDir = Paths.get("projects");

		managedMusicPackProjects = new HashMap<>();
		EasyMock.expect(mockMusicPackProjectManager.getManagedMusicPackProjects())
				.andStubReturn(managedMusicPackProjects);

		trackStore.onActivate();
	}

	@Test
	public void testOnActivate() {
		Assert.assertNotNull(trackStore.getTrackStores());
		Assert.assertTrue(trackStore.getTrackStores().isEmpty());
	}

	@Test(expected = NullPointerException.class)
	public void testGetTrackStoreNull() {
		trackStore.getTrackStore(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTrackStoreUnmanagedProject() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);

		this.replayAll();

		trackStore.getTrackStore(this.createMock(MusicPackProject.class));

		this.verifyAll();
	}

	@Test
	public void testGetTrackStoreManagedProjectAndPresentStore() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);

		MusicPackProject mockProject = createMockMusicPackProject();
		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		Path projectDir = projectsDir.resolve("proj");

		this.replayAll();

		managedMusicPackProjects.put(mockProject, projectDir);
		trackStore.getTrackStores().put(mockProject, mockTrackStore);
		trackStore.getTrackStore(mockProject);

		this.verifyAll();
	}

	@Test(expected = ServiceException.class)
	public void testGetTrackStoreManagedProjectIOException() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);

		MusicPackProject mockProject = createMockMusicPackProject();

		Path projectDir = projectsDir.resolve("proj");

		EasyMock.expect(mockFileManager.getPathAndCreateDir(EasyMock.anyString(), EasyMock.anyString()))
				.andStubThrow(new IOException("An error occured"));

		this.replayAll();

		managedMusicPackProjects.put(mockProject, projectDir);
		trackStore.getTrackStore(mockProject);
	}

	@Test
	public void testGetTrackStoreManagedProject() throws Exception {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectsDir.toString())).andStubReturn(projectsDir);

		MusicPackProject mockProject = createMockMusicPackProject();

		Path projectDir = projectsDir.resolve("proj");
		Path tracksStoreRootDir = projectDir.resolve("tracks");

		TrackStoreImpl mockTrackStore = PowerMock.createMock(TrackStoreImpl.class);

		PowerMock.expectNew(TrackStoreImpl.class, mockProject, tracksStoreRootDir, mockFileManager)
				.andReturn(mockTrackStore).once();

		EasyMock.expect(mockFileManager.getPathAndCreateDir(projectDir.toString(), "tracks"))
				.andReturn(tracksStoreRootDir).once();

		mockTrackStore.refresh();
		EasyMock.expectLastCall().once();

		this.replayAll();
		PowerMock.replayAll();

		managedMusicPackProjects.put(mockProject, projectDir);
		TrackStore store = trackStore.getTrackStore(mockProject);

		Assert.assertEquals(mockTrackStore, store);

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test(expected = NullPointerException.class)
	public void testDeleteTrackStoreNull() {
		trackStore.deleteTrackStore(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteTrackStoreUnmanagedProject() {
		this.replayAll();

		trackStore.deleteTrackStore(createMockMusicPackProject());
	}

	@Test
	public void testDeleteTrackStoreMananagedProject() {
		MusicPackProject mockMusicPackProject = this.createMockMusicPackProject();

		Path projectDir = projectsDir.resolve("proj");

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		this.replayAll();

		managedMusicPackProjects.put(mockMusicPackProject, projectDir);
		trackStore.getTrackStores().put(mockMusicPackProject, mockTrackStore);

		trackStore.deleteTrackStore(mockMusicPackProject);

		Assert.assertTrue(trackStore.getTrackStores().isEmpty());

		this.verifyAll();
	}

	private MusicPackProject createMockMusicPackProject() {
		MusicPackProject mock = this.createMock(MusicPackProject.class);
		EasyMock.expect(mock.getName()).andStubReturn("proj");
		return mock;
	}

}

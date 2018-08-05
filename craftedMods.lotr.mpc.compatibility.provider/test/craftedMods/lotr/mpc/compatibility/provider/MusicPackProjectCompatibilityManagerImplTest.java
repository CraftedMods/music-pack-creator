package craftedMods.lotr.mpc.compatibility.provider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
import org.osgi.service.log.LogService;

import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.EventProperties;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.eventManager.base.DefaultWriteableEventProperties;
import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.compatibility.api.MusicPackProjectCompatibilityManager;
import craftedMods.lotr.mpc.core.api.MusicPack;
import craftedMods.lotr.mpc.core.api.MusicPackCreator;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.core.api.MusicPackProjectFactory;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.lotr.mpc.core.base.DefaultRegion;
import craftedMods.lotr.mpc.core.base.DefaultTrack;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectWriter;

@RunWith(EasyMockRunner.class)
public class MusicPackProjectCompatibilityManagerImplTest extends EasyMockSupport {

	@TestSubject
	public MusicPackProjectCompatibilityManagerImpl compatibilityManager = new MusicPackProjectCompatibilityManagerImpl();

	@Mock
	private EventManager mockEventManager;

	@Mock(type = MockType.NICE)
	private MusicPackCreator mockMusicPackCreator;

	@Mock
	private MusicPackProjectFactory mockMusicPackProjectFactory;

	@Mock
	private MusicPackProjectWriter mockMusicPackProjectWriter;

	@Mock(type = MockType.NICE)
	private LogService mockLogService;

	@Mock
	private FileManager mockFileManager;

	@Mock
	private SerializedWorkspaceToJSONConverter mockSerializedConverter;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private Path projectsDir;
	private Path oldProjectFile;

	@Before
	public void setup() {
		projectsDir = folder.getRoot().toPath();
		oldProjectFile = projectsDir.resolve(SerializedWorkspaceToJSONConverter.OLD_PROJECT_FILE);
	}

	@Test
	public void testApplyPreLoadFixesSerializedWorkspaceNoOldFile() {
		EasyMock.expect(mockFileManager.exists(oldProjectFile)).andReturn(false).once();

		replayAll();

		compatibilityManager.applyPreLoadFixes(projectsDir);

		verifyAll();
	}

	@Test
	public void testApplyPreLoadFixesSerializedWorkspace() {
		EasyMock.expect(mockFileManager.exists(oldProjectFile)).andReturn(true).once();

		Capture<WriteableEventProperties> capturedProperties1 = Capture.newInstance();

		Collection<EventProperties> results = new ArrayList<>();

		WriteableEventProperties proceedProperties = new DefaultWriteableEventProperties();
		proceedProperties.put(
				MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_RESULT_PROCEED,
				Boolean.TRUE);

		results.add(proceedProperties);

		EasyMock.expect(mockEventManager.dispatchEvent(
				EasyMock.eq(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT),
				EasyMock.capture(capturedProperties1))).andReturn(results).once();

		mockSerializedConverter.convertWorkspace(projectsDir);
		EasyMock.expectLastCall().once();

		Capture<WriteableEventProperties> capturedProperties2 = Capture.newInstance();

		EasyMock.expect(mockEventManager.dispatchEvent(
				EasyMock.eq(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_CONVERTED_EVENT),
				EasyMock.capture(capturedProperties2))).andReturn(null).once();

		replayAll();

		compatibilityManager.applyPreLoadFixes(projectsDir);

		WriteableEventProperties properties1 = capturedProperties1.getValue();
		WriteableEventProperties properties2 = capturedProperties2.getValue();

		Assert.assertEquals(projectsDir, properties1
				.getProperty(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_PATH));
		Assert.assertEquals(projectsDir, properties2
				.getProperty(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_CONVERTED_EVENT_PATH));

		verifyAll();
	}

	@Test
	public void testApplyPreLoadFixesSerializedWorkspaceNoFeedbackFromListeners() {
		EasyMock.expect(mockFileManager.exists(oldProjectFile)).andReturn(true).once();

		EasyMock.expect(mockEventManager.dispatchEvent(
				EasyMock.eq(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT),
				EasyMock.anyObject(WriteableEventProperties.class))).andReturn(new ArrayList<>()).once();

		mockSerializedConverter.convertWorkspace(projectsDir);
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockEventManager.dispatchEvent(
				EasyMock.eq(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_CONVERTED_EVENT),
				EasyMock.anyObject(WriteableEventProperties.class))).andReturn(null).once();

		replayAll();

		compatibilityManager.applyPreLoadFixes(projectsDir);

		verifyAll();
	}

	@Test
	public void testApplyPreLoadFixesSerializedWorkspaceCancel() {
		EasyMock.expect(mockFileManager.exists(oldProjectFile)).andReturn(true).once();

		Capture<WriteableEventProperties> capturedProperties = Capture.newInstance();

		Collection<EventProperties> results = new ArrayList<>();

		WriteableEventProperties proceedProperties = new DefaultWriteableEventProperties();
		proceedProperties.put(
				MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_RESULT_PROCEED,
				Boolean.TRUE);

		WriteableEventProperties cancelProperties = new DefaultWriteableEventProperties();
		proceedProperties.put(
				MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_RESULT_PROCEED,
				Boolean.FALSE);

		results.add(proceedProperties);
		results.add(cancelProperties);

		EasyMock.expect(mockEventManager.dispatchEvent(
				EasyMock.eq(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT),
				EasyMock.capture(capturedProperties))).andReturn(results).once();

		replayAll();

		compatibilityManager.applyPreLoadFixes(projectsDir);

		verifyAll();
	}

	@Test
	public void testApplyPreLoadFixesSerializedWorkspaceConversionException() {
		EasyMock.expect(mockFileManager.exists(oldProjectFile)).andReturn(true).once();

		EasyMock.expect(mockEventManager.dispatchEvent(
				EasyMock.eq(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT),
				EasyMock.anyObject(WriteableEventProperties.class))).andReturn(new ArrayList<>()).once();

		RuntimeException thrownException = new RuntimeException("Error");

		mockSerializedConverter.convertWorkspace(projectsDir);
		EasyMock.expectLastCall().andThrow(thrownException).once();

		Capture<WriteableEventProperties> capturedProperties = Capture.newInstance();

		EasyMock.expect(mockEventManager.dispatchEvent(
				EasyMock.eq(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT),
				EasyMock.capture(capturedProperties))).andReturn(null).once();

		replayAll();

		compatibilityManager.applyPreLoadFixes(projectsDir);

		WriteableEventProperties properties = capturedProperties.getValue();

		Assert.assertEquals(thrownException, properties
				.getProperty(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT_EXCEPTION));
		Assert.assertEquals(projectsDir, properties
				.getProperty(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT_PATH));

		verifyAll();
	}

	@Test(expected = NullPointerException.class)
	public void testApplyPreLoadFixesNull() {
		compatibilityManager.applyPreLoadFixes(null);
	}

	@Test
	public void testApplyPostLoadFixesAndrastFix() {
		MusicPackProject mockMusicPackProject = this.createMock(MusicPackProject.class);
		MusicPack mockMusicPack = this.createMock(MusicPack.class);
		List<Track> tracks = new ArrayList<>();

		EasyMock.expect(mockMusicPackProject.getMusicPack()).andStubReturn(mockMusicPack);
		EasyMock.expect(mockMusicPack.getTracks()).andStubReturn(tracks);
		EasyMock.expect(mockMusicPackProject.getName()).andStubReturn("Name");

		tracks.add(new DefaultTrack(Paths.get("test", "test2"), "title",
				Arrays.asList(new DefaultRegion("andrast", Arrays.asList("test"), Arrays.asList(), null)),
				Arrays.asList()));
		tracks.add(new DefaultTrack(Paths.get("test", "test3"), "2title",
				Arrays.asList(new DefaultRegion("all", Arrays.asList(), Arrays.asList(), null)), Arrays.asList()));
		tracks.add(new DefaultTrack(Paths.get("test", "test4"), "2titl3e",
				Arrays.asList(new DefaultRegion("andrast", Arrays.asList(), Arrays.asList(), null)),
				Arrays.asList("s")));

		Capture<WriteableEventProperties> capturedProject = Capture.newInstance();

		EasyMock.expect(mockEventManager.dispatchEvent(
				EasyMock.eq(MusicPackProjectCompatibilityManager.POST_LOAD_ANDRAST_FIX_EVENT),
				EasyMock.capture(capturedProject))).andReturn(null).times(2);

		this.replayAll();

		compatibilityManager.applyPostLoadFixes(mockMusicPackProject, "Music Pack Creator Beta 2.0");

		WriteableEventProperties properties = capturedProject.getValue();

		Assert.assertEquals(mockMusicPackProject, properties
				.getProperty(MusicPackProjectCompatibilityManager.POST_LOAD_ANDRAST_FIX_EVENT_MUSIC_PACK_PROJECT));

		this.verifyAll();
	}

	@Test
	public void testApplyPostLoadFixesAndrastFixNoTracksToFix() {
		MusicPackProject mockMusicPackProject = this.createMock(MusicPackProject.class);
		MusicPack mockMusicPack = this.createMock(MusicPack.class);
		List<Track> tracks = new ArrayList<>();

		EasyMock.expect(mockMusicPackProject.getMusicPack()).andStubReturn(mockMusicPack);
		EasyMock.expect(mockMusicPack.getTracks()).andStubReturn(tracks);

		tracks.add(new DefaultTrack(Paths.get("test", "test2"), "title",
				Arrays.asList(new DefaultRegion("all", Arrays.asList(), Arrays.asList(), null)), Arrays.asList()));

		this.replayAll();

		compatibilityManager.applyPostLoadFixes(mockMusicPackProject, "Music Pack Creator Beta 2.0");

		this.verifyAll();
	}

	@Test
	public void testApplyPostLoadFixesAndrastFixWrongVersion() {
		MusicPackProject mockMusicPackProject = this.createMock(MusicPackProject.class);

		this.replayAll();

		compatibilityManager.applyPostLoadFixes(mockMusicPackProject, "Music Pack Creator Beta 3.3");

		this.verifyAll();
	}
	
	@Test
	public void testApplyPostLoadFixesAndrastFixUnprefixedVersion() {
		MusicPackProject mockMusicPackProject = this.createMock(MusicPackProject.class);

		this.replayAll();

		compatibilityManager.applyPostLoadFixes(mockMusicPackProject, "2.0");

		this.verifyAll();
	}

	@Test(expected = NullPointerException.class)
	public void testApplyPostLoadFixesNull() {
		compatibilityManager.applyPostLoadFixes(null, null);
	}

}

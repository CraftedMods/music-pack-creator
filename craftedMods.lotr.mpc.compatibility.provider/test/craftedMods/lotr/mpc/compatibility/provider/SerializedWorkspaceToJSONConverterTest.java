package craftedMods.lotr.mpc.compatibility.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceException;

import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.core.api.MusicPack;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.core.api.MusicPackProjectFactory;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.lotr.mpc.core.base.DefaultRegion;
import craftedMods.lotr.mpc.core.base.DefaultTrack;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectWriter;
import craftedMods.utils.Utils;
import craftedMods.utils.data.ExtendedProperties;
import craftedMods.utils.data.PrimitiveProperties;

@RunWith(EasyMockRunner.class)
public class SerializedWorkspaceToJSONConverterTest extends EasyMockSupport {

	@TestSubject
	public SerializedWorkspaceToJSONConverter converter = new SerializedWorkspaceToJSONConverter();

	@Mock
	private MusicPackProjectFactory mockMusicPackProjectFactory;

	@Mock
	private MusicPackProjectWriter mockMusicPackProjectWriter;

	@Mock
	private FileManager mockFileManager;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private Path projectFolder;

	@Before
	public void setup() throws IOException {
		projectFolder = folder.getRoot().toPath();

		try (InputStream in = this.getClass().getResourceAsStream("serializedProject.lmpp");
				OutputStream out = Files
						.newOutputStream(projectFolder.resolve(SerializedWorkspaceToJSONConverter.OLD_PROJECT_FILE))) {
			Utils.writeFromInputStreamToOutputStream(in, out);
		}
		converter.onActivate();
	}

	@Test
	public void testOnActivate() {
		Assert.assertNotNull(converter.getOldProjects());
		Assert.assertTrue(converter.getOldProjects().isEmpty());
	}

	@Test
	public void testConvertWorkspace() throws IOException {
		MusicPackProject mockMusicPackProject = createMock(MusicPackProject.class);
		MusicPack mockMusicPack = createMock(MusicPack.class);

		Set<Track> tracks = new LinkedHashSet<>();
		PrimitiveProperties properties = new ExtendedProperties();

		EasyMock.expect(mockMusicPackProject.getName()).andStubReturn("Testproject");
		EasyMock.expect(mockMusicPackProject.getMusicPack()).andStubReturn(mockMusicPack);
		EasyMock.expect(mockMusicPackProject.getProperties()).andStubReturn(properties);

		EasyMock.expect(mockMusicPack.getTracks()).andStubReturn(tracks);

		EasyMock.expect(mockMusicPackProjectFactory.createMusicPackProjectInstance("Testproject"))
				.andReturn(mockMusicPackProject);

		OutputStream mockOutputStream = createMock(OutputStream.class);

		EasyMock.expect(
				mockFileManager.createFile(projectFolder.resolve(SerializedWorkspaceToJSONConverter.NEW_PROJECT_FILE)))
				.andReturn(true);
		EasyMock.expect(mockFileManager
				.newOutputStream(projectFolder.resolve(SerializedWorkspaceToJSONConverter.NEW_PROJECT_FILE)))
				.andReturn(mockOutputStream).once();
		mockFileManager.rename(Paths.get(projectFolder.toString(), SerializedWorkspaceToJSONConverter.OLD_PROJECT_FILE),
				SerializedWorkspaceToJSONConverter.OLD_PROJECT_FILE_RENAMED);
		EasyMock.expectLastCall().once();

		mockMusicPackProjectWriter.writeMusicPackProject(mockMusicPackProject, mockOutputStream);

		EasyMock.expectLastCall().once();

		replayAll();

		converter.convertWorkspace(projectFolder);

		Assert.assertEquals(mockMusicPackProject.getName(), "Testproject");
		Assert.assertEquals("Music Pack Creator Beta 4.0",
				mockMusicPackProject.getProperties().get(MusicPackProject.PROPERTY_MPC_VERSION));
		Assert.assertEquals(3, mockMusicPackProject.getMusicPack().getTracks().size());

		Track track1 = new DefaultTrack("The Descent.ogg", "The Descent",
				Arrays.asList(new DefaultRegion("all", Arrays.asList(), Arrays.asList(), null)),
				Arrays.asList("Crafted_Mods"));
		Track track2 = new DefaultTrack("Skye Cuillin.ogg", "Skye Cuillin",
				Arrays.asList(new DefaultRegion("angmar", Arrays.asList("ettenmoors"), Arrays.asList("night"), null)),
				Arrays.asList());
		Track track3 = new DefaultTrack("The-Castle-Beyond-the-Forest.ogg", "A Track",
				Arrays.asList(new DefaultRegion("dale", Arrays.asList(), Arrays.asList(), null)),
				Arrays.asList("Someone"));

		int c = 1;
		for (Track track : mockMusicPack.getTracks()) {
			Assert.assertEquals(c == 1 ? track1 : c == 2 ? track2 : track3, track);
			++c;
		}

		Assert.assertTrue(converter.getOldProjects().containsKey(projectFolder));
		Assert.assertEquals(3, converter.getOldProjects().get(projectFolder).size());

		verifyAll();
	}

	@Test(expected = ServiceException.class)
	public void testSaveNewProjectIOException() throws IOException {
		MusicPackProject mockMusicPackProject = createMock(MusicPackProject.class);
		MusicPack mockMusicPack = createMock(MusicPack.class);

		Set<Track> tracks = new LinkedHashSet<>();
		PrimitiveProperties properties = new ExtendedProperties();

		EasyMock.expect(mockMusicPackProject.getName()).andStubReturn("Testproject");
		EasyMock.expect(mockMusicPackProject.getMusicPack()).andStubReturn(mockMusicPack);
		EasyMock.expect(mockMusicPackProject.getProperties()).andStubReturn(properties);

		EasyMock.expect(mockMusicPack.getTracks()).andStubReturn(tracks);

		EasyMock.expect(mockMusicPackProjectFactory.createMusicPackProjectInstance(EasyMock.anyString()))
				.andStubReturn(mockMusicPackProject);

		OutputStream mockOutputStream = createMock(OutputStream.class);

		EasyMock.expect(
				mockFileManager.createFile(projectFolder.resolve(SerializedWorkspaceToJSONConverter.NEW_PROJECT_FILE)))
				.andThrow(new IOException("Error"));

		mockMusicPackProjectWriter.writeMusicPackProject(mockMusicPackProject, mockOutputStream);

		EasyMock.expectLastCall().once();

		replayAll();

		converter.convertWorkspace(projectFolder);
	}

	@Test(expected = ServiceException.class)
	public void testSaveDeleteOldProjectFileIOException() throws IOException {
		MusicPackProject mockMusicPackProject = createMock(MusicPackProject.class);
		MusicPack mockMusicPack = createMock(MusicPack.class);

		Set<Track> tracks = new LinkedHashSet<>();
		PrimitiveProperties properties = new ExtendedProperties();

		EasyMock.expect(mockMusicPackProject.getName()).andStubReturn("Testproject");
		EasyMock.expect(mockMusicPackProject.getMusicPack()).andStubReturn(mockMusicPack);
		EasyMock.expect(mockMusicPackProject.getProperties()).andStubReturn(properties);

		EasyMock.expect(mockMusicPack.getTracks()).andStubReturn(tracks);

		EasyMock.expect(mockMusicPackProjectFactory.createMusicPackProjectInstance(EasyMock.anyString()))
				.andStubReturn(mockMusicPackProject);

		OutputStream mockOutputStream = createMock(OutputStream.class);

		EasyMock.expect(
				mockFileManager.createFile(projectFolder.resolve(SerializedWorkspaceToJSONConverter.NEW_PROJECT_FILE)))
				.andStubReturn(true);
		EasyMock.expect(mockFileManager
				.newOutputStream(projectFolder.resolve(SerializedWorkspaceToJSONConverter.NEW_PROJECT_FILE)))
				.andStubReturn(mockOutputStream);
		mockFileManager.rename(Paths.get(projectFolder.toString(), SerializedWorkspaceToJSONConverter.OLD_PROJECT_FILE),
				SerializedWorkspaceToJSONConverter.OLD_PROJECT_FILE_RENAMED);
		EasyMock.expectLastCall().andThrow(new IOException("Error")).once();

		mockMusicPackProjectWriter.writeMusicPackProject(mockMusicPackProject, mockOutputStream);

		EasyMock.expectLastCall().asStub();

		replayAll();

		converter.convertWorkspace(projectFolder);
	}

}

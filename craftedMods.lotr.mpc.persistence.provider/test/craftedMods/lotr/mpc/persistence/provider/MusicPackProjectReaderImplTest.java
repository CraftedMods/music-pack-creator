package craftedMods.lotr.mpc.persistence.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import craftedMods.lotr.mpc.core.api.MusicPack;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.core.api.MusicPackProjectFactory;
import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectReader;
import craftedMods.utils.data.ExtendedProperties;
import craftedMods.utils.data.PrimitiveProperties;

@RunWith(EasyMockRunner.class)
public class MusicPackProjectReaderImplTest {

	@TestSubject
	private MusicPackProjectReader reader = new MusicPackProjectReaderImpl();

	@Mock
	private MusicPackProjectFactory mockFactory;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private InputStream testInputStream;

	@Before
	public void setup() {
		testInputStream = this.getClass().getResourceAsStream("project.json");
	}

	@After
	public void cleanup() throws IOException {
		testInputStream.close();
	}

	@Test(expected = NullPointerException.class)
	public void testReadProjectNullPath() throws IOException {
		this.reader.readMusicPackProject(null);
	}

	@Test
	public void testReadProject() throws IOException, URISyntaxException {

		PrimitiveProperties musicPackProperties = new ExtendedProperties();

		MusicPack mockMusicPack = EasyMock.createNiceMock(MusicPack.class);

		List<Track> tracksList = new ArrayList<>();

		EasyMock.expect(mockFactory.createMusicPackProjectInstance("TestProject")).andAnswer(() -> {
			return new MusicPackProject() {

				@Override
				public PrimitiveProperties getProperties() {
					return musicPackProperties;
				}

				@Override
				public String getName() {
					return "TestProject";
				}

				@Override
				public MusicPack getMusicPack() {
					return mockMusicPack;
				}
			};
		}).once();

		EasyMock.expect(mockMusicPack.getTracks()).andReturn(tracksList).anyTimes();

		EasyMock.replay(mockFactory);
		EasyMock.replay(mockMusicPack);

		MusicPackProject readProject = reader.readMusicPackProject(testInputStream);

		EasyMock.verify(mockFactory);
		EasyMock.verify(mockMusicPack);

		checkReadMusicPackProject(readProject);
	}

	private void checkReadMusicPackProject(MusicPackProject project) {
		Assert.assertEquals("TestProject", project.getName());
		Assert.assertEquals(2, project.getMusicPack().getTracks().size());

		Track track1 = project.getMusicPack().getTracks().get(0);
		Assert.assertEquals(Paths.get("Path", "to", "an", "example", "track.ogg"), track1.getTrackPath());
		Assert.assertEquals("Example Track", track1.getTitle());
		Assert.assertEquals(2, track1.getRegions().size());
		Region track1Region1 = track1.getRegions().get(0);
		Assert.assertEquals(track1Region1.getName(), "Name");
		Assert.assertArrayEquals(new String[] { "subregion1", "subregion2", "subregion3", "subregion4" }, track1Region1.getSubregions().toArray(new String[4]));
		Assert.assertArrayEquals(new String[] { "category1", "category2" }, track1Region1.getCategories().toArray(new String[2]));
		Assert.assertNull(track1Region1.getWeight());
		Region track1Region2 = track1.getRegions().get(1);
		Assert.assertEquals(track1Region2.getName(), "Name2");
		Assert.assertArrayEquals(new String[] { "subregionPi" }, track1Region2.getSubregions().toArray(new String[1]));
		Assert.assertTrue(track1Region2.getCategories().isEmpty());
		Assert.assertEquals(Float.valueOf(1.5f), track1Region2.getWeight());
		Assert.assertArrayEquals(new String[] { "CraftedMods", "J.S. Bach" }, track1.getAuthors().toArray(new String[2]));

		Track track2 = project.getMusicPack().getTracks().get(1);
		Assert.assertEquals(Paths.get("C:", "entry"), track2.getTrackPath());
		Assert.assertNull(track2.getTitle());
		Assert.assertTrue(track2.getRegions().isEmpty());
		Assert.assertArrayEquals(new String[] { "Test_Author" }, track2.getAuthors().toArray(new String[1]));

		PrimitiveProperties properties = project.getProperties();
		Assert.assertEquals(2, properties.size());
		Assert.assertEquals("Crafted_Mods", properties.get(MusicPackProject.PROPERTY_AUTHOR));
		Assert.assertEquals("314.159.265-ALPHA.3", properties.get(MusicPackProject.PROPERTY_MPC_VERSION));
	}

}

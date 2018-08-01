package craftedMods.lotr.mpc.persistence.provider;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.easymock.EasyMock;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import com.google.gson.stream.JsonReader;

import craftedMods.lotr.mpc.core.api.*;
import craftedMods.lotr.mpc.core.base.*;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectWriter;
import craftedMods.utils.data.PrimitiveProperties;

public class MusicPackProjectWriterImplTest {

	private MusicPackProject mockMusicPackProject;

	private MusicPack mockMusicPack;

	private List<Track> tracks;

	private List<Region> regions;

	private PrimitiveProperties packProperties;

	private MusicPackProjectWriter writer;

	private ByteArrayOutputStream testOutputStream;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setup() {
		this.mockMusicPackProject = EasyMock.mock(MusicPackProject.class);

		this.mockMusicPack = EasyMock.mock(MusicPack.class);

		this.tracks = new ArrayList<>();
		this.regions = new ArrayList<>();

		this.regions.add(new DefaultRegion("Name", Arrays.asList("subregion1", "subregion2", "subregion3", "subregion4"),
				Arrays.asList("category1", "category2"), null));
		this.regions.add(new DefaultRegion("Name2", Arrays.asList("subregionPi"), Arrays.asList(), 1.5f));

		this.tracks.add(
				new DefaultTrack(Paths.get("C:\\Path\\to\\an\\example\\track.ogg"), "Example Track", this.regions, Arrays.asList("CraftedMods", "J.S. Bach")));
		this.tracks.add(new DefaultTrack(Paths.get("C:\\", "entry"), null, Arrays.asList(), Arrays.asList("Test_Author")));

		this.packProperties = new PrimitiveProperties();

		this.packProperties.put(MusicPackProject.PROPERTY_AUTHOR, "Crafted_Mods");
		this.packProperties.put(MusicPackProject.PROPERTY_MPC_VERSION, "314.159.265-ALPHA.3");

		this.writer = new MusicPackProjectWriterImpl();
		this.testOutputStream = new ByteArrayOutputStream();
	}

	@After
	public void cleanup() throws IOException {
		testOutputStream.close();
	}

	@Test(expected = NullPointerException.class)
	public void testWriteProjectNullProject() throws IOException {
		this.writer.writeMusicPackProject(null, this.testOutputStream);
	}

	@Test(expected = NullPointerException.class)
	public void testWriteProjectNullPath() throws IOException {
		EasyMock.resetToStrict(this.mockMusicPackProject);
		EasyMock.replay(this.mockMusicPackProject);

		this.writer.writeMusicPackProject(this.mockMusicPackProject, null);

		EasyMock.verify(this.mockMusicPackProject);
	}

	@Test
	public void testWriteMusicPackProject() throws IOException {
		EasyMock.expect(this.mockMusicPackProject.getName()).andReturn("TestProject").atLeastOnce();
		EasyMock.expect(this.mockMusicPackProject.getMusicPack()).andReturn(this.mockMusicPack).atLeastOnce();
		EasyMock.expect(this.mockMusicPackProject.getProperties()).andReturn(this.packProperties).atLeastOnce();

		EasyMock.expect(this.mockMusicPack.getTracks()).andReturn(this.tracks).atLeastOnce();

		EasyMock.replay(this.mockMusicPackProject);
		EasyMock.replay(this.mockMusicPack);

		this.writer.writeMusicPackProject(this.mockMusicPackProject, testOutputStream);

		this.checkWrittenMusicPackProject(new String(testOutputStream.toByteArray()));

		EasyMock.verify(this.mockMusicPackProject);
		EasyMock.verify(this.mockMusicPack);
	}

	private void checkWrittenMusicPackProject(String projectData) throws IOException {
		try (StringReader contentReader = new StringReader(projectData); JsonReader reader = new JsonReader(contentReader)) {
			reader.beginObject();
			Assert.assertEquals(MusicPackProjectWriterImpl.JSON_PROJECT, reader.nextName());
			reader.beginObject();
			Assert.assertEquals(MusicPackProjectWriterImpl.JSON_PROJECT_NAME, reader.nextName());
			Assert.assertEquals("TestProject", reader.nextString());
			Assert.assertEquals(MusicPackProjectWriterImpl.JSON_PROJECT_TRACKS, reader.nextName());
			reader.beginArray();
			checkWrittenTrack(reader, tracks.get(0));
			checkWrittenTrack(reader, tracks.get(1));
			reader.endArray();
			Assert.assertEquals(MusicPackProjectWriterImpl.JSON_PROJECT_PROPERTIES, reader.nextName());
			reader.beginArray();
			for (Map.Entry<Object, Object> entry : this.mockMusicPackProject.getProperties().entrySet()) {
				reader.beginObject();
				Assert.assertEquals(entry.getKey().toString(), reader.nextName());
				Assert.assertEquals(entry.getValue().toString(), reader.nextString());
				reader.endObject();
			}
			reader.endArray();
			reader.endObject();
			reader.endObject();
		}
	}

	private void checkWrittenTrack(JsonReader reader, Track comparison) throws IOException {
		reader.beginObject();
		Assert.assertEquals(MusicPackProjectWriterImpl.JSON_TRACK_PATH, reader.nextName());
		Assert.assertEquals(comparison.getTrackPath().toString(), reader.nextString());
		if (comparison.hasTitle()) {
			Assert.assertEquals(MusicPackProjectWriterImpl.JSON_TRACK_TITLE, reader.nextName());
			Assert.assertEquals(comparison.getTitle(), reader.nextString());
		}
		Assert.assertEquals(MusicPackProjectWriterImpl.JSON_TRACK_REGIONS, reader.nextName());
		reader.beginArray();
		for (Region region : comparison.getRegions()) {
			this.checkWrittenRegion(reader, region);
		}
		reader.endArray();
		checkStringArray(reader, MusicPackProjectWriterImpl.JSON_TRACK_AUTHORS, comparison.getAuthors());
		reader.endObject();
	}

	private void checkWrittenRegion(JsonReader reader, Region comparison) throws IOException {
		reader.beginObject();
		Assert.assertEquals(MusicPackProjectWriterImpl.JSON_REGION_NAME, reader.nextName());
		Assert.assertEquals(comparison.getName(), reader.nextString());
		checkStringArray(reader, MusicPackProjectWriterImpl.JSON_REGION_SUBREGIONS, comparison.getSubregions());
		checkStringArray(reader, MusicPackProjectWriterImpl.JSON_REGION_CATEGORIES, comparison.getCategories());
		if (comparison.getWeight() != null) {
			Assert.assertEquals(MusicPackProjectWriterImpl.JSON_REGION_WEIGHT, reader.nextName());
			Assert.assertEquals(comparison.getWeight().toString(), reader.nextString());
		}
		reader.endObject();
	}

	private void checkStringArray(JsonReader reader, String arrayName, List<String> values) throws IOException {
		Assert.assertEquals(arrayName, reader.nextName());
		reader.beginArray();
		for (int i = 0; i < values.size(); i++) {
			Assert.assertEquals(values.get(i), reader.nextString());
		}
		reader.endArray();
	}

}

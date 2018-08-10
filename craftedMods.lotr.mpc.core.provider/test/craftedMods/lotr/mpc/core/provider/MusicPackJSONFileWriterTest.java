package craftedMods.lotr.mpc.core.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.stream.JsonReader;

import craftedMods.lotr.mpc.core.api.MusicPackProjectExporter;
import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.lotr.mpc.core.base.DefaultRegion;
import craftedMods.lotr.mpc.core.base.DefaultTrack;

public class MusicPackJSONFileWriterTest {

	private MusicPackJSONFileWriter writer = new MusicPackJSONFileWriter();

	@Test
	public void testWriteJSONFile() throws Exception {
		DefaultTrack track1 = new DefaultTrack(Paths.get("path", "to", "track"), "Title",
				Arrays.asList(new DefaultRegion("all", Arrays.asList(), Arrays.asList(), null)),
				Arrays.asList("Author1", "Author2"));
		DefaultTrack track2 = new DefaultTrack(Paths.get("path", "to", "track23", "kl"), null,
				Arrays.asList(new DefaultRegion("all", Arrays.asList(), Arrays.asList(), null),
						new DefaultRegion("mordor", Arrays.asList("nanUngol", "nurn"), Arrays.asList("day"), null)),
				Arrays.asList("Author1"));
		DefaultTrack track3 = new DefaultTrack(Paths.get("path", "to2"), "Title",
				Arrays.asList(new DefaultRegion("all", Arrays.asList(), Arrays.asList(), 2.0f)), Arrays.asList());

		List<Track> tracksList = Arrays.asList(track1, track2, track3);

		checkMusicJSONFile(writer.writeJSONFile(tracksList), tracksList);
	}

	private void checkMusicJSONFile(byte[] data, List<Track> tracks) throws IOException {
		try (ByteArrayInputStream in = new ByteArrayInputStream(data);
				InputStreamReader bridge = new InputStreamReader(in);
				JsonReader reader = new JsonReader(bridge)) {
			reader.beginObject();
			Assert.assertEquals(MusicPackProjectExporter.JSON_TRACKS, reader.nextName());
			reader.beginArray();
			for (Track track : tracks) {
				checkWrittenTrack(reader, track);
			}
			reader.endArray();
			reader.endObject();
		}
	}

	private void checkWrittenTrack(JsonReader reader, Track comparison) throws IOException {
		reader.beginObject();
		Assert.assertEquals(MusicPackProjectExporter.JSON_TRACK_FILE_NAME, reader.nextName());
		Assert.assertEquals(comparison.getTrackPath().getFileName().toString(), reader.nextString());
		if (comparison.hasTitle()) {
			Assert.assertEquals(MusicPackProjectExporter.JSON_TRACK_TITLE, reader.nextName());
			Assert.assertEquals(comparison.getTitle(), reader.nextString());
		}
		Assert.assertEquals(MusicPackProjectExporter.JSON_TRACK_REGIONS, reader.nextName());
		reader.beginArray();
		for (Region region : comparison.getRegions()) {
			this.checkWrittenRegion(reader, region);
		}
		reader.endArray();
		checkStringArray(reader, MusicPackProjectExporter.JSON_TRACK_AUTHORS, comparison.getAuthors());
		reader.endObject();
	}

	private void checkWrittenRegion(JsonReader reader, Region comparison) throws IOException {
		reader.beginObject();
		Assert.assertEquals(MusicPackProjectExporter.JSON_REGION_NAME, reader.nextName());
		Assert.assertEquals(comparison.getName(), reader.nextString());
		checkStringArray(reader, MusicPackProjectExporter.JSON_REGION_SUB, comparison.getSubregions());
		checkStringArray(reader, MusicPackProjectExporter.JSON_REGION_CATEGORIES, comparison.getCategories());
		if (comparison.getWeight() != null) {
			Assert.assertEquals(MusicPackProjectExporter.JSON_REGION_WEIGHT, reader.nextName());
			Assert.assertEquals(comparison.getWeight().toString(), reader.nextString());
		}
		reader.endObject();
	}

	private void checkStringArray(JsonReader reader, String arrayName, List<String> values) throws IOException {
		if (!values.isEmpty()) {
			Assert.assertEquals(arrayName, reader.nextName());
			reader.beginArray();
			for (int i = 0; i < values.size(); i++) {
				Assert.assertEquals(values.get(i), reader.nextString());
			}
			reader.endArray();
		}
	}

}

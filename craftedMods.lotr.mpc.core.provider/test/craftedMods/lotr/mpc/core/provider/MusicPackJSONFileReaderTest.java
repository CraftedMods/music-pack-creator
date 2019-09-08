package craftedMods.lotr.mpc.core.provider;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.lotr.mpc.core.base.DefaultRegion;
import craftedMods.lotr.mpc.core.base.DefaultTrack;

public class MusicPackJSONFileReaderTest {

	private MusicPackJSONFileWriter writer = new MusicPackJSONFileWriter();
	private MusicPackJSONFileReader reader = new MusicPackJSONFileReader();

	@Test
	public void testWriteJSONFile() throws IOException {
		DefaultTrack track1 = new DefaultTrack("Track1", "Title",
				Arrays.asList(new DefaultRegion("all", Arrays.asList(), Arrays.asList(), null)),
				Arrays.asList("Author1", "Author2"));
		DefaultTrack track2 = new DefaultTrack("Track2", null,
				Arrays.asList(new DefaultRegion("all", Arrays.asList(), Arrays.asList(), null),
						new DefaultRegion("mordor", Arrays.asList("nanUngol", "nurn"), Arrays.asList("day"), null)),
				Arrays.asList("Author1"));
		DefaultTrack track3 = new DefaultTrack("Track3", "Title",
				Arrays.asList(new DefaultRegion("all", Arrays.asList(), Arrays.asList(), 2.0f)), Arrays.asList());

		List<Track> tracksList = Arrays.asList(track1, track2, track3);

		byte[] data = writer.writeJSONFile(tracksList);

		Assert.assertEquals(tracksList, reader.readJSONFile(data));
	}
}

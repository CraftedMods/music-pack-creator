package craftedMods.lotr.mpc.core.base;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import craftedMods.lotr.mpc.core.api.Region;

public class DefaultTrackTest {

	@Test
	public void testClone() {
		DefaultRegion region1 = new DefaultRegion("region1");
		DefaultRegion region2 = new DefaultRegion("region2");
		DefaultRegion region3 = new DefaultRegion("region3");

		DefaultTrack track = new DefaultTrack("track", null, Arrays.asList(region1, region2, region3),
				Arrays.asList("rerfd"));
		Collection<Region> regions = track.getRegions();
		Collection<String> authors = track.getAuthors();

		DefaultTrack track2 = track.clone();

		Assert.assertEquals(track, track2);
		Assert.assertFalse(track == track2);

		Assert.assertEquals(regions, track2.getRegions()); // Theoretically implied be the track comparison, but
															// if equals is wrong...
		Assert.assertEquals(authors, track2.getAuthors());

		Assert.assertFalse(regions == track2.getRegions());
		Assert.assertFalse(authors == track2.getAuthors());

		for (Region reg : track2.getRegions()) {
			Region comparison = reg.getName().equals("region1") ? region1
					: reg.getName().equals("region2") ? region2 : region3;
			Assert.assertEquals(comparison, reg);
			Assert.assertFalse(comparison == reg);
		}
	}

}

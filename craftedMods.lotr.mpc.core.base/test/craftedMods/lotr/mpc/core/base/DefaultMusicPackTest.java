package craftedMods.lotr.mpc.core.base;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import craftedMods.lotr.mpc.core.api.Track;

public class DefaultMusicPackTest {

	@Test
	public void testClone() {
		DefaultTrack track1 = new DefaultTrack("name");
		DefaultTrack track2 = new DefaultTrack("name2");

		DefaultMusicPack pack = new DefaultMusicPack(Arrays.asList(track1, track2));
		Collection<Track> tracks = pack.getTracks();

		DefaultMusicPack pack2 = pack.clone();

		Assert.assertEquals(pack, pack2);
		Assert.assertFalse(pack == pack2);

		Assert.assertEquals(tracks, pack2.getTracks()); // Theoretically implied be the pack comparison, but if equals
														// is wrong...
		Assert.assertFalse(tracks == pack2.getTracks());

		for (Track trck : pack2.getTracks()) {
			Track comparison = trck.getName().equals("name") ? track1 : track2;
			Assert.assertEquals(comparison, trck);
			Assert.assertFalse(comparison == trck);

		}
	}

}

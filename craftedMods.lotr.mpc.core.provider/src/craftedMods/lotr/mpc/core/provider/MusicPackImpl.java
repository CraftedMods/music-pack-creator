package craftedMods.lotr.mpc.core.provider;

import craftedMods.lotr.mpc.core.api.MusicPack;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.utils.data.CollectionUtils;
import craftedMods.utils.data.NonNullSet;

public class MusicPackImpl implements MusicPack {

	private NonNullSet<Track> tracksSet = CollectionUtils.createNonNullHashSet();

	@Override
	public NonNullSet<Track> getTracks() {
		return this.tracksSet;
	}

}

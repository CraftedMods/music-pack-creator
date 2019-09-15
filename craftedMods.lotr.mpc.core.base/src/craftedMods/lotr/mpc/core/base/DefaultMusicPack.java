package craftedMods.lotr.mpc.core.base;

import java.util.Collection;

import craftedMods.lotr.mpc.core.api.MusicPack;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.utils.data.CollectionUtils;
import craftedMods.utils.data.NonNullSet;

public class DefaultMusicPack implements MusicPack {

	private NonNullSet<Track> tracksSet = CollectionUtils.createNonNullHashSet();

	public DefaultMusicPack() {
	}

	public DefaultMusicPack(Collection<Track> tracks) {
		this.tracksSet.addAll(tracks);
	}

	@Override
	public NonNullSet<Track> getTracks() {
		return this.tracksSet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tracksSet == null) ? 0 : tracksSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultMusicPack other = (DefaultMusicPack) obj;
		if (tracksSet == null) {
			if (other.tracksSet != null)
				return false;
		} else if (!tracksSet.equals(other.tracksSet))
			return false;
		return true;
	}

}

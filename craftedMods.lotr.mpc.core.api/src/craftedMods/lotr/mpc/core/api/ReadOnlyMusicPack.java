package craftedMods.lotr.mpc.core.api;

import craftedMods.utils.data.NonNullSet;

public interface ReadOnlyMusicPack {
	
	public NonNullSet<? extends ReadOnlyTrack> getTracks();
	
}

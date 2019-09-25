package craftedMods.lotr.mpc.core.api;

import craftedMods.utils.data.NonNullSet;
import craftedMods.utils.lang.CloneableObject;

public interface ReadOnlyMusicPack extends CloneableObject<ReadOnlyMusicPack>{
	
	public NonNullSet<? extends ReadOnlyTrack> getTracks();
	
}

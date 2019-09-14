package craftedMods.lotr.mpc.core.api;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.utils.data.NonNullSet;

@ProviderType
public interface MusicPack extends ReadOnlyMusicPack {

	@Override
	public NonNullSet<Track> getTracks();

}

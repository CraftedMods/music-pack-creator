package craftedMods.lotr.mpc.core.api;

import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface MusicPack {

	public Set<Track> getTracks();

}

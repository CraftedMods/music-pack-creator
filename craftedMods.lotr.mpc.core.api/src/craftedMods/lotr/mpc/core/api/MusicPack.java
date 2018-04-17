package craftedMods.lotr.mpc.core.api;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface MusicPack {

	public List<Track> getTracks();

}

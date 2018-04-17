package craftedMods.lotr.mpc.core.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface MusicPackProjectFactory {
	
	public MusicPackProject createMusicPackProjectInstance(String name);

}

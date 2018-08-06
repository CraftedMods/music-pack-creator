package craftedMods.lotr.mpc.core.api;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.versionChecker.api.SemanticVersion;

@ProviderType
public interface MusicPackCreator {

	public SemanticVersion getVersion();

}

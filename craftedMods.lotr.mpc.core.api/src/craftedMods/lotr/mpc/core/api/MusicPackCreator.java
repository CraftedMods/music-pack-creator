package craftedMods.lotr.mpc.core.api;

import java.nio.file.Path;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.versionChecker.api.SemanticVersion;

@ProviderType
public interface MusicPackCreator {

	public SemanticVersion getVersion();

	public Path getWorkspaceRoot();

}

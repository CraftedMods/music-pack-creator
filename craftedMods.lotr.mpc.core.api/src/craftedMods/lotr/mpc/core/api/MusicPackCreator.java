package craftedMods.lotr.mpc.core.api;

import java.nio.file.Path;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface MusicPackCreator {

	public String getVersion();

	public boolean isPreRelease();

	public Path getWorkspaceRoot();

}

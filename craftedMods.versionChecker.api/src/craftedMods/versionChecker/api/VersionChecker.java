package craftedMods.versionChecker.api;

import java.net.URL;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface VersionChecker {

	public RemoteVersion retrieveRemoteVersion(URL versionFileURL);

	public boolean isNewVersionAvailable(SemanticVersion localVersion, RemoteVersion remoteVersion);

}

package craftedMods.versionChecker.api;

import java.net.URL;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface RemoteVersion {
	
		public SemanticVersion getRemoteVersion();

		public URL getDownloadURL();

		public URL getChangelogURL();

}

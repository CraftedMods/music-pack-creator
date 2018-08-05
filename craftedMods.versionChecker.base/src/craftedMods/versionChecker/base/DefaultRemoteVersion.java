package craftedMods.versionChecker.base;

import java.net.URL;

import craftedMods.versionChecker.api.RemoteVersion;
import craftedMods.versionChecker.api.SemanticVersion;

public class DefaultRemoteVersion implements RemoteVersion {

	private final SemanticVersion remoteVersion;
	private final URL changelogURL;
	private final URL downloadURL;

	public DefaultRemoteVersion(SemanticVersion remoteVersion, URL downloadURL, URL changelogURL) {
		this.remoteVersion = remoteVersion;
		this.downloadURL = downloadURL;
		this.changelogURL = changelogURL;
	}

	public SemanticVersion getRemoteVersion() {
		return this.remoteVersion;
	}

	public URL getDownloadURL() {
		return this.downloadURL;
	}

	public URL getChangelogURL() {
		return this.changelogURL;
	}

}

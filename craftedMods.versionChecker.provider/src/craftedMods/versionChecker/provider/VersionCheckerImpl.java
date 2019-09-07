package craftedMods.versionChecker.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import craftedMods.versionChecker.api.RemoteVersion;
import craftedMods.versionChecker.api.SemanticVersion;
import craftedMods.versionChecker.api.VersionChecker;
import craftedMods.versionChecker.base.DefaultRemoteVersion;
import craftedMods.versionChecker.base.DefaultSemanticVersion;

@Component
public class VersionCheckerImpl implements VersionChecker {

	@Reference(service=LoggerFactory.class)
	private Logger logger;

	@Override
	public RemoteVersion retrieveRemoteVersion(URL versionFileURL) {
		Objects.requireNonNull(versionFileURL);
		if (this.ping(versionFileURL)) {
			try {
				return this.parseVersionFile(this.downloadVersionFile(versionFileURL));
			} catch (IOException e) {
				logger.error("Couldn't download the version file \"%s\"", versionFileURL.toString(), e);
			} catch (Exception e) {
				logger.error("Couldn't parse the contents of the version file \"%s\"",
						versionFileURL.toString(), e);
			}
		}
		return null;
	}

	private boolean ping(URL versionFileURL) {
		if (versionFileURL != null) {
			try {
				URLConnection conn = versionFileURL.openConnection();
				conn.setConnectTimeout(2000);
				conn.connect();
				return true;
			} catch (IOException e) {
				this.logger.error("Cannot connect to the version file \"%s\"", versionFileURL.toString(), e);
			}
		}
		return false;
	}

	private String downloadVersionFile(URL versionFileURL) throws IOException {
		try (InputStream stream = versionFileURL.openStream();
				InputStreamReader bridge = new InputStreamReader(stream);
				BufferedReader reader = new BufferedReader(bridge)) {
			return reader.readLine();
		}
	}

	private RemoteVersion parseVersionFile(String versionString) throws MalformedURLException {
		if (versionString != null) {

			SemanticVersion remoteVersion = null;
			URL downloadURL = null;
			URL changelogURL = null;

			String[] parts = versionString.split("\\|");

			remoteVersion = DefaultSemanticVersion.of(parts[0]);
			if (parts.length > 1 && !parts[1].trim().isEmpty()) {
				downloadURL = new URL(parts[1]);
			}
			if (parts.length > 2 && !parts[2].trim().isEmpty()) {
				changelogURL = new URL(parts[2]);
			}

			return new DefaultRemoteVersion(remoteVersion, downloadURL, changelogURL);
		}
		return null;
	}

	@Override
	public boolean isNewVersionAvailable(SemanticVersion localVersion, RemoteVersion remoteVersion) {
		Objects.requireNonNull(localVersion);
		return remoteVersion != null && localVersion.compareTo(remoteVersion.getRemoteVersion()) < 0;
	}

}

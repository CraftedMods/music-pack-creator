package craftedMods.preferences.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Objects;

import craftedMods.fileManager.api.FileManager;
import craftedMods.preferences.api.Preferences;
import craftedMods.utils.data.ExtendedProperties;

public class PreferencesImpl extends ExtendedProperties implements Preferences {

	private static final long serialVersionUID = -5861050820611013133L;

	private final String pid;
	private Path configFile;
	private final FileManager fileManager;

	public PreferencesImpl(String pid, Path configFile, FileManager fileManager) {
		Objects.requireNonNull(pid);
		Objects.requireNonNull(configFile);
		Objects.requireNonNull(fileManager);
		this.pid = pid;
		this.configFile = configFile;
		this.fileManager = fileManager;
	}

	@Override
	public String getPID() {
		return pid;
	}

	public Path getConfigFile() {
		return configFile;
	}

	public void setConfigFile(Path configFile) {
		Objects.requireNonNull(configFile);
		this.configFile = configFile;
	}

	@Override
	public void sync() throws IOException {
		this.clear();
		try (InputStream in = fileManager.newInputStream(configFile)) {
			this.load(in);
		}
	}

	@Override
	public void flush() throws IOException {
		try (OutputStream out = fileManager.newOutputStream(configFile)) {
			this.store(out, "The properties are stored under the PID " + pid);
		}
	}

}

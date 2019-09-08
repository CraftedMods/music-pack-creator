package craftedMods.preferences.provider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.ServiceException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.FormatterLogger;
import org.osgi.service.log.LoggerFactory;

import craftedMods.fileManager.api.FileManager;
import craftedMods.preferences.api.Preferences;
import craftedMods.preferences.api.PreferencesManager;

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE)
public class PreferencesManagerImpl implements PreferencesManager {

	public @interface Configuration {
		String configurationDirectory();
	}

	@Reference
	private FileManager fileManager;

	@Reference(service=LoggerFactory.class)
	private FormatterLogger logger;

	private String configDirString;

	private Path configDir;

	private Map<String, PreferencesImpl> managedPreferences = new HashMap<>();

	@Activate
	public void onActivate(Configuration config) throws IOException {
		configDirString = config.configurationDirectory();
		configDir = fileManager.getPathAndCreateDir(configDirString);
		this.logger.info("Configuration directory: \"%s\"", configDir.toString());
	}

	@Modified
	public void onModify(Configuration config) throws IOException {
		if (!config.configurationDirectory().equals(configDirString)) {
			String oldDir = configDir.toString();
			configDirString = config.configurationDirectory();
			configDir = fileManager.getPathAndCreateDir(configDirString);
			for (PreferencesImpl pref : managedPreferences.values()) {
				try {
					pref.setConfigFile(getAndCreateConfigFile(pref.getPID()));
					pref.flush();
				} catch (IOException e) {
					logger.error(
							"Couldn't move the preferences for the PID \"%s\" to the new location", pref.getPID(), e);
				}
			}
			this.logger.info(
					"The configuration directory was moved from \"%s\" to \"%s\"", oldDir, configDir.toString());
		}
	}

	public String getConfigDirString() {
		return configDirString;
	}

	@Deactivate
	public void onDeactivate() {
		managedPreferences.values().forEach(pref -> {
			try {
				pref.flush();
			} catch (IOException e) {
				logger.error("Couldn't save the preferences for the PID \"%s\"", pref.getPID(), e);
			}
		});
		managedPreferences.clear();
	}

	@Override
	public Preferences getPreferences(String pid) {
		if (!managedPreferences.containsKey(pid)) {
			Path configFile = null;
			try {
				configFile = getAndCreateConfigFile(pid);
			} catch (IOException e) {
				throw new ServiceException(
						String.format("Couldn't create the configuration file for the PID \"%s\"", pid), e);
			}

			PreferencesImpl impl = new PreferencesImpl(pid, configFile, fileManager);

			try {
				impl.sync();
			} catch (IOException e) {
				throw new ServiceException(
						String.format("Couldn't load the configuration entries for the PID \"%s\"", pid), e);
			}

			managedPreferences.put(pid, impl);

		}
		return managedPreferences.get(pid);
	}

	public Map<String, PreferencesImpl> getManagedPreferences() {
		return managedPreferences;
	}

	private Path getAndCreateConfigFile(String pid) throws IOException {
		return fileManager.getPathAndCreateFile(configDir.toString(),
				pid.replace(".", fileManager.getSeparator()) + ".properties");
	}

}

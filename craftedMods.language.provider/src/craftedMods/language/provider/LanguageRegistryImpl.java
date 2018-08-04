package craftedMods.language.provider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import craftedMods.language.api.LanguageRegistry;

@Component(configurationPid = LanguageRegistry.LANGUAGE_CONFIG_PID)
public class LanguageRegistryImpl implements LanguageRegistry {

	public @interface Config {
		String defaultLanguage() default "en-US";

		String currentLanguage() default LanguageRegistry.SYSTEM_LANGUAGE;
	}

	private Locale defaultLanguage;

	private Locale currentLanguage;

	private Map<Locale, ResourceBundle> entries;

	@Reference
	private ResourceBundleLoader resourceBundleLoader;

	@Reference
	private LogService logger;

	@Activate
	public void onActivate(Config config) {
		entries = new HashMap<>();
		defaultLanguage = parseLocale(config.defaultLanguage());
		currentLanguage = parseLocale(config.currentLanguage());
		this.reload();
	}

	@Modified
	public void onModify(Config config) {
		Locale newDefaultLanguage = parseLocale(config.defaultLanguage());
		Locale newCurrentLanguage = parseLocale(config.currentLanguage());
		if (!newDefaultLanguage.equals(defaultLanguage) || !newCurrentLanguage.equals(currentLanguage)) {
			defaultLanguage = newDefaultLanguage;
			currentLanguage = newCurrentLanguage;
			this.reload();
		}
	}

	private Locale parseLocale(String languageTag) {
		if (languageTag.equals(LanguageRegistry.SYSTEM_LANGUAGE)) {
			return Locale.getDefault();
		}
		return Locale.forLanguageTag(languageTag);
	}

	@Deactivate
	public void onDeactivate() {
		entries.clear();
	}

	@Override
	public Locale getDefaultLanguage() {
		return defaultLanguage;
	}

	@Override
	public Locale getCurrentLanguage() {
		return currentLanguage;
	}

	@Override
	public String getDefaultValue(String key, Object... params) {
		String defaultEntry = this.getStringFromBundle(this.entries.get(this.defaultLanguage), key);
		if (defaultEntry.equals(key)) {
			return key;
		}
		return String.format(defaultEntry, params);
	}

	@Override
	public String getLocalizedValue(String key, Object... params) {
		ResourceBundle currentBundle = this.entries.get(this.currentLanguage);
		String currentEntry = this.getStringFromBundle(currentBundle, key);
		if (currentEntry.equals(key))
			return getDefaultValue(key, params);
		return String.format(currentEntry, params);
	}

	public Map<Locale, ResourceBundle> getEntries() {
		return entries;
	}

	private String getStringFromBundle(ResourceBundle bundle, String key) {
		String ret = key;
		if (bundle != null) {
			try {
				ret = bundle.getString(key);
			} catch (MissingResourceException e) {
			}
		}
		return ret;
	}

	private void reload() {
		this.loadResourceBundle(this.defaultLanguage, false, true);
		this.loadResourceBundle(this.currentLanguage, true, false);
	}

	private void loadResourceBundle(Locale locale, boolean searchSimilar, boolean isDefault) {
		ResourceBundle bundle = resourceBundleLoader.loadResourceBunde(locale, searchSimilar);
		if (bundle != null) {
			this.entries.put(locale, bundle);
			this.logger.log(LogService.LOG_INFO,
					String.format("Successfully loaded %d language entries for the %s locale (%s)",
							bundle.keySet().size(), isDefault ? "default" : "current", locale.toString()));
			return;
		}
		this.logger.log(isDefault ? LogService.LOG_ERROR : LogService.LOG_WARNING,
				String.format("The language entries for the %s locale (%s) couldn't be loaded",
						isDefault ? "default" : "current", locale.toString()));
	}
}

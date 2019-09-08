package craftedMods.language.provider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.FormatterLogger;
import org.osgi.service.log.LoggerFactory;

import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.eventManager.base.DefaultWriteableEventProperties;
import craftedMods.language.api.LanguageRegistry;
import craftedMods.preferences.api.Preferences;
import craftedMods.preferences.api.PreferencesManager;

@Component
public class LanguageRegistryImpl implements LanguageRegistry {

	public static final String CONFIG_PID = LanguageRegistryImpl.class.getName();

	public static final String DEFAULT_LANGUAGE_KEY = "defaultLanguage";
	public static final String CURRENT_LANGUAGE_KEY = "currentLanguage";

	private Locale defaultLanguage;

	private Locale currentLanguage;

	private Map<Locale, ResourceBundle> entries;

	@Reference
	private PreferencesManager preferences;

	private Preferences prefs;

	@Reference
	private ResourceBundleLoader resourceBundleLoader;

	@Reference(service = LoggerFactory.class)
	private FormatterLogger logger;

	@Reference
	private EventManager eventManager;

	@Activate
	public void onActivate() {
		entries = new HashMap<>();

		prefs = preferences.getPreferences(LanguageRegistryImpl.CONFIG_PID);
		defaultLanguage = Locale.forLanguageTag(prefs.getString(DEFAULT_LANGUAGE_KEY, "en-US"));
		currentLanguage = Locale
				.forLanguageTag(prefs.getString(CURRENT_LANGUAGE_KEY, Locale.getDefault().toLanguageTag()));

		this.reload();

		fireDefaultLanguageChangedEvent(null);
		fireCurrentLanguageChangedEvent(null);
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
	public boolean setDefaultLanguage(Locale defaultLanguage) {
		Objects.requireNonNull(defaultLanguage);
		if (!defaultLanguage.equals(this.defaultLanguage)) {
			Locale oldLanguage = this.defaultLanguage;

			this.defaultLanguage = defaultLanguage;
			prefs.setString(DEFAULT_LANGUAGE_KEY, this.defaultLanguage.toLanguageTag());
			this.loadResourceBundle(this.defaultLanguage, false, true);

			fireDefaultLanguageChangedEvent(oldLanguage);

			return true;
		}
		return false;
	}

	private void fireDefaultLanguageChangedEvent(Locale oldLanguage) {
		WriteableEventProperties properties = new DefaultWriteableEventProperties();
		properties.put(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_OLD_LANGUAGE, oldLanguage);
		properties.put(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_NEW_LANGUAGE, this.defaultLanguage);

		eventManager.dispatchEvent(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_EVENT, properties);
	}

	@Override
	public Locale getCurrentLanguage() {
		return currentLanguage;
	}

	@Override
	public boolean setCurrentLanguage(Locale currentLanguage) {
		Objects.requireNonNull(currentLanguage);
		if (!currentLanguage.equals(this.currentLanguage)) {
			Locale oldLanguage = this.currentLanguage;

			this.currentLanguage = currentLanguage;
			prefs.setString(CURRENT_LANGUAGE_KEY, this.currentLanguage.toLanguageTag());
			this.loadResourceBundle(this.currentLanguage, true, false);

			fireCurrentLanguageChangedEvent(oldLanguage);

			return true;
		}
		return false;
	}

	private void fireCurrentLanguageChangedEvent(Locale oldLanguage) {
		WriteableEventProperties properties = new DefaultWriteableEventProperties();
		properties.put(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_OLD_LANGUAGE, oldLanguage);
		properties.put(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_NEW_LANGUAGE, this.currentLanguage);

		eventManager.dispatchEvent(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_EVENT, properties);
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
			this.logger.info("Successfully loaded %d language entries for the %s locale (%s)", bundle.keySet().size(),
					isDefault ? "default" : "current", locale.toString());
			return;
		}

		String message = String.format("The language entries for the %s locale (%s) couldn't be loaded",
				isDefault ? "default" : "current", locale.toString());

		if (isDefault) {
			this.logger.error(message);
		} else {
			this.logger.warn(message);
		}
	}
}

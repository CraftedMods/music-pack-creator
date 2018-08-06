package craftedMods.language.api;

import java.util.Locale;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface LanguageRegistry {

	public static final String CONFIG_PID = LanguageRegistry.class.getName();

	public static final String DEFAULT_LANGUAGE_KEY = "defaultLanguage";
	public static final String CURRENT_LANGUAGE_KEY = "currentLanguage";

	public Locale getDefaultLanguage();

	public boolean setDefaultLanguage(Locale defaultLanguage);

	public Locale getCurrentLanguage();

	public boolean setCurrentLanguage(Locale currentLanguage);

	public String getLocalizedValue(String key, Object... params);

	public String getDefaultValue(String key, Object... params);

}

package craftedMods.language.api;

import java.util.Locale;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface LanguageRegistry {

	public static final String LANGUAGE_CONFIG_PID = "craftedMods.language.api.LanguageRegistry";
	public static final String CURRENT_LANGUAGE_KEY = "currentLanguage";
	public static final String DEFAULT_LANGUAGE_KEY = "defaultLanguage";

	public static final String SYSTEM_LANGUAGE = "$SYS_LANG$";

	public Locale getDefaultLanguage();

	public Locale getCurrentLanguage();

	public String getLocalizedValue(String key, Object... params);

	public String getDefaultValue(String key, Object... params);

}

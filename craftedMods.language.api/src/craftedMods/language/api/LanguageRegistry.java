package craftedMods.language.api;

import java.util.Locale;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface LanguageRegistry {

	public Locale getDefaultLanguage();

	public boolean setDefaultLanguage(Locale defaultLanguage);

	public Locale getCurrentLanguage();

	public boolean setCurrentLanguage(Locale currentLanguage);

	public String getLocalizedValue(String key, Object... params);

	public String getDefaultValue(String key, Object... params);

}

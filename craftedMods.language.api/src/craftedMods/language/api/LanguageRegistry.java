package craftedMods.language.api;

import java.util.Locale;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.utils.data.TypedPropertyKey;

@ProviderType
public interface LanguageRegistry {

	public static EventInfo DEFAULT_LANGUAGE_CHANGED_EVENT = new DefaultEventInfo(LanguageRegistry.class,
			"DEFAULT_LANGUAGE_CHANGED");

	public static EventInfo CURRENT_LANGUAGE_CHANGED_EVENT = new DefaultEventInfo(LanguageRegistry.class,
			"CURRENT_LANGUAGE_CHANGED");

	public static TypedPropertyKey<Locale> DEFAULT_LANGUAGE_CHANGED_OLD_LANGUAGE = TypedPropertyKey
			.createPropertyKey(Locale.class);
	public static TypedPropertyKey<Locale> DEFAULT_LANGUAGE_CHANGED_NEW_LANGUAGE = TypedPropertyKey
			.createPropertyKey(Locale.class);

	public static TypedPropertyKey<Locale> CURRENT_LANGUAGE_CHANGED_OLD_LANGUAGE = TypedPropertyKey
			.createPropertyKey(Locale.class);
	public static TypedPropertyKey<Locale> CURRENT_LANGUAGE_CHANGED_NEW_LANGUAGE = TypedPropertyKey
			.createPropertyKey(Locale.class);

	public Locale getDefaultLanguage();

	public boolean setDefaultLanguage(Locale defaultLanguage);

	public Locale getCurrentLanguage();

	public boolean setCurrentLanguage(Locale currentLanguage);

	public String getLocalizedValue(String key, Object... params);

	public String getDefaultValue(String key, Object... params);

}

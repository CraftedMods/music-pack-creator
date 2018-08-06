package craftedMods.preferences.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface PreferencesManager {

	public Preferences getPreferences(String pid);

}

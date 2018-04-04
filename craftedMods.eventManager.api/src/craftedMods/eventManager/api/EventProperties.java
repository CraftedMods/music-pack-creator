package craftedMods.eventManager.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface EventProperties {

	public <T> boolean containsProperty(EventPropertyKey<T> property);

	public <T> T getProperty(EventPropertyKey<T> property);

}

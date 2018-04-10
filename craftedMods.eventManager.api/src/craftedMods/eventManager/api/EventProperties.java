package craftedMods.eventManager.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface EventProperties {

	public boolean isEmpty();

	public <T> boolean containsProperty(PropertyKey<T> key);

	public <T> T getProperty(PropertyKey<T> key);

}

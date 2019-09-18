package craftedMods.utils.data;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface ReadOnlyTypedProperties {

	public boolean isEmpty();

	public <T> boolean containsProperty(TypedPropertyKey<T> key);

	public <T> T getProperty(TypedPropertyKey<T> key);

}

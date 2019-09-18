package craftedMods.utils.data;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface TypedProperties extends ReadOnlyTypedProperties {

	public <T> T put(TypedPropertyKey<T> key, T value);

	public void clear();
}

package craftedMods.eventManager.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface WriteableEventProperties extends EventProperties {

	public boolean isLocked();

	public boolean lock();

	public <T> T put(PropertyKey<T> key, T value);

	public void clear();

}

package craftedMods.eventManager.api;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.utils.data.TypedProperties;

@ProviderType
public interface WriteableEventProperties extends TypedProperties {

	public boolean isLocked();

	public boolean lock();

}

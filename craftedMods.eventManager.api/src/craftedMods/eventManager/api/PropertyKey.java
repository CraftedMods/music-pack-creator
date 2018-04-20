package craftedMods.eventManager.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface PropertyKey<T> {

	public Long getUniquePropertyIdentifier();

	public Class<T> getPropertyType();

}

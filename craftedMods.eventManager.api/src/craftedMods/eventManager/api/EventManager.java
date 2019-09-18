package craftedMods.eventManager.api;

import java.util.Collection;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.utils.data.ReadOnlyTypedProperties;

@ProviderType
public interface EventManager {

	public Collection<ReadOnlyTypedProperties> dispatchEvent(EventInfo eventInfo, WriteableEventProperties properties);// The event properties will be locked. The returned collection is never null.

	public Collection<ReadOnlyTypedProperties> dispatchEvent(EventInfo eventInfo);

	public Collection<ReadOnlyTypedProperties> dispatchEvent(EventInfo eventInfo, WriteableEventProperties properties, EventDispatchPolicy policy);

	public Collection<ReadOnlyTypedProperties> dispatchEvent(EventInfo eventInfo, EventDispatchPolicy policy);

}

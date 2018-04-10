package craftedMods.eventManager.api;

import java.util.Collection;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface EventManager {
	
	/*
	 * TODO: Only synchronous dispatched events support event results. The returned collection is never null.
	 */

	public Collection<EventProperties> dispatchEvent(EventInfo eventInfo, WriteableEventProperties properties);// The event properties will be locked

	public Collection<EventProperties> dispatchEvent(EventInfo eventInfo);

	public Collection<EventProperties> dispatchEvent(EventInfo eventInfo, WriteableEventProperties properties, EventDispatchPolicy policy);

	public Collection<EventProperties> dispatchEvent(EventInfo eventInfo, EventDispatchPolicy policy);

}

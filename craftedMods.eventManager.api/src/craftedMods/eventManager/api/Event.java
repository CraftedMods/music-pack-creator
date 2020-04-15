package craftedMods.eventManager.api;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.utils.data.*;

@ProviderType
public interface Event {

	public EventInfo getEventInfo(); // Can (but don't has to) be the same instance as provided by the dispatcher. The event dispatch policy is either synchronous or asynchronous.

	public ReadOnlyTypedProperties getEventProperties();

	public boolean matches(EventInfo eventInfo);

	public LockableTypedProperties getEventResults();// TODO: Only supported for synchronous dispatched events

}

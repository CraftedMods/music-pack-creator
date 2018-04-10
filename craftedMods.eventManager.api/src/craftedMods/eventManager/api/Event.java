package craftedMods.eventManager.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Event {

	public EventInfo getEventInfo(); // Can (but don't has to) be the same instance as provided by the dispatcher. The event dispatch policy is either synchronous or asynchronous.

	public EventProperties getEventProperties();

	public boolean matches(EventInfo eventInfo);

	public WriteableEventProperties getEventResults();// TODO: Only supported for synchronous dispatched events

}

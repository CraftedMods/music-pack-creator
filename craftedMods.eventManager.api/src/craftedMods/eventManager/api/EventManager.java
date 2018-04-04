package craftedMods.eventManager.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface EventManager {

	public void dispatchEvent(EventInfo eventInfo, WriteableEventProperties properties);// The event properties will be locked

	public void dispatchEvent(EventInfo eventInfo);

	public void dispatchEvent(EventInfo eventInfo, WriteableEventProperties properties, EventDispatchPolicy policy);

	public void dispatchEvent(EventInfo eventInfo, EventDispatchPolicy policy);

}

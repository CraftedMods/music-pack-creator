package craftedMods.eventManager.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface EventInfo {

	public String getTopic();

	public EventDispatchPolicy getEventDispatchPolicy(); // Has to be synchronous or asynchronous

}

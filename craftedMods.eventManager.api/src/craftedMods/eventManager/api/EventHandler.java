package craftedMods.eventManager.api;

import java.util.Map;

import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface EventHandler {

	public Map<EventInfo, EventHandlerPolicy> getSupportedEvents();

	public void handleEvent(Event event);

}

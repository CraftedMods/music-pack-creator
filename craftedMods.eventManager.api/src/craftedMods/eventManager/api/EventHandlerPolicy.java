package craftedMods.eventManager.api;

import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public enum EventHandlerPolicy {

	SYNCHRONOUS, ASYNCHRONOUS, NOT_SPECIFIED;

}

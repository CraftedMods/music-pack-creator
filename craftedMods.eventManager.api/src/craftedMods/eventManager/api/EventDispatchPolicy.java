package craftedMods.eventManager.api;

import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public enum EventDispatchPolicy {

	SYNCHRONOUS, ASYNCHRONOUS, HANDLER, NOT_SPECIFIED;

}

package craftedMods.eventManager.provider;

import craftedMods.eventManager.api.*;

public class EventImpl implements Event {

	private final EventInfo eventInfo;
	private final EventProperties eventProperties;

	public EventImpl(EventInfo eventInfo, EventProperties eventProperties) {
		this.eventInfo = eventInfo;
		this.eventProperties = eventProperties;
	}

	@Override
	public EventInfo getEventInfo() {
		return this.eventInfo;
	}

	@Override
	public EventProperties getEventProperties() {
		return this.eventProperties;
	}

	@Override
	public boolean matches(EventInfo eventInfo) {
		return this.eventInfo.getTopic().equals(eventInfo.getTopic());
	}

}

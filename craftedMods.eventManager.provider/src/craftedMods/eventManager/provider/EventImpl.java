package craftedMods.eventManager.provider;

import craftedMods.eventManager.api.*;
import craftedMods.eventManager.base.DefaultWriteableEventProperties;
import craftedMods.utils.data.ReadOnlyTypedProperties;

public class EventImpl implements Event {

	private final EventInfo eventInfo;
	private final ReadOnlyTypedProperties eventProperties;
	private WriteableEventProperties eventResults = new DefaultWriteableEventProperties();

	public EventImpl(EventInfo eventInfo, ReadOnlyTypedProperties eventProperties) {
		this.eventInfo = eventInfo;
		this.eventProperties = eventProperties;
	}

	@Override
	public EventInfo getEventInfo() {
		return this.eventInfo;
	}

	@Override
	public ReadOnlyTypedProperties getEventProperties() {
		return this.eventProperties;
	}

	@Override
	public boolean matches(EventInfo eventInfo) {
		return this.eventInfo.getTopic().equals(eventInfo.getTopic());
	}

	void recreateEventResults() {
		this.eventResults = new DefaultWriteableEventProperties();
	}

	@Override
	public WriteableEventProperties getEventResults() {
		return this.eventResults;
	}

}

package craftedMods.eventManager.base;

import craftedMods.eventManager.api.*;

public class DefaultEventInfo implements EventInfo {

	private final String eventTopic;
	private final EventDispatchPolicy eventDeliverPolicy;

	public DefaultEventInfo(Class<?> prefix, String suffix) {
		this(prefix, suffix, EventDispatchPolicy.HANDLER);
	}

	public DefaultEventInfo(String eventTopic) {
		this(eventTopic, EventDispatchPolicy.HANDLER);
	}

	public DefaultEventInfo(Class<?> prefix, String suffix, EventDispatchPolicy eventDeliverPolicy) {
		this(getEventTopicFromClass(prefix, suffix), eventDeliverPolicy);
	}

	public DefaultEventInfo(String eventTopic, EventDispatchPolicy eventDeliverPolicy) {
		this.eventTopic = eventTopic;
		this.eventDeliverPolicy = eventDeliverPolicy;
	}

	@Override
	public String getTopic() {
		return this.eventTopic;
	}

	@Override
	public EventDispatchPolicy getEventDispatchPolicy() {
		return this.eventDeliverPolicy;
	}

	public static String getEventTopicFromClass(Class<?> prefix, String suffix) {
		return prefix.getName().replace(".", "/").concat("/" + suffix);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.eventTopic == null ? 0 : this.eventTopic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		DefaultEventInfo other = (DefaultEventInfo) obj;
		if (this.eventTopic == null) {
			if (other.eventTopic != null) return false;
		} else if (!this.eventTopic.equals(other.eventTopic)) return false;
		return true;
	}

}

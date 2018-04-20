package craftedMods.eventManager.base;

import java.util.*;

import craftedMods.eventManager.api.*;

public class EventUtils {

	@SafeVarargs
	public static Map<EventInfo, EventHandlerPolicy> getSupportedEvents(Map.Entry<EventInfo, EventHandlerPolicy>... entries) {
		Map<EventInfo, EventHandlerPolicy> ret = new HashMap<>();
		for (Map.Entry<EventInfo, EventHandlerPolicy> entry : entries)
			ret.put(entry.getKey(), entry.getValue());
		return ret;
	}

	public static Map<EventInfo, EventHandlerPolicy> getSupportedEvents(EventInfo... eventInfo) {
		return EventUtils.getSupportedEvents(EventHandlerPolicy.NOT_SPECIFIED, eventInfo);
	}

	public static Map<EventInfo, EventHandlerPolicy> getSupportedEvents(EventHandlerPolicy policy, EventInfo... eventInfo) {
		Map<EventInfo, EventHandlerPolicy> ret = new HashMap<>();
		for (EventInfo info : eventInfo)
			ret.put(info, policy);
		return ret;
	}

}

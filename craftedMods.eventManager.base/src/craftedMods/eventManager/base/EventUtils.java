package craftedMods.eventManager.base;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import craftedMods.eventManager.api.EventDispatchPolicy;
import craftedMods.eventManager.api.EventHandlerPolicy;
import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.utils.data.ReadOnlyTypedProperties;
import craftedMods.utils.data.TypedPropertyKey;

public class EventUtils {

	@SafeVarargs
	public static Map<EventInfo, EventHandlerPolicy> getSupportedEvents(
			Map.Entry<EventInfo, EventHandlerPolicy>... entries) {
		Map<EventInfo, EventHandlerPolicy> ret = new HashMap<>();
		for (Map.Entry<EventInfo, EventHandlerPolicy> entry : entries)
			ret.put(entry.getKey(), entry.getValue());
		return ret;
	}

	public static Map<EventInfo, EventHandlerPolicy> getSupportedEvents(EventInfo... eventInfo) {
		return EventUtils.getSupportedEvents(EventHandlerPolicy.NOT_SPECIFIED, eventInfo);
	}

	public static Map<EventInfo, EventHandlerPolicy> getSupportedEvents(EventHandlerPolicy policy,
			EventInfo... eventInfo) {
		Map<EventInfo, EventHandlerPolicy> ret = new HashMap<>();
		for (EventInfo info : eventInfo)
			ret.put(info, policy);
		return ret;
	}

	public static boolean proceed(Collection<ReadOnlyTypedProperties> results,
			TypedPropertyKey<Boolean> proceedProperty) {
		return proceed(results, proceedProperty, true);
	}

	public static boolean proceed(Collection<ReadOnlyTypedProperties> results,
			TypedPropertyKey<Boolean> proceedProperty, boolean defaultValue) {
		Objects.requireNonNull(results);
		Objects.requireNonNull(proceedProperty);
		return results.isEmpty() ? defaultValue : results.stream().map(result -> {
			return Boolean.valueOf(
					!result.containsProperty(proceedProperty) ? defaultValue : result.getProperty(proceedProperty));
		}).allMatch(proceed -> proceed.booleanValue());
	}

	public static <T> Collection<ReadOnlyTypedProperties> dispatchEvent(EventManager manager, EventInfo info,
			TypedPropertyKey<T> key, T value) {
		return dispatchEvent(manager, info, key, value, null);
	}

	public static <T> Collection<ReadOnlyTypedProperties> dispatchEvent(EventManager manager, EventInfo info,
			TypedPropertyKey<T> key, T value, EventDispatchPolicy policy) {
		Objects.requireNonNull(manager);
		Objects.requireNonNull(info);
		Objects.requireNonNull(key);
		WriteableEventProperties properties = new DefaultWriteableEventProperties();
		properties.put(key, value);
		return policy == null ? manager.dispatchEvent(info, properties)
				: manager.dispatchEvent(info, properties, policy);
	}

}

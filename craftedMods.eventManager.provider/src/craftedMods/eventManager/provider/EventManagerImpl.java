package craftedMods.eventManager.provider;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

import org.osgi.service.component.annotations.*;
import org.osgi.service.log.LogService;

import craftedMods.eventManager.api.*;
import craftedMods.eventManager.base.*;

@Component(scope = ServiceScope.SINGLETON)
public class EventManagerImpl implements EventManager {

	private Map<String, Map<EventHandlerPolicy, Set<EventHandler>>> eventHandlers = new Hashtable<>();

	@Reference
	private LogService logger;

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	public void addHandler(EventHandler eventHandler) {
		if (eventHandler.getSupportedEvents() != null) {
			for (Map.Entry<EventInfo, EventHandlerPolicy> entry : eventHandler.getSupportedEvents().entrySet()) {
				EventInfo info = entry.getKey();
				EventHandlerPolicy policy = entry.getValue();
				if (!this.eventHandlers.containsKey(info.getTopic())) this.eventHandlers.put(info.getTopic(), new HashMap<>());
				if (!this.eventHandlers.get(info.getTopic()).containsKey(policy)) this.eventHandlers.get(info.getTopic()).put(policy, new HashSet<>());
				this.eventHandlers.get(info.getTopic()).get(policy).add(eventHandler);
			}
			this.logger.log(LogService.LOG_DEBUG, String.format("Successfully registered event handler \"%s\" listening for \"%d\" events", eventHandler,
					eventHandler.getSupportedEvents().size()));
		}
	}

	public void removeHandler(EventHandler eventHandler) {
		if (eventHandler.getSupportedEvents() != null) {
			for (Map.Entry<EventInfo, EventHandlerPolicy> entry : eventHandler.getSupportedEvents().entrySet()) {
				EventInfo info = entry.getKey();
				EventHandlerPolicy policy = entry.getValue();
				if (this.eventHandlers.containsKey(info.getTopic())) {
					if (this.eventHandlers.get(info.getTopic()).containsKey(policy)) {
						this.eventHandlers.get(info.getTopic()).get(policy).remove(eventHandler);
					}
				}
			}
			this.logger.log(LogService.LOG_DEBUG, "Successfully unregistered event handler " + eventHandler);
		}
	}

	@Override
	public void dispatchEvent(EventInfo eventInfo) {
		this.dispatchEvent(eventInfo, null, null);
	}

	@Override
	public void dispatchEvent(EventInfo eventInfo, EventDispatchPolicy policy) {
		this.dispatchEvent(eventInfo, null, policy);
	}

	@Override
	public void dispatchEvent(EventInfo eventInfo, WriteableEventProperties properties) {
		this.dispatchEvent(eventInfo, properties, null);
	}

	private ExecutorService asynchronousExecutor = Executors.newCachedThreadPool();

	@Override
	@SuppressWarnings("incomplete-switch")
	public void dispatchEvent(EventInfo eventInfo, WriteableEventProperties properties, EventDispatchPolicy policy) {
		String eventTopic = eventInfo.getTopic();

		WriteableEventProperties writeableEventProperties = properties == null ? new DefaultWriteableEventProperties() : properties;
		writeableEventProperties.lock();

		Set<EventHandler> synchronousHandlers = new HashSet<>();
		Set<EventHandler> asynchronousHandlers = new HashSet<>();

		EventDispatchPolicy dispatchPolicy = eventInfo.getEventDispatchPolicy() != EventDispatchPolicy.NOT_SPECIFIED ? eventInfo.getEventDispatchPolicy()
				: policy != null && policy != EventDispatchPolicy.NOT_SPECIFIED ? policy : EventDispatchPolicy.HANDLER;

		switch (dispatchPolicy) {
			case SYNCHRONOUS:
				synchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.SYNCHRONOUS));
				synchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.NOT_SPECIFIED));
				break;
			case ASYNCHRONOUS:
				asynchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.ASYNCHRONOUS));
				asynchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.NOT_SPECIFIED));
				break;
			case HANDLER:
				synchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.SYNCHRONOUS));
				asynchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.ASYNCHRONOUS));
				synchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.NOT_SPECIFIED));
				break;
		}

		this.dispatchEvent(eventInfo, EventDispatchPolicy.SYNCHRONOUS, writeableEventProperties, synchronousHandlers, this::sendEvent);
		this.dispatchEvent(eventInfo, EventDispatchPolicy.ASYNCHRONOUS, writeableEventProperties, asynchronousHandlers, this::postEvent);
	}

	private void dispatchEvent(EventInfo eventInfo, EventDispatchPolicy policy, WriteableEventProperties properties, Set<EventHandler> handlers,
			BiConsumer<Event, Set<EventHandler>> dispatchMethod) {
		dispatchMethod.accept(new EventImpl(new DefaultEventInfo(eventInfo.getTopic(), policy), properties), handlers);
	}

	private Set<EventHandler> getEventHandlersByTopicAndPolicy(String eventTopic, EventHandlerPolicy policy) {
		Set<EventHandler> ret = new HashSet<>();
		if (this.eventHandlers.containsKey(eventTopic) && this.eventHandlers.get(eventTopic).containsKey(policy)) {
			ret.addAll(this.eventHandlers.get(eventTopic).get(policy));
		}
		return ret;
	}

	private void sendEvent(Event event, Set<EventHandler> handlers) {
		handlers.forEach(handler -> {
			handler.handleEvent(event);
		});
	}

	private void postEvent(Event event, Set<EventHandler> handlers) {
		this.asynchronousExecutor.execute(() -> this.sendEvent(event, handlers));
	}

	@Deactivate
	public void onDeactivate() {
		this.asynchronousExecutor.shutdownNow();
	}

}

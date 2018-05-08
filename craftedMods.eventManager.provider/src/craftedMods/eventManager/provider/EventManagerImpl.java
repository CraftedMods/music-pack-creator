package craftedMods.eventManager.provider;

import java.util.*;
import java.util.concurrent.*;

import org.osgi.service.component.annotations.*;
import org.osgi.service.log.LogService;

import craftedMods.eventManager.api.*;
import craftedMods.eventManager.base.*;

@Component(scope = ServiceScope.SINGLETON)
public class EventManagerImpl implements EventManager {

	private Map<String, Map<EventHandlerPolicy, Set<EventHandler>>> eventHandlers = new Hashtable<>();

	private List<Runnable> preActivationEventHandlerQuery = new ArrayList<>();

	@Reference
	private LogService logger;

	@Activate
	public void onActivate() {
		for (Runnable task : this.preActivationEventHandlerQuery)
			task.run();
		this.preActivationEventHandlerQuery.clear();
	}

	@Deactivate
	public void onDeactivate() throws InterruptedException {
		this.asynchronousExecutor.shutdown();
		this.asynchronousExecutor.awaitTermination(2, TimeUnit.SECONDS);
	}

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
			Runnable task = () -> {
				this.logger.log(LogService.LOG_DEBUG, String.format("Rregistered event handler \"%s\" listening for \"%d\" events", eventHandler,
						eventHandler.getSupportedEvents().size()));
			};
			if (this.logger != null) task.run();
			else this.preActivationEventHandlerQuery.add(task);
		}
	}

	public void removeHandler(EventHandler eventHandler) {
		if (eventHandler.getSupportedEvents() != null) {
			for (Map.Entry<EventInfo, EventHandlerPolicy> entry : eventHandler.getSupportedEvents().entrySet()) {
				EventInfo info = entry.getKey();
				EventHandlerPolicy policy = entry.getValue();
				if (this.eventHandlers.containsKey(info.getTopic()))
					if (this.eventHandlers.get(info.getTopic()).containsKey(policy)) this.eventHandlers.get(info.getTopic()).get(policy).remove(eventHandler);
			}
			this.logger.log(LogService.LOG_DEBUG, "Unregistered event handler " + eventHandler);
		}
	}

	private ExecutorService asynchronousExecutor = Executors.newCachedThreadPool();

	@Override
	public Collection<EventProperties> dispatchEvent(EventInfo eventInfo) {
		return this.dispatchEvent(eventInfo, null, null);
	}

	@Override
	public Collection<EventProperties> dispatchEvent(EventInfo eventInfo, EventDispatchPolicy policy) {
		return this.dispatchEvent(eventInfo, null, policy);
	}

	@Override
	public Collection<EventProperties> dispatchEvent(EventInfo eventInfo, WriteableEventProperties properties) {
		return this.dispatchEvent(eventInfo, properties, null);
	}

	@Override
	@SuppressWarnings("incomplete-switch")
	public Collection<EventProperties> dispatchEvent(EventInfo eventInfo, WriteableEventProperties properties, EventDispatchPolicy policy) {
		String eventTopic = eventInfo.getTopic();

		WriteableEventProperties writeableEventProperties = properties == null ? new DefaultWriteableEventProperties() : properties;
		writeableEventProperties.lock();

		Set<EventHandler> synchronousHandlers = new HashSet<>();
		Set<EventHandler> asynchronousHandlers = new HashSet<>();

		EventDispatchPolicy dispatchPolicy = eventInfo.getEventDispatchPolicy() != EventDispatchPolicy.NOT_SPECIFIED ? eventInfo.getEventDispatchPolicy()
				: policy != null && policy != EventDispatchPolicy.NOT_SPECIFIED ? policy : EventDispatchPolicy.HANDLER;

		switch (dispatchPolicy) {
			case ASYNCHRONOUS:
				synchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.SYNCHRONOUS));
				synchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.NOT_SPECIFIED));
				break;
			case SYNCHRONOUS:
				asynchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.ASYNCHRONOUS));
				asynchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.NOT_SPECIFIED));
				break;
			case HANDLER:
				synchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.SYNCHRONOUS));
				asynchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.ASYNCHRONOUS));
				synchronousHandlers.addAll(this.getEventHandlersByTopicAndPolicy(eventTopic, EventHandlerPolicy.NOT_SPECIFIED));// This is provider specific
				break;
		}

		this.postEvent(new EventImpl(new DefaultEventInfo(eventTopic, EventDispatchPolicy.ASYNCHRONOUS), writeableEventProperties), asynchronousHandlers);
		return this.sendEvent(new EventImpl(new DefaultEventInfo(eventTopic, EventDispatchPolicy.SYNCHRONOUS), writeableEventProperties), synchronousHandlers);
	}

	private Set<EventHandler> getEventHandlersByTopicAndPolicy(String eventTopic, EventHandlerPolicy policy) {
		Set<EventHandler> ret = new HashSet<>();
		if (this.eventHandlers.containsKey(eventTopic) && this.eventHandlers.get(eventTopic).containsKey(policy))
			ret.addAll(this.eventHandlers.get(eventTopic).get(policy));
		return ret;
	}

	private Collection<EventProperties> sendEvent(EventImpl event, Set<EventHandler> handlers) {
		List<WriteableEventProperties> results = new ArrayList<>();
		for (EventHandler handler : handlers) {
			handler.handleEvent(event);
			if (!event.getEventResults().isEmpty()) {
				event.getEventResults().lock();
				results.add(event.getEventResults());
				event.recreateEventResults();
			}
		}
		return Collections.unmodifiableList(results);
	}

	private void postEvent(Event event, Set<EventHandler> handlers) {
		this.asynchronousExecutor.execute(() -> {
			handlers.forEach(handler -> handler.handleEvent(event));
		});
	}

}

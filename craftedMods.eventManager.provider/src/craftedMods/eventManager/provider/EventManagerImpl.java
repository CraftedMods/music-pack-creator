package craftedMods.eventManager.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

import craftedMods.eventManager.api.Event;
import craftedMods.eventManager.api.EventDispatchPolicy;
import craftedMods.eventManager.api.EventHandler;
import craftedMods.eventManager.api.EventHandlerPolicy;
import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.EventProperties;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.eventManager.base.DefaultWriteableEventProperties;

@Component(scope = ServiceScope.SINGLETON)
public class EventManagerImpl implements EventManager {

	private Map<String, Map<EventHandlerPolicy, Set<EventHandler>>> eventHandlers = new Hashtable<>();

	private List<Runnable> preActivationEventHandlerQuery = new ArrayList<>();

	@Reference(service=LoggerFactory.class)
	private Logger logger;

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
				this.logger.debug("Registered the event handler \"%s\" listening for %d events", eventHandler,
						eventHandler.getSupportedEvents().size());
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
			this.logger.debug("Unregistered the event handler %s", eventHandler);
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

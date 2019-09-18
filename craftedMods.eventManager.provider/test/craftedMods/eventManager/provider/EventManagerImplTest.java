package craftedMods.eventManager.provider;

import java.util.ArrayList;
import java.util.List;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.log.FormatterLogger;

import craftedMods.eventManager.api.Event;
import craftedMods.eventManager.api.EventDispatchPolicy;
import craftedMods.eventManager.api.EventHandler;
import craftedMods.eventManager.api.EventHandlerPolicy;
import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.eventManager.base.DefaultWriteableEventProperties;
import craftedMods.eventManager.base.EventUtils;
import craftedMods.utils.data.ArrayUtils;
import craftedMods.utils.data.ReadOnlyTypedProperties;
import craftedMods.utils.data.TypedPropertyKey;

@RunWith(EasyMockRunner.class)
public class EventManagerImplTest {

	@TestSubject
	private EventManagerImpl eventManager = new EventManagerImpl();

	@Mock(type = MockType.NICE)
	private FormatterLogger mockLogger;

	private EventInfo synchronousDispatchedEvent;
	private EventInfo asynchronousDispatchedEvent;
	private EventInfo handlerSpecificDispatchedEvent;
	private EventInfo unspecifiedDispatchedEvent;

	private EventHandler mockEventHandler1;
	private EventHandler mockEventHandler2;
	private EventHandler mockEventHandler3;

	@Before
	public void setup() {
		this.synchronousDispatchedEvent = new DefaultEventInfo("TOPIC_1", EventDispatchPolicy.SYNCHRONOUS);
		this.asynchronousDispatchedEvent = new DefaultEventInfo("TOPIC_2", EventDispatchPolicy.ASYNCHRONOUS);
		this.handlerSpecificDispatchedEvent = new DefaultEventInfo("TOPIC_3", EventDispatchPolicy.HANDLER);
		this.unspecifiedDispatchedEvent = new DefaultEventInfo("TOPIC_4", EventDispatchPolicy.NOT_SPECIFIED);

		this.mockEventHandler1 = EasyMock.createMock(EventHandler.class);
		this.mockEventHandler2 = EasyMock.createMock(EventHandler.class);
		this.mockEventHandler3 = EasyMock.createMock(EventHandler.class);

		EasyMock.replay(this.mockLogger);
	}

	@Test
	public void testAddEventHandler() {
		EasyMock.expect(this.mockEventHandler1.getSupportedEvents())
				.andStubReturn(EventUtils.getSupportedEvents(EventHandlerPolicy.SYNCHRONOUS, this.synchronousDispatchedEvent));
		this.mockEventHandler1.handleEvent(EasyMock.anyObject(Event.class));
		EasyMock.expectLastCall().once();

		EasyMock.replay(this.mockEventHandler1);

		this.eventManager.addHandler(this.mockEventHandler1);
		this.eventManager.dispatchEvent(this.synchronousDispatchedEvent);

		EasyMock.verify(this.mockEventHandler1);
	}

	@Test
	public void testRemoveEventHandler() {
		EasyMock.expect(this.mockEventHandler1.getSupportedEvents())
				.andStubReturn(EventUtils.getSupportedEvents(EventHandlerPolicy.SYNCHRONOUS, this.synchronousDispatchedEvent));

		EasyMock.replay(this.mockEventHandler1);

		this.eventManager.addHandler(this.mockEventHandler1);
		this.eventManager.removeHandler(this.mockEventHandler1);
		this.eventManager.dispatchEvent(this.synchronousDispatchedEvent);

		EasyMock.verify(this.mockEventHandler1);
	}

	@Test
	public void testSynchronousDeclaredUnmodifiedDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventHandlerPolicy.SYNCHRONOUS, EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testSynchronousDeclaredUnmodifiedDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventHandlerPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testSynchronousDeclaredUnmodifiedDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventHandlerPolicy.NOT_SPECIFIED, EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredUnmodifiedDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventHandlerPolicy.SYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredUnmodifiedDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventHandlerPolicy.ASYNCHRONOUS, EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredUnmodifiedDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventHandlerPolicy.NOT_SPECIFIED, EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredUnmodifiedDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventHandlerPolicy.SYNCHRONOUS, EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredUnmodifiedDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventHandlerPolicy.ASYNCHRONOUS, EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredUnmodifiedDispatchedUnspecificHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventHandlerPolicy.NOT_SPECIFIED, EventDispatchPolicy.SYNCHRONOUS,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredUnmodifiedDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventHandlerPolicy.SYNCHRONOUS, EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredUnmodifiedDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventHandlerPolicy.ASYNCHRONOUS, EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredUnmodifiedDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventHandlerPolicy.NOT_SPECIFIED, EventDispatchPolicy.SYNCHRONOUS,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testSynchronousDeclaredSynchronousDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.SYNCHRONOUS,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testSynchronousDeclaredSynchronousDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testSynchronousDeclaredSynchronousDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredSynchronousDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.SYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredSynchronousDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.ASYNCHRONOUS,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredSynchronousDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredSynchronousDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.SYNCHRONOUS,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredSynchronousDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.ASYNCHRONOUS,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredSynchronousDispatchedUnspecificHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.SYNCHRONOUS, EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredSynchronousDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.SYNCHRONOUS,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredSynchronousDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredSynchronousDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventDispatchPolicy.SYNCHRONOUS, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testSynchronousDeclaredAsynchronousDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.SYNCHRONOUS,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testSynchronousDeclaredAsynchronousDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testSynchronousDeclaredAsynchronousDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredAsynchronousDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.SYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredAsynchronousDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.ASYNCHRONOUS,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredAsynchronousDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredAsynchronousDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.SYNCHRONOUS,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredAsynchronousDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.ASYNCHRONOUS,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredAsynchronousDispatchedUnspecificHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.SYNCHRONOUS, EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredAsynchronousDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.SYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredAsynchronousDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.ASYNCHRONOUS,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredAsynchronousDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventDispatchPolicy.ASYNCHRONOUS, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testSynchronousDeclaredUnspecificDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.SYNCHRONOUS,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testSynchronousDeclaredUnspecificDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testSynchronousDeclaredUnspecificDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.synchronousDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredUnspecificDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.SYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredUnspecificDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.ASYNCHRONOUS,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testAsynchronousDeclaredUnspecificDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.asynchronousDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredUnspecificDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.SYNCHRONOUS,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredUnspecificDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.ASYNCHRONOUS,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testHandlerSpecificDeclaredUnspecificDispatchedUnspecificHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.handlerSpecificDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.SYNCHRONOUS, EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredUnspecificDispatchedSynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.SYNCHRONOUS,
				EventDispatchPolicy.SYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredUnspecificDispatchedAsynchronousHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.ASYNCHRONOUS,
				EventDispatchPolicy.ASYNCHRONOUS);
	}

	@Test
	public void testUnspecifiedDeclaredUnspecificDispatchedUnspecifiedHandledEvent() throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(this.unspecifiedDispatchedEvent, EventDispatchPolicy.NOT_SPECIFIED, EventHandlerPolicy.NOT_SPECIFIED,
				EventDispatchPolicy.SYNCHRONOUS, EventDispatchPolicy.ASYNCHRONOUS);
	}

	private void testWithoutModificationsDispatchedEvent(EventInfo eventInfo, EventHandlerPolicy handlerPolicy, EventDispatchPolicy... requiredPolicies)
			throws InterruptedException {
		this.testWithoutModificationsDispatchedEvent(eventInfo, null, handlerPolicy, requiredPolicies);
	}

	private void testWithoutModificationsDispatchedEvent(EventInfo eventInfo, EventDispatchPolicy dispatchPolicy, EventHandlerPolicy handlerPolicy,
			EventDispatchPolicy... requiredPolicies) throws InterruptedException {
		EasyMock.expect(this.mockEventHandler1.getSupportedEvents()).andStubReturn(EventUtils.getSupportedEvents(handlerPolicy, eventInfo));

		Capture<Event> receivedEventCapture = EasyMock.newCapture();
		if (requiredPolicies.length > 0) {
			this.mockEventHandler1.handleEvent(EasyMock.capture(receivedEventCapture));
			EasyMock.expectLastCall().once();
		}

		EasyMock.replay(this.mockEventHandler1);

		WriteableEventProperties properties = new DefaultWriteableEventProperties();
		TypedPropertyKey<String> property1 = TypedPropertyKey.createStringPropertyKey();
		properties.put(property1, "Test");
		TypedPropertyKey<Integer> property2 = TypedPropertyKey.createIntegerPropertyKey();
		properties.put(property2, 6);

		this.eventManager.addHandler(this.mockEventHandler1);
		this.eventManager.dispatchEvent(eventInfo, properties, dispatchPolicy);

		this.eventManager.onDeactivate();

		EasyMock.verify(this.mockEventHandler1);

		if (requiredPolicies.length > 0) {
			Event receivedEvent = receivedEventCapture.getValue();

			Assert.assertTrue(receivedEvent.matches(eventInfo));
			Assert.assertTrue(ArrayUtils.contains(requiredPolicies, receivedEvent.getEventInfo().getEventDispatchPolicy()));
			Assert.assertEquals("Test", receivedEvent.getEventProperties().getProperty(property1));
			Assert.assertEquals(Integer.valueOf(6), receivedEvent.getEventProperties().getProperty(property2));
		}
	}

	@Test
	public void testSynchronousHandledEventWithResults() {
		EasyMock.expect(this.mockEventHandler1.getSupportedEvents())
				.andStubReturn(EventUtils.getSupportedEvents(EventHandlerPolicy.SYNCHRONOUS, this.synchronousDispatchedEvent));
		EasyMock.expect(this.mockEventHandler2.getSupportedEvents())
				.andStubReturn(EventUtils.getSupportedEvents(EventHandlerPolicy.SYNCHRONOUS, this.synchronousDispatchedEvent));
		EasyMock.expect(this.mockEventHandler3.getSupportedEvents())
				.andStubReturn(EventUtils.getSupportedEvents(EventHandlerPolicy.SYNCHRONOUS, this.synchronousDispatchedEvent));

		TypedPropertyKey<String> property1 = TypedPropertyKey.createStringPropertyKey();
		TypedPropertyKey<Integer> property2 = TypedPropertyKey.createIntegerPropertyKey();

		this.mockEventHandler1.handleEvent(EasyMock.anyObject(Event.class));
		EasyMock.expectLastCall().andAnswer(() -> {
			Event event = (Event) EasyMock.getCurrentArguments()[0];
			event.getEventResults().put(property1, "Test");
			event.getEventResults().put(property2, 6);
			return null;
		}).once();

		this.mockEventHandler2.handleEvent(EasyMock.anyObject(Event.class));
		EasyMock.expectLastCall().andAnswer(() -> {
			Event event = (Event) EasyMock.getCurrentArguments()[0];
			event.getEventResults().put(property1, "Test2");
			return null;
		}).once();

		this.mockEventHandler3.handleEvent(EasyMock.anyObject(Event.class));
		EasyMock.expectLastCall().once();

		EasyMock.replay(this.mockEventHandler1);
		EasyMock.replay(this.mockEventHandler2);
		EasyMock.replay(this.mockEventHandler3);

		this.eventManager.addHandler(this.mockEventHandler1);
		this.eventManager.addHandler(this.mockEventHandler2);
		this.eventManager.addHandler(this.mockEventHandler3);

		List<ReadOnlyTypedProperties> results = new ArrayList<>(this.eventManager.dispatchEvent(this.synchronousDispatchedEvent));

		EasyMock.verify(this.mockEventHandler1);

		Assert.assertEquals(2, results.size());

		ReadOnlyTypedProperties properties1 = results.get(0);
		ReadOnlyTypedProperties properties2 = results.get(1);

		Assert.assertNotNull(properties1);
		Assert.assertNotNull(properties2);

		if (!properties1.containsProperty(property2)) {
			ReadOnlyTypedProperties tmp2 = properties2;
			properties2 = properties1;
			properties1 = tmp2;
		}

		Assert.assertEquals("Test", properties1.getProperty(property1));
		Assert.assertEquals(Integer.valueOf(6), properties1.getProperty(property2));
		Assert.assertEquals("Test2", properties2.getProperty(property1));

	}

	@Test
	public void testAynchronousHandledEventWithResults() throws InterruptedException {
		EasyMock.expect(this.mockEventHandler1.getSupportedEvents())
				.andStubReturn(EventUtils.getSupportedEvents(EventHandlerPolicy.ASYNCHRONOUS, this.asynchronousDispatchedEvent));
		EasyMock.expect(this.mockEventHandler2.getSupportedEvents())
				.andStubReturn(EventUtils.getSupportedEvents(EventHandlerPolicy.ASYNCHRONOUS, this.asynchronousDispatchedEvent));
		EasyMock.expect(this.mockEventHandler3.getSupportedEvents())
				.andStubReturn(EventUtils.getSupportedEvents(EventHandlerPolicy.ASYNCHRONOUS, this.asynchronousDispatchedEvent));

		this.mockEventHandler1.handleEvent(EasyMock.anyObject(Event.class));
		EasyMock.expectLastCall().once();

		this.mockEventHandler2.handleEvent(EasyMock.anyObject(Event.class));
		EasyMock.expectLastCall().once();

		this.mockEventHandler3.handleEvent(EasyMock.anyObject(Event.class));
		EasyMock.expectLastCall().once();

		EasyMock.replay(this.mockEventHandler1);
		EasyMock.replay(this.mockEventHandler2);
		EasyMock.replay(this.mockEventHandler3);

		this.eventManager.addHandler(this.mockEventHandler1);
		this.eventManager.addHandler(this.mockEventHandler2);
		this.eventManager.addHandler(this.mockEventHandler3);

		List<ReadOnlyTypedProperties> results = new ArrayList<>(this.eventManager.dispatchEvent(this.asynchronousDispatchedEvent));

		this.eventManager.onDeactivate();

		EasyMock.verify(this.mockEventHandler1);

		Assert.assertEquals(0, results.size());
	}

}

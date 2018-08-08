package craftedMods.eventManager.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Test;

import craftedMods.eventManager.api.EventDispatchPolicy;
import craftedMods.eventManager.api.EventHandlerPolicy;
import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.EventProperties;
import craftedMods.eventManager.api.PropertyKey;
import craftedMods.eventManager.api.WriteableEventProperties;

public class EventUtilsTest extends EasyMockSupport {

	@Test
	public void testGetSupportedEventsDefaultPolicy() {
		EventInfo info = new DefaultEventInfo("TOPIC");
		Assert.assertEquals(EventHandlerPolicy.NOT_SPECIFIED, EventUtils.getSupportedEvents(info).get(info));
	}

	@Test(expected = NullPointerException.class)
	public void testProceedNullCollection() {
		EventUtils.proceed(null, DefaultPropertyKey.createBooleanPropertyKey());
	}

	@Test(expected = NullPointerException.class)
	public void testProceedNullKey() {
		EventUtils.proceed(Arrays.asList(), null);
	}

	@Test
	public void testProceedEmptyListDefaultTrue() {
		Assert.assertTrue(EventUtils.proceed(Arrays.asList(), DefaultPropertyKey.createBooleanPropertyKey()));
	}

	@Test
	public void testProceedEmptyListDefaultFalse() {
		Assert.assertFalse(EventUtils.proceed(Arrays.asList(), DefaultPropertyKey.createBooleanPropertyKey(), false));
	}

	@Test
	public void testProceedAllContainAllDefaultTrue() {
		PropertyKey<Boolean> key = DefaultPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.TRUE);

		Assert.assertTrue(EventUtils.proceed(Arrays.asList(props1, props2), key));
	}

	@Test
	public void testProceedAllContainAllDefaultFalse() {
		PropertyKey<Boolean> key = DefaultPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.TRUE);

		Assert.assertTrue(EventUtils.proceed(Arrays.asList(props1, props2), key, false));
	}

	@Test
	public void testProceedSomeContainAllDefaultTrue() {
		PropertyKey<Boolean> key = DefaultPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.FALSE);

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key));
	}

	@Test
	public void testProceedSomeContainAllDefaultFalse() {
		PropertyKey<Boolean> key = DefaultPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.FALSE);

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key, false));
	}

	@Test
	public void testProceedSomeContainSomeDefaultTrue() {
		PropertyKey<Boolean> key = DefaultPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();

		Assert.assertTrue(EventUtils.proceed(Arrays.asList(props1, props2), key));
	}

	@Test
	public void testProceedSomeContainSomeDefaultFalse() {
		PropertyKey<Boolean> key = DefaultPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key, false));
	}

	@Test
	public void testProceedNoneContainAllDefaultTrue() {
		PropertyKey<Boolean> key = DefaultPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.FALSE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.FALSE);

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key));
	}

	@Test
	public void testProceedNoneContainAllDefaultFalse() {
		PropertyKey<Boolean> key = DefaultPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.FALSE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.FALSE);

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key, false));
	}
	
	@Test
	public void testProceedNoneContainSomeDefaultTrue() {
		PropertyKey<Boolean> key = DefaultPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.FALSE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key));
	}

	@Test
	public void testProceedNoneContainSomeDefaultFalse() {
		PropertyKey<Boolean> key = DefaultPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.FALSE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key, false));
	}

	@Test(expected = NullPointerException.class)
	public void testDispatchEventNoPolicyNullManager() {
		EventUtils.dispatchEvent(null, new DefaultEventInfo("FKD"), DefaultPropertyKey.createBooleanPropertyKey(),
				false);
	}

	@Test(expected = NullPointerException.class)
	public void testDispatchEventNoPolicyNullInfo() {
		EventUtils.dispatchEvent(this.createMock(EventManager.class), null,
				DefaultPropertyKey.createBooleanPropertyKey(), false);
	}

	@Test(expected = NullPointerException.class)
	public void testDispatchEventNoPolicyNullKey() {
		EventUtils.dispatchEvent(this.createMock(EventManager.class), new DefaultEventInfo("FKD"), null, false);
	}

	@Test
	public void testDispatchEventNoPolicyNullPolicy() {
		EventManager mockEventManager = this.createMock(EventManager.class);
		EventInfo info = new DefaultEventInfo("TOPIC");
		PropertyKey<String> key = DefaultPropertyKey.createStringPropertyKey();
		String value = "123lk";
		Collection<EventProperties> returnedResults = new ArrayList<>();

		Capture<WriteableEventProperties> capturedProperties = Capture.newInstance();

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(info), EasyMock.capture(capturedProperties)))
				.andReturn(returnedResults).once();

		this.replayAll();

		Collection<EventProperties> results = EventUtils.dispatchEvent(mockEventManager, info, key, value, null);

		Assert.assertTrue(returnedResults == results);

		WriteableEventProperties properties = capturedProperties.getValue();

		Assert.assertEquals(value, properties.getProperty(key));

		this.verifyAll();
	}

	@Test
	public void testDispatchEventNoPolicy() {
		EventManager mockEventManager = this.createMock(EventManager.class);
		EventInfo info = new DefaultEventInfo("TOPIC");
		PropertyKey<String> key = DefaultPropertyKey.createStringPropertyKey();
		String value = "123lk";
		Collection<EventProperties> returnedResults = new ArrayList<>();

		Capture<WriteableEventProperties> capturedProperties = Capture.newInstance();

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(info), EasyMock.capture(capturedProperties)))
				.andReturn(returnedResults).once();

		this.replayAll();

		Collection<EventProperties> results = EventUtils.dispatchEvent(mockEventManager, info, key, value);

		Assert.assertTrue(returnedResults == results);

		WriteableEventProperties properties = capturedProperties.getValue();

		Assert.assertEquals(value, properties.getProperty(key));

		this.verifyAll();
	}

	@Test
	public void testDispatchEventSpecifiedPolicy() {
		EventManager mockEventManager = this.createMock(EventManager.class);
		EventInfo info = new DefaultEventInfo("TOPIC");
		PropertyKey<String> key = DefaultPropertyKey.createStringPropertyKey();
		String value = "123lk";
		Collection<EventProperties> returnedResults = new ArrayList<>();

		Capture<WriteableEventProperties> capturedProperties = Capture.newInstance();

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(info), EasyMock.capture(capturedProperties),
				EasyMock.eq(EventDispatchPolicy.HANDLER))).andReturn(returnedResults).once();

		this.replayAll();

		Collection<EventProperties> results = EventUtils.dispatchEvent(mockEventManager, info, key, value,
				EventDispatchPolicy.HANDLER);

		Assert.assertTrue(returnedResults == results);

		WriteableEventProperties properties = capturedProperties.getValue();

		Assert.assertEquals(value, properties.getProperty(key));

		this.verifyAll();
	}

}

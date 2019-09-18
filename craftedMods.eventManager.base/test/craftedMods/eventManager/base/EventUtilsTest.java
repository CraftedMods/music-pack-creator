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
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.utils.data.ReadOnlyTypedProperties;
import craftedMods.utils.data.TypedPropertyKey;

public class EventUtilsTest extends EasyMockSupport {

	@Test
	public void testGetSupportedEventsDefaultPolicy() {
		EventInfo info = new DefaultEventInfo("TOPIC");
		Assert.assertEquals(EventHandlerPolicy.NOT_SPECIFIED, EventUtils.getSupportedEvents(info).get(info));
	}

	@Test(expected = NullPointerException.class)
	public void testProceedNullCollection() {
		EventUtils.proceed(null, TypedPropertyKey.createBooleanPropertyKey());
	}

	@Test(expected = NullPointerException.class)
	public void testProceedNullKey() {
		EventUtils.proceed(Arrays.asList(), null);
	}

	@Test
	public void testProceedEmptyListDefaultTrue() {
		Assert.assertTrue(EventUtils.proceed(Arrays.asList(), TypedPropertyKey.createBooleanPropertyKey()));
	}

	@Test
	public void testProceedEmptyListDefaultFalse() {
		Assert.assertFalse(EventUtils.proceed(Arrays.asList(), TypedPropertyKey.createBooleanPropertyKey(), false));
	}

	@Test
	public void testProceedAllContainAllDefaultTrue() {
		TypedPropertyKey<Boolean> key = TypedPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.TRUE);

		Assert.assertTrue(EventUtils.proceed(Arrays.asList(props1, props2), key));
	}

	@Test
	public void testProceedAllContainAllDefaultFalse() {
		TypedPropertyKey<Boolean> key = TypedPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.TRUE);

		Assert.assertTrue(EventUtils.proceed(Arrays.asList(props1, props2), key, false));
	}

	@Test
	public void testProceedSomeContainAllDefaultTrue() {
		TypedPropertyKey<Boolean> key = TypedPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.FALSE);

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key));
	}

	@Test
	public void testProceedSomeContainAllDefaultFalse() {
		TypedPropertyKey<Boolean> key = TypedPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.FALSE);

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key, false));
	}

	@Test
	public void testProceedSomeContainSomeDefaultTrue() {
		TypedPropertyKey<Boolean> key = TypedPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();

		Assert.assertTrue(EventUtils.proceed(Arrays.asList(props1, props2), key));
	}

	@Test
	public void testProceedSomeContainSomeDefaultFalse() {
		TypedPropertyKey<Boolean> key = TypedPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.TRUE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key, false));
	}

	@Test
	public void testProceedNoneContainAllDefaultTrue() {
		TypedPropertyKey<Boolean> key = TypedPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.FALSE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.FALSE);

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key));
	}

	@Test
	public void testProceedNoneContainAllDefaultFalse() {
		TypedPropertyKey<Boolean> key = TypedPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.FALSE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();
		props2.put(key, Boolean.FALSE);

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key, false));
	}

	@Test
	public void testProceedNoneContainSomeDefaultTrue() {
		TypedPropertyKey<Boolean> key = TypedPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.FALSE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key));
	}

	@Test
	public void testProceedNoneContainSomeDefaultFalse() {
		TypedPropertyKey<Boolean> key = TypedPropertyKey.createBooleanPropertyKey();

		WriteableEventProperties props1 = new DefaultWriteableEventProperties();
		props1.put(key, Boolean.FALSE);

		WriteableEventProperties props2 = new DefaultWriteableEventProperties();

		Assert.assertFalse(EventUtils.proceed(Arrays.asList(props1, props2), key, false));
	}

	@Test(expected = NullPointerException.class)
	public void testDispatchEventNoPolicyNullManager() {
		EventUtils.dispatchEvent(null, new DefaultEventInfo("FKD"), TypedPropertyKey.createBooleanPropertyKey(), false);
	}

	@Test(expected = NullPointerException.class)
	public void testDispatchEventNoPolicyNullInfo() {
		EventUtils.dispatchEvent(this.createMock(EventManager.class), null, TypedPropertyKey.createBooleanPropertyKey(),
				false);
	}

	@Test(expected = NullPointerException.class)
	public void testDispatchEventNoPolicyNullKey() {
		EventUtils.dispatchEvent(this.createMock(EventManager.class), new DefaultEventInfo("FKD"), null, false);
	}

	@Test
	public void testDispatchEventNoPolicyNullPolicy() {
		EventManager mockEventManager = this.createMock(EventManager.class);
		EventInfo info = new DefaultEventInfo("TOPIC");
		TypedPropertyKey<String> key = TypedPropertyKey.createStringPropertyKey();
		String value = "123lk";
		Collection<ReadOnlyTypedProperties> returnedResults = new ArrayList<>();

		Capture<WriteableEventProperties> capturedProperties = Capture.newInstance();

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(info), EasyMock.capture(capturedProperties)))
				.andReturn(returnedResults).once();

		this.replayAll();

		Collection<ReadOnlyTypedProperties> results = EventUtils.dispatchEvent(mockEventManager, info, key, value,
				null);

		Assert.assertTrue(returnedResults == results);

		WriteableEventProperties properties = capturedProperties.getValue();

		Assert.assertEquals(value, properties.getProperty(key));

		this.verifyAll();
	}

	@Test
	public void testDispatchEventNoPolicy() {
		EventManager mockEventManager = this.createMock(EventManager.class);
		EventInfo info = new DefaultEventInfo("TOPIC");
		TypedPropertyKey<String> key = TypedPropertyKey.createStringPropertyKey();
		String value = "123lk";
		Collection<ReadOnlyTypedProperties> returnedResults = new ArrayList<>();

		Capture<WriteableEventProperties> capturedProperties = Capture.newInstance();

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(info), EasyMock.capture(capturedProperties)))
				.andReturn(returnedResults).once();

		this.replayAll();

		Collection<ReadOnlyTypedProperties> results = EventUtils.dispatchEvent(mockEventManager, info, key, value);

		Assert.assertTrue(returnedResults == results);

		WriteableEventProperties properties = capturedProperties.getValue();

		Assert.assertEquals(value, properties.getProperty(key));

		this.verifyAll();
	}

	@Test
	public void testDispatchEventSpecifiedPolicy() {
		EventManager mockEventManager = this.createMock(EventManager.class);
		EventInfo info = new DefaultEventInfo("TOPIC");
		TypedPropertyKey<String> key = TypedPropertyKey.createStringPropertyKey();
		String value = "123lk";
		Collection<ReadOnlyTypedProperties> returnedResults = new ArrayList<>();

		Capture<WriteableEventProperties> capturedProperties = Capture.newInstance();

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(info), EasyMock.capture(capturedProperties),
				EasyMock.eq(EventDispatchPolicy.HANDLER))).andReturn(returnedResults).once();

		this.replayAll();

		Collection<ReadOnlyTypedProperties> results = EventUtils.dispatchEvent(mockEventManager, info, key, value,
				EventDispatchPolicy.HANDLER);

		Assert.assertTrue(returnedResults == results);

		WriteableEventProperties properties = capturedProperties.getValue();

		Assert.assertEquals(value, properties.getProperty(key));

		this.verifyAll();
	}

}

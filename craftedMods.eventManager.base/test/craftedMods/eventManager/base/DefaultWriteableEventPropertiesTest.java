package craftedMods.eventManager.base;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import craftedMods.utils.data.TypedPropertyKey;

public class DefaultWriteableEventPropertiesTest {

	private DefaultWriteableEventProperties properties;
	private TypedPropertyKey<String> key;

	@Before
	public void setup() {
		this.properties = new DefaultWriteableEventProperties();
		this.key = TypedPropertyKey.createStringPropertyKey();
	}

	@Test
	public void testLock() {
		Assert.assertFalse(this.properties.isLocked());
		Assert.assertTrue(this.properties.lock());
		Assert.assertTrue(this.properties.isLocked());
		Assert.assertFalse(this.properties.lock());
		Assert.assertTrue(this.properties.isLocked());
	}

	@Test(expected = IllegalStateException.class)
	public void testLockOnPut() {
		this.properties.lock();
		this.properties.put(this.key, "Test");
	}

	@Test(expected = IllegalStateException.class)
	public void testLockOnClear() {
		this.properties.lock();
		this.properties.clear();
	}

	@Test
	public void testLockOnContains() {
		this.properties.put(this.key, "Test");
		this.properties.lock();
		Assert.assertTrue(this.properties.containsProperty(this.key));
	}

	@Test
	public void testLockOnGet() {
		this.properties.put(this.key, "Test");
		this.properties.lock();
		Assert.assertEquals("Test", this.properties.getProperty(this.key));
	}

}

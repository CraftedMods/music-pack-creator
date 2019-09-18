package craftedMods.utils.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultTypedPropertiesTest {

	private DefaultTypedProperties properties;
	private TypedPropertyKey<String> key;

	@Before
	public void setup() {
		this.properties = new DefaultTypedProperties();
		this.key = TypedPropertyKey.createStringPropertyKey();
	}

	@Test
	public void testPut() {
		this.properties.put(this.key, "Test");
		Assert.assertEquals("Test", this.properties.getProperty(this.key));
	}

	@Test(expected = NullPointerException.class)
	public void testNullKey() {
		this.properties.put(null, false);
	}

	@Test
	public void testContains() {
		this.properties.put(this.key, "Test");
		Assert.assertTrue(this.properties.containsProperty(this.key));
	}

	@Test
	public void testIsEmpty() {
		Assert.assertTrue(this.properties.isEmpty());
		this.properties.put(this.key, "Test");
		Assert.assertFalse(this.properties.isEmpty());
		this.properties.clear();
		Assert.assertTrue(this.properties.isEmpty());
	}

	@Test
	public void testGet() {
		this.properties.put(this.key, "Test");
		Assert.assertEquals("Test", this.properties.getProperty(this.key));
	}

	@Test
	public void testClear() {
		this.properties.put(this.key, "Test");
		this.properties.clear();
		Assert.assertFalse(this.properties.containsProperty(this.key));
	}
}

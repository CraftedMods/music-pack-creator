package craftedMods.utils.data;

import org.junit.*;

public class ArrayUtilsTest {

	private String[] testArray1 = new String[] { "Test1", "Test2", null, "Test3", "Test4", "Pi", null };
	private String[] testArray2 = new String[] { "Test1", "Test2", "Test3", "Test4", "Pi" };
	private String[] testArray3 = new String[] {};

	@Test
	public void testContains() {
		Assert.assertTrue(ArrayUtils.contains(testArray1, "Test1"));
		Assert.assertFalse(ArrayUtils.contains(testArray1, "Test5"));
		Assert.assertFalse(ArrayUtils.contains(testArray3, "Test2"));
	}

	@Test
	public void testContainsNullValue() {
		Assert.assertTrue(ArrayUtils.contains(testArray1, null));
		Assert.assertFalse(ArrayUtils.contains(testArray2, null));
		Assert.assertFalse(ArrayUtils.contains(testArray3, null));
	}

	@Test(expected = NullPointerException.class)
	public void testContainsNullArray() {
		Assert.assertTrue(ArrayUtils.contains(null, "Test1"));
	}

}

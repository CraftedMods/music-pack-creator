package craftedMods.utils.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class CollectionUtilsTest {

	@Test(expected = NullPointerException.class)
	public void testWrappedSetNull() {
		CollectionUtils.createNonNullSetWithInternal(null);
	}

	@Test(expected = NullPointerException.class)
	public void testWrappedSetNonNullInitial() {
		Set<String> internal = new HashSet<>();

		internal.add("String1");
		internal.add(null);

		CollectionUtils.createNonNullSetWithInternal(internal);
	}

	@Test(expected = NullPointerException.class)
	public void testWrappedSetAddNull() {
		CollectionUtils.createNonNullSetWithInternal(new HashSet<>()).add(null);
	}

	@Test
	public void testWrappedSetNonNullToInternal() {
		Set<String> internal = new HashSet<>();

		internal.add("String1");

		Set<String> nonNullSet = CollectionUtils.createNonNullSetWithInternal(internal);

		internal.add(null);

		Assert.assertTrue(nonNullSet.contains(null));
	}

	@Test
	public void testWrappedSetBackingChanges() {
		Set<String> internal = new HashSet<>();

		internal.add("String1");

		Set<String> nonNullSet = CollectionUtils.createNonNullSetWithInternal(internal);

		internal.add("String2");
		nonNullSet.add("String3");

		Assert.assertTrue(nonNullSet.contains("String1"));
		Assert.assertTrue(nonNullSet.contains("String2"));
		Assert.assertTrue(nonNullSet.contains("String3"));
		Assert.assertTrue(internal.contains("String3"));
	}

	@Test(expected = NullPointerException.class)
	public void testCreateNonNullHashSetAddNull() {
		CollectionUtils.createNonNullHashSet().add(null);
	}

	@Test(expected = NullPointerException.class)
	public void testCreateNonNullLinkedHashSetAddNull() {
		CollectionUtils.createNonNullLinkedHashSet().add(null);
	}

	@Test(expected = NullPointerException.class)
	public void testCreateNonNullHashSetInitialNull() {
		CollectionUtils.createNonNullHashSet(null);
	}

	@Test(expected = NullPointerException.class)
	public void testCreateNonNullLinkedHashSetInitialNull() {
		CollectionUtils.createNonNullLinkedHashSet(null);
	}

	@Test(expected = NullPointerException.class)
	public void testCreateNonNullHashSetInitialContainsNull() {
		CollectionUtils.createNonNullHashSet(Arrays.asList("2", null, "e"));
	}

	@Test(expected = NullPointerException.class)
	public void testCreateNonNullLinkedHashSetInitialContainsNull() {
		CollectionUtils.createNonNullLinkedHashSet(Arrays.asList("2", null, "e"));
	}

	@Test
	public void testCreateNonNullHashSetInitial() {
		Set<String> set = CollectionUtils.createNonNullHashSet(Arrays.asList("2", "e"));

		Assert.assertTrue(set.contains("2"));
		Assert.assertTrue(set.contains("e"));
	}

	@Test
	public void testCreateNonNullLinkedHashSetInitial() {
		Set<String> set = CollectionUtils.createNonNullLinkedHashSet(Arrays.asList("2", "e"));

		Assert.assertTrue(set.contains("2"));
		Assert.assertTrue(set.contains("e"));
	}

	@Test
	public void testEqualsEqualObjectsButNotSameReference() {
		Set<String> set1 = CollectionUtils.createNonNullHashSet(Arrays.asList("str1", "str2", "str3"));
		Set<String> set2 = CollectionUtils.createNonNullHashSet(Arrays.asList("str1", "str2", "str3"));

		Assert.assertTrue(set1.equals(set2));
	}

}

package craftedMods.utils.data;

import org.junit.*;

public class TypedPropertyKeyTest {

	@Test
	public void testUniquesnessOfIdentifier() {
		Assert.assertNotEquals(TypedPropertyKey.createIntegerPropertyKey().getUniquePropertyIdentifier(),
				TypedPropertyKey.createIntegerPropertyKey().getUniquePropertyIdentifier());
	}

}

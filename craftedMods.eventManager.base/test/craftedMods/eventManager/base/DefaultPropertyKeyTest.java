package craftedMods.eventManager.base;

import org.junit.*;

public class DefaultPropertyKeyTest {

	@Test
	public void testUniquesnessOfIdentifier() {
		Assert.assertNotEquals(DefaultPropertyKey.createIntegerPropertyKey().getUniquePropertyIdentifier(),
				DefaultPropertyKey.createIntegerPropertyKey().getUniquePropertyIdentifier());
	}

}

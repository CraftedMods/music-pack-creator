package craftedMods.eventManager.base;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class DefaultPropertyKeyTest {

	@Test
	public void testUniquesnessOfIdentifier() {
		assertNotEquals(DefaultPropertyKey.getIntegerPropertyKey().getUniquePropertyIdentifier(),
				DefaultPropertyKey.getIntegerPropertyKey().getUniquePropertyIdentifier());
	}

}

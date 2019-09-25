package craftedMods.lotr.mpc.core.base;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

public class DefaultRegionTest {

	@Test
	public void testClone() {
		DefaultRegion region = new DefaultRegion("region1", Arrays.asList("sub1", "sub2", "sub3"),
				Arrays.asList("cat1"), 23.3f);
		Collection<String> subregions = region.getSubregions();
		Collection<String> categories = region.getCategories();

		DefaultRegion region2 = region.clone();

		Assert.assertEquals(region, region2);
		Assert.assertFalse(region == region2);

		Assert.assertEquals(subregions, region2.getSubregions()); // Theoretically implied be the region comparison, but
																	// if equals is wrong...
		Assert.assertEquals(categories, region2.getCategories());

		Assert.assertFalse(subregions == region2.getSubregions());
		Assert.assertFalse(categories == region2.getCategories());
	}

}

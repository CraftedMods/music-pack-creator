package craftedMods.lotr.mpc.data.provider;

import org.junit.Before;
import org.junit.Test;

public class MusicPackDataImplTest {

	private MusicPackDataImpl impl;

	@Before
	public void setup() {
		impl = new MusicPackDataImpl();
		impl.onActivate();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIsGetRegionsUnmodifiable() {
		impl.getRegions().remove("gondor");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIsGetRegionsListUnmodifiable() {
		impl.getRegions().get("gondor").remove("pelargir");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testIsGetCategoriesUnmodifiable() {
		impl.getCategories().add("height");
	}

}

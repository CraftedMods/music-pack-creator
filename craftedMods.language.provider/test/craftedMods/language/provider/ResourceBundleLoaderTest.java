package craftedMods.language.provider;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ResourceBundleLoader.class)
public class ResourceBundleLoaderTest extends EasyMockSupport {

	private ResourceBundleLoader loader;

	@Before
	public void setup() {
		loader = new ResourceBundleLoader();

	}

	@Test
	public void testLoadResourceBundleSuccessful() {
		PowerMock.mockStatic(ResourceBundle.class);

		ResourceBundle mockResourceBundle = createMockResourceBundle(Locale.US);

		EasyMock.expect(ResourceBundle.getBundle("." + ResourceBundleLoader.DEFAULT_BUNDLE_NAME, Locale.US))
				.andReturn(mockResourceBundle).once();

		PowerMock.replayAll();
		this.replayAll();

		Assert.assertEquals(mockResourceBundle, loader.loadResourceBunde(Locale.US, false));

		PowerMock.verifyAll();
		this.verifyAll();
	}

	@Test
	public void testLoadResourceBundleNotFound() {
		PowerMock.mockStatic(ResourceBundle.class);

		EasyMock.expect(ResourceBundle.getBundle("." + ResourceBundleLoader.DEFAULT_BUNDLE_NAME, Locale.US))
				.andThrow(new MissingResourceException("Resource not found", "", "")).once();

		PowerMock.replayAll();
		this.replayAll();

		Assert.assertEquals(null, loader.loadResourceBunde(Locale.US, false));

		PowerMock.verifyAll();
		this.verifyAll();
	}

	@Test
	public void testLoadResourceBundleNotFoundButAsSimilar() {
		PowerMock.mockStatic(ResourceBundle.class);

		ResourceBundle mockResourceBundle = createMockResourceBundle(Locale.CANADA);

		EasyMock.expect(ResourceBundle.getBundle("." + ResourceBundleLoader.DEFAULT_BUNDLE_NAME, Locale.US))
				.andThrow(new MissingResourceException("Resource not found", "", "")).once();
		EasyMock.expect(ResourceBundle.getBundle(EasyMock.eq("." + ResourceBundleLoader.DEFAULT_BUNDLE_NAME),
				EasyMock.not(EasyMock.or(EasyMock.eq(Locale.US), EasyMock.eq(Locale.CANADA)))))
				.andThrow(new MissingResourceException("Resource not found", "", "")).anyTimes();
		EasyMock.expect(ResourceBundle.getBundle("." + ResourceBundleLoader.DEFAULT_BUNDLE_NAME, Locale.CANADA))
				.andReturn(mockResourceBundle).once();

		PowerMock.replayAll();
		this.replayAll();

		Assert.assertEquals(mockResourceBundle, loader.loadResourceBunde(Locale.US, true));

		PowerMock.verifyAll();
		this.verifyAll();
	}

	private ResourceBundle createMockResourceBundle(Locale locale) {
		ResourceBundle mockBundle = this.createNiceMock(ResourceBundle.class);
		EasyMock.expect(mockBundle.getLocale()).andStubReturn(locale);
		return mockBundle;
	}

}

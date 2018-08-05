package craftedMods.language.provider;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.log.LogService;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import craftedMods.language.api.LanguageRegistry;
import craftedMods.language.provider.LanguageRegistryImpl.Config;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ResourceBundle.class)
public class LanguageRegistryImplTest extends EasyMockSupport {

	@TestSubject
	public LanguageRegistryImpl languageRegistry = new LanguageRegistryImpl();

	@Mock(type = MockType.NICE)
	private LogService mockLogger;

	@Mock
	private ResourceBundleLoader mockResourceBundleLoader;

	@Before
	public void setup() {
		if (Locale.getDefault().equals(Locale.US)) {
			Locale.setDefault(Locale.GERMANY);
		}
	}

	@Test
	public void testOnActivateDefaultConfiguration() {
		ResourceBundle mockBundle1 = createMockResourceBundle();
		ResourceBundle mockBundle2 = createMockResourceBundle();

		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andReturn(mockBundle1).once();
		EasyMock.expect(
				mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.getDefault()), EasyMock.anyBoolean()))
				.andReturn(mockBundle2).once();

		this.replayAll();

		languageRegistry.onActivate(createConfig("en-US", LanguageRegistry.SYSTEM_LANGUAGE));

		Assert.assertEquals(Locale.US, languageRegistry.getDefaultLanguage());
		Assert.assertEquals(Locale.getDefault(), languageRegistry.getCurrentLanguage());

		Assert.assertEquals(mockBundle1, languageRegistry.getEntries().get(Locale.US));
		Assert.assertEquals(mockBundle2, languageRegistry.getEntries().get(Locale.getDefault()));

		this.verifyAll();
	}

	@Test
	public void testOnActivateCurrentLanguageSet() {
		ResourceBundle mockBundle1 = createMockResourceBundle();

		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andReturn(mockBundle1).times(2);

		this.replayAll();

		languageRegistry.onActivate(createConfig("en-US", "en-US"));

		Assert.assertEquals(Locale.US, languageRegistry.getDefaultLanguage());
		Assert.assertEquals(Locale.US, languageRegistry.getCurrentLanguage());

		Assert.assertEquals(mockBundle1, languageRegistry.getEntries().get(Locale.US));

		this.verifyAll();
	}

	@Test
	public void testOnActivateNullBundles() {
		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andReturn(null).once();
		EasyMock.expect(
				mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.getDefault()), EasyMock.anyBoolean()))
				.andReturn(null).once();

		this.replayAll();

		languageRegistry.onActivate(createConfig("en-US", LanguageRegistry.SYSTEM_LANGUAGE));

		Assert.assertTrue(languageRegistry.getEntries().isEmpty());

		this.verifyAll();
	}

	@Test
	public void testOnModifyNothingChanged() {
		activate();

		this.replayAll();

		languageRegistry.onModify(createConfig("en-US", LanguageRegistry.SYSTEM_LANGUAGE));

		Assert.assertEquals(Locale.US, languageRegistry.getDefaultLanguage());
		Assert.assertEquals(Locale.getDefault(), languageRegistry.getCurrentLanguage());

		this.verifyAll();
	}

	@Test
	public void testOnModifyLocaleChanged() {
		activate();

		ResourceBundle mockBundle1 = createMockResourceBundle();
		ResourceBundle mockBundle2 = createMockResourceBundle();

		EasyMock.expect(
				mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.getDefault()), EasyMock.anyBoolean()))
				.andReturn(mockBundle1).once();
		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andReturn(mockBundle2).once();

		this.replayAll();

		languageRegistry.onModify(createConfig(LanguageRegistry.SYSTEM_LANGUAGE, "en-US"));

		Assert.assertEquals(Locale.getDefault(), languageRegistry.getDefaultLanguage());
		Assert.assertEquals(Locale.US, languageRegistry.getCurrentLanguage());

		Assert.assertEquals(mockBundle1, languageRegistry.getEntries().get(Locale.getDefault()));
		Assert.assertEquals(mockBundle2, languageRegistry.getEntries().get(Locale.US));

		this.verifyAll();
	}

	@Test
	@PrepareForTest(LanguageRegistryImpl.class)
	public void testGetLocalizedValueNoParams() {
		powerMockActivate();

		ResourceBundle currentBundle = languageRegistry.getEntries().get(Locale.getDefault());

		EasyMock.expect(currentBundle.getString("language.key")).andReturn("Text").once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals("Text", languageRegistry.getLocalizedValue("language.key"));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@PrepareForTest(LanguageRegistryImpl.class)
	public void testGetLocalizedValueParams() {
		powerMockActivate();

		ResourceBundle currentBundle = languageRegistry.getEntries().get(Locale.getDefault());

		EasyMock.expect(currentBundle.getString("language.key")).andReturn("Text %s").once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals("Text PI", languageRegistry.getLocalizedValue("language.key", "PI"));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@PrepareForTest(LanguageRegistryImpl.class)
	public void testGetLocalizedValueNoParamsFallback() {
		powerMockActivate();

		ResourceBundle currentBundle = languageRegistry.getEntries().get(Locale.getDefault());
		ResourceBundle defaultBundle = languageRegistry.getEntries().get(Locale.US);

		EasyMock.expect(currentBundle.getString("language.key"))
				.andThrow(new MissingResourceException("Resource not found", "", "")).once();
		EasyMock.expect(defaultBundle.getString("language.key")).andReturn("Tex").once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals("Tex", languageRegistry.getLocalizedValue("language.key"));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@PrepareForTest(LanguageRegistryImpl.class)
	public void testGetLocalizedValueParamsFallback() {
		powerMockActivate();

		ResourceBundle currentBundle = languageRegistry.getEntries().get(Locale.getDefault());
		ResourceBundle defaultBundle = languageRegistry.getEntries().get(Locale.US);

		EasyMock.expect(currentBundle.getString("language.key"))
				.andThrow(new MissingResourceException("Resource not found", "", "")).once();
		EasyMock.expect(defaultBundle.getString("language.key")).andReturn("Tex %s l %s").once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals("Tex 1 l PI", languageRegistry.getLocalizedValue("language.key", "1", "PI"));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@PrepareForTest(LanguageRegistryImpl.class)
	public void testGetLocalizedValueNoParamsNoMatch() {
		powerMockActivate();

		ResourceBundle currentBundle = languageRegistry.getEntries().get(Locale.getDefault());
		ResourceBundle defaultBundle = languageRegistry.getEntries().get(Locale.US);

		EasyMock.expect(currentBundle.getString("language.key"))
				.andThrow(new MissingResourceException("Resource not found", "", "")).once();
		EasyMock.expect(defaultBundle.getString("language.key"))
				.andThrow(new MissingResourceException("Resource not found", "", "")).once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals("language.key", languageRegistry.getLocalizedValue("language.key"));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@PrepareForTest(LanguageRegistryImpl.class)
	public void testGetDefaultValueNoParams() {
		powerMockActivate();

		ResourceBundle defaultBundle = languageRegistry.getEntries().get(Locale.US);

		EasyMock.expect(defaultBundle.getString("language.key")).andReturn("Text").once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals("Text", languageRegistry.getDefaultValue("language.key"));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@PrepareForTest(LanguageRegistryImpl.class)
	public void testGetDefaultValueParams() {
		powerMockActivate();

		ResourceBundle defaultBundle = languageRegistry.getEntries().get(Locale.US);

		EasyMock.expect(defaultBundle.getString("language.key")).andReturn("Text %s i").once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals("Text l i", languageRegistry.getDefaultValue("language.key", "l"));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@PrepareForTest(LanguageRegistryImpl.class)
	public void testGetDefaultValueNoParamsNoMatch() {
		powerMockActivate();

		ResourceBundle defaultBundle = languageRegistry.getEntries().get(Locale.US);

		EasyMock.expect(defaultBundle.getString("language.key"))
				.andThrow(new MissingResourceException("Resource not found", "", "")).once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals("language.key", languageRegistry.getDefaultValue("language.key"));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	private void activate() {
		ResourceBundle mockBundle1 = createMockResourceBundle();
		ResourceBundle mockBundle2 = createMockResourceBundle();

		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andStubReturn(mockBundle1);
		EasyMock.expect(
				mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.getDefault()), EasyMock.anyBoolean()))
				.andStubReturn(mockBundle2);

		this.replayAll();

		languageRegistry.onActivate(createConfig("en-US", LanguageRegistry.SYSTEM_LANGUAGE));

		this.verifyAll();

		this.resetAll();
	}

	private void powerMockActivate() {
		ResourceBundle mockBundle1 = createPowerMockResourceBundle();
		ResourceBundle mockBundle2 = createPowerMockResourceBundle();

		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andStubReturn(mockBundle1);
		EasyMock.expect(
				mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.getDefault()), EasyMock.anyBoolean()))
				.andStubReturn(mockBundle2);

		this.replayAll();
		PowerMock.replayAll();

		languageRegistry.onActivate(createConfig(Locale.US.toLanguageTag(), Locale.getDefault().toLanguageTag()));

		this.verifyAll();
		PowerMock.verifyAll();

		this.resetAll();
		PowerMock.resetAll();
	}

	private Config createConfig(String defaultLocale, String currentLocale) {
		return new Config() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Config.class;
			}

			@Override
			public String defaultLanguage() {
				return defaultLocale;
			}

			@Override
			public String currentLanguage() {
				return currentLocale;
			}
		};
	}

	private ResourceBundle createMockResourceBundle() {
		ResourceBundle mockBundle = this.createNiceMock(ResourceBundle.class);
		EasyMock.expect(mockBundle.keySet()).andStubReturn(new HashSet<>());
		return mockBundle;
	}

	private ResourceBundle createPowerMockResourceBundle() {
		ResourceBundle mockBundle = PowerMock.createNiceMock(ResourceBundle.class);
		EasyMock.expect(mockBundle.keySet()).andStubReturn(new HashSet<>());
		return mockBundle;
	}

}
package craftedMods.language.provider;

import java.lang.annotation.Annotation;
import java.util.*;

import org.easymock.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.osgi.service.log.*;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import craftedMods.eventManager.api.EventManager;
import craftedMods.language.api.LanguageRegistry;
import craftedMods.language.provider.LanguageRegistryImpl.Configuration;
import craftedMods.preferences.api.*;
import craftedMods.utils.data.LockableTypedProperties;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ResourceBundle.class)
public class LanguageRegistryImplTest extends EasyMockSupport {

	@TestSubject
	public LanguageRegistryImpl languageRegistry = new LanguageRegistryImpl();

	@Mock(type = MockType.NICE)
	private FormatterLogger mockLogger;

	@Mock
	private ResourceBundleLoader mockResourceBundleLoader;

	@Mock
	private PreferencesManager mockPreferencesService;

	@Mock
	private EventManager mockEventManager;

	private Configuration configuration;

	@Before
	public void setup() {
		if (Locale.getDefault().equals(Locale.US)) {
			Locale.setDefault(Locale.GERMANY);
		}

		configuration = new Configuration() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Configuration.class;
			}

			@Override
			public boolean unknownKeysLogged() {
				return true;
			}

			@Override
			public LogLevel unknownKeysLogLevel() {
				return LogLevel.DEBUG;
			}
		};
	}

	@Test
	public void testOnActivateDefaultConfiguration() {
		ResourceBundle mockBundle1 = createMockResourceBundle();
		ResourceBundle mockBundle2 = createMockResourceBundle();

		EasyMock.expect(mockPreferencesService.getPreferences(LanguageRegistryImpl.CONFIG_PID))
				.andReturn(createMockPreferences(Locale.US, Locale.getDefault())).once();

		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andReturn(mockBundle1).once();
		EasyMock.expect(
				mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.getDefault()), EasyMock.anyBoolean()))
				.andReturn(mockBundle2).once();

		Capture<LockableTypedProperties> defaultLanguagePropertiesCapture = EasyMock.newCapture(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_EVENT),
				EasyMock.capture(defaultLanguagePropertiesCapture))).andReturn(null).once();

		Capture<LockableTypedProperties> currentLanguagePropertiesCapture = EasyMock.newCapture(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_EVENT),
				EasyMock.capture(currentLanguagePropertiesCapture))).andReturn(null).once();

		this.replayAll();

		languageRegistry.onActivate(configuration);

		Assert.assertEquals(Locale.US, languageRegistry.getDefaultLanguage());
		Assert.assertEquals(Locale.getDefault(), languageRegistry.getCurrentLanguage());

		Assert.assertEquals(mockBundle1, languageRegistry.getEntries().get(Locale.US));
		Assert.assertEquals(mockBundle2, languageRegistry.getEntries().get(Locale.getDefault()));

		LockableTypedProperties defaultLanguageProperties = defaultLanguagePropertiesCapture.getValue();
		Assert.assertEquals(Locale.US,
				defaultLanguageProperties.getProperty(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_NEW_LANGUAGE));
		Assert.assertNull(
				defaultLanguageProperties.getProperty(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_OLD_LANGUAGE));

		LockableTypedProperties currentLanguageProperties = currentLanguagePropertiesCapture.getValue();
		Assert.assertEquals(Locale.getDefault(),
				currentLanguageProperties.getProperty(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_NEW_LANGUAGE));
		Assert.assertNull(
				currentLanguageProperties.getProperty(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_OLD_LANGUAGE));

		this.verifyAll();
	}

	@Test
	public void testOnActivateCurrentLanguageSet() {
		ResourceBundle mockBundle1 = createMockResourceBundle();
		ResourceBundle mockBundle2 = createMockResourceBundle();

		EasyMock.expect(mockPreferencesService.getPreferences(LanguageRegistryImpl.CONFIG_PID))
				.andReturn(createMockPreferences(Locale.US, Locale.CANADA)).once();

		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andReturn(mockBundle1).once();
		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.CANADA), EasyMock.anyBoolean()))
				.andReturn(mockBundle2).once();

		Capture<LockableTypedProperties> defaultLanguagePropertiesCapture = EasyMock.newCapture(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_EVENT),
				EasyMock.capture(defaultLanguagePropertiesCapture))).andReturn(null).once();

		Capture<LockableTypedProperties> currentLanguagePropertiesCapture = EasyMock.newCapture(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_EVENT),
				EasyMock.capture(currentLanguagePropertiesCapture))).andReturn(null).once();

		this.replayAll();

		languageRegistry.onActivate(configuration);

		Assert.assertEquals(Locale.US, languageRegistry.getDefaultLanguage());
		Assert.assertEquals(Locale.CANADA, languageRegistry.getCurrentLanguage());

		Assert.assertEquals(mockBundle1, languageRegistry.getEntries().get(Locale.US));
		Assert.assertEquals(mockBundle2, languageRegistry.getEntries().get(Locale.CANADA));

		LockableTypedProperties defaultLanguageProperties = defaultLanguagePropertiesCapture.getValue();
		Assert.assertEquals(Locale.US,
				defaultLanguageProperties.getProperty(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_NEW_LANGUAGE));
		Assert.assertNull(
				defaultLanguageProperties.getProperty(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_OLD_LANGUAGE));

		LockableTypedProperties currentLanguageProperties = currentLanguagePropertiesCapture.getValue();
		Assert.assertEquals(Locale.CANADA,
				currentLanguageProperties.getProperty(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_NEW_LANGUAGE));
		Assert.assertNull(
				currentLanguageProperties.getProperty(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_OLD_LANGUAGE));

		this.verifyAll();
	}

	@Test
	public void testOnActivateNullBundles() {
		EasyMock.expect(mockPreferencesService.getPreferences(LanguageRegistryImpl.CONFIG_PID))
				.andReturn(createMockPreferences(Locale.US, Locale.getDefault())).once();

		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andReturn(null).once();
		EasyMock.expect(
				mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.getDefault()), EasyMock.anyBoolean()))
				.andReturn(null).once();

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andStubReturn(null);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andStubReturn(null);

		this.replayAll();

		languageRegistry.onActivate(configuration);

		Assert.assertTrue(languageRegistry.getEntries().isEmpty());

		this.verifyAll();
	}

	@Test(expected = NullPointerException.class)
	public void testSetDefaultLanguageNull() {
		this.replayAll();

		languageRegistry.setDefaultLanguage(null);
	}

	@Test
	public void testSetDefaultLanguageNothingChanged() {
		this.activate();
		this.replayAll();

		Assert.assertFalse(languageRegistry.setDefaultLanguage(Locale.US));
		Assert.assertEquals(Locale.US, languageRegistry.getDefaultLanguage());

		this.verifyAll();
	}

	@Test
	public void testSetDefaultLanguageChanged() {
		Preferences prefs = this.activate();

		EasyMock.expect(
				mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.getDefault()), EasyMock.anyBoolean()))
				.andStubReturn(createMockResourceBundle());

		prefs.setString(LanguageRegistryImpl.DEFAULT_LANGUAGE_KEY, Locale.getDefault().toLanguageTag());
		EasyMock.expectLastCall().once();

		Capture<LockableTypedProperties> propertiesCapture = EasyMock.newCapture(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_EVENT),
				EasyMock.capture(propertiesCapture))).andReturn(null).once();

		this.replayAll();

		Assert.assertTrue(languageRegistry.setDefaultLanguage(Locale.getDefault()));
		Assert.assertEquals(Locale.getDefault(), languageRegistry.getDefaultLanguage());

		LockableTypedProperties properties = propertiesCapture.getValue();
		Assert.assertEquals(Locale.getDefault(),
				properties.getProperty(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_NEW_LANGUAGE));
		Assert.assertEquals(Locale.US, properties.getProperty(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_OLD_LANGUAGE));

		this.verifyAll();
	}

	@Test(expected = NullPointerException.class)
	public void testSetCurrentLanguageNull() {
		this.replayAll();

		languageRegistry.setCurrentLanguage(null);
	}

	@Test
	public void testSetCurrentLanguageNothingChanged() {
		this.activate();
		this.replayAll();

		Assert.assertFalse(languageRegistry.setCurrentLanguage(Locale.getDefault()));
		Assert.assertEquals(Locale.getDefault(), languageRegistry.getCurrentLanguage());

		this.verifyAll();
	}

	@Test
	public void testSetCurrentLanguageChanged() {
		Preferences prefs = this.activate();

		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andStubReturn(createMockResourceBundle());

		prefs.setString(LanguageRegistryImpl.CURRENT_LANGUAGE_KEY, Locale.US.toLanguageTag());
		EasyMock.expectLastCall().once();

		Capture<LockableTypedProperties> propertiesCapture = EasyMock.newCapture(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_EVENT),
				EasyMock.capture(propertiesCapture))).andReturn(null).once();

		this.replayAll();

		Assert.assertTrue(languageRegistry.setCurrentLanguage(Locale.US));
		Assert.assertEquals(Locale.US, languageRegistry.getCurrentLanguage());

		LockableTypedProperties properties = propertiesCapture.getValue();
		Assert.assertEquals(Locale.US, properties.getProperty(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_NEW_LANGUAGE));
		Assert.assertEquals(Locale.getDefault(),
				properties.getProperty(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_OLD_LANGUAGE));

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

		EasyMock.resetToDefault(mockLogger);

		mockLogger.debug((String) EasyMock.anyObject());
		EasyMock.expectLastCall().once();

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

		EasyMock.resetToDefault(mockLogger);

		mockLogger.debug((String) EasyMock.anyObject());
		EasyMock.expectLastCall().once();

		ResourceBundle defaultBundle = languageRegistry.getEntries().get(Locale.US);

		EasyMock.expect(defaultBundle.getString("language.key"))
				.andThrow(new MissingResourceException("Resource not found", "", "")).once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals("language.key", languageRegistry.getDefaultValue("language.key"));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	private Preferences activate() {
		ResourceBundle mockBundle1 = createMockResourceBundle();
		ResourceBundle mockBundle2 = createMockResourceBundle();

		Preferences prefs = createMockPreferences(Locale.US, Locale.getDefault());

		EasyMock.expect(mockPreferencesService.getPreferences(LanguageRegistryImpl.CONFIG_PID)).andReturn(prefs).once();

		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andStubReturn(mockBundle1);
		EasyMock.expect(
				mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.getDefault()), EasyMock.anyBoolean()))
				.andStubReturn(mockBundle2);

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andStubReturn(null);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andStubReturn(null);

		this.replayAll();

		languageRegistry.onActivate(configuration);

		this.verifyAll();

		this.resetAll();
		return prefs;
	}

	private void powerMockActivate() {
		ResourceBundle mockBundle1 = createPowerMockResourceBundle();
		ResourceBundle mockBundle2 = createPowerMockResourceBundle();

		EasyMock.expect(mockPreferencesService.getPreferences(LanguageRegistryImpl.CONFIG_PID))
				.andReturn(createMockPreferences(Locale.US, Locale.getDefault())).once();

		EasyMock.expect(mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.US), EasyMock.anyBoolean()))
				.andStubReturn(mockBundle1);
		EasyMock.expect(
				mockResourceBundleLoader.loadResourceBunde(EasyMock.eq(Locale.getDefault()), EasyMock.anyBoolean()))
				.andStubReturn(mockBundle2);

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.DEFAULT_LANGUAGE_CHANGED_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andStubReturn(null);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(LanguageRegistry.CURRENT_LANGUAGE_CHANGED_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andStubReturn(null);

		this.replayAll();
		PowerMock.replayAll();

		languageRegistry.onActivate(configuration);

		this.verifyAll();
		PowerMock.verifyAll();

		this.resetAll();
		PowerMock.resetAll();
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

	private Preferences createMockPreferences(Locale defaultLanguage, Locale currentLanguage) {
		Preferences mockPreferences = this.createMock(Preferences.class);
		EasyMock.expect(mockPreferences.getString(LanguageRegistryImpl.DEFAULT_LANGUAGE_KEY, Locale.US.toLanguageTag()))
				.andReturn(defaultLanguage.toLanguageTag()).once();
		EasyMock.expect(mockPreferences.getString(LanguageRegistryImpl.CURRENT_LANGUAGE_KEY,
				Locale.getDefault().toLanguageTag())).andReturn(currentLanguage.toLanguageTag()).once();
		return mockPreferences;
	}

}

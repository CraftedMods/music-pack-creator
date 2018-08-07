package craftedMods.preferences.provider;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceException;
import org.osgi.service.log.LogService;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import craftedMods.fileManager.api.FileManager;
import craftedMods.preferences.api.Preferences;
import craftedMods.preferences.provider.PreferencesManagerImpl.Configuration;

@RunWith(PowerMockRunner.class)
@PrepareForTest(PreferencesManagerImpl.class)
public class PreferencesManagerImplTest extends EasyMockSupport {

	@TestSubject
	private PreferencesManagerImpl preferencesManager = new PreferencesManagerImpl();

	@Mock
	private FileManager mockFileManager;

	@Mock(type = MockType.NICE)
	private LogService mockLogger;

	private String configDirString;

	@Before
	public void setup() {
		configDirString = "./config";
	}

	@Test
	public void testOnActivate() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(configDirString)).andReturn(Paths.get(configDirString))
				.once();

		this.replayAll();

		preferencesManager.onActivate(createConfig());

		Assert.assertEquals(configDirString, preferencesManager.getConfigDirString());
		Assert.assertTrue(preferencesManager.getManagedPreferences().isEmpty());

		this.verifyAll();
	}

	@Test
	public void testOnModifyNothingChanged() throws IOException {
		this.activate();

		this.replayAll();

		preferencesManager.onModify(createConfig());

		this.verifyAll();
	}

	@Test
	public void testOnModifyNewConfigDir() throws IOException {
		this.activate();

		String newConfigDirString = "./configurationFolder";

		String pid = "123.234.567";
		PreferencesImpl mockPreference = this.createMock(PreferencesImpl.class);

		preferencesManager.getManagedPreferences().put(pid, mockPreference);

		Path newConfigFilePath = Paths.get(newConfigDirString,
				pid.replace(".", FileSystems.getDefault().getSeparator()) + ".properties");

		EasyMock.expect(mockFileManager.getPathAndCreateDir(newConfigDirString))
				.andReturn(Paths.get(newConfigDirString)).once();
		EasyMock.expect(mockFileManager.getSeparator()).andStubReturn(FileSystems.getDefault().getSeparator());
		EasyMock.expect(mockFileManager.getPathAndCreateFile(Paths.get(newConfigDirString).toString(),
				pid.replace(".", File.separator) + ".properties")).andReturn(newConfigFilePath).once();

		EasyMock.expect(mockPreference.getPID()).andStubReturn(pid);

		mockPreference.setConfigFile(newConfigFilePath);
		EasyMock.expectLastCall().once();

		mockPreference.flush();
		EasyMock.expectLastCall().once();

		this.replayAll();

		preferencesManager.onModify(createConfig(newConfigDirString));

		this.verifyAll();
	}

	@Test
	public void testOnModifyNewConfigDirOneError() throws IOException {
		this.activate();

		String newConfigDirString = "./configurationFolderr";

		String pid1 = "123.234.567";
		String pid2 = "314.156.asd";

		PreferencesImpl mockPreference1 = this.createMock(PreferencesImpl.class);
		PreferencesImpl mockPreference2 = this.createMock(PreferencesImpl.class);

		preferencesManager.getManagedPreferences().put(pid1, mockPreference1);
		preferencesManager.getManagedPreferences().put(pid2, mockPreference2);

		Path newConfigFilePath1 = Paths.get(newConfigDirString,
				pid1.replace(".", FileSystems.getDefault().getSeparator()) + ".properties");
		Path newConfigFilePath2 = Paths.get(newConfigDirString,
				pid2.replace(".", FileSystems.getDefault().getSeparator()) + ".properties");

		EasyMock.expect(mockFileManager.getPathAndCreateDir(newConfigDirString))
				.andReturn(Paths.get(newConfigDirString)).once();
		EasyMock.expect(mockFileManager.getSeparator()).andStubReturn(FileSystems.getDefault().getSeparator());

		EasyMock.expect(mockFileManager.getPathAndCreateFile(Paths.get(newConfigDirString).toString(),
				pid1.replace(".", File.separator) + ".properties")).andReturn(newConfigFilePath1).once();
		EasyMock.expect(mockFileManager.getPathAndCreateFile(Paths.get(newConfigDirString).toString(),
				pid2.replace(".", File.separator) + ".properties")).andReturn(newConfigFilePath2).once();

		EasyMock.expect(mockPreference1.getPID()).andStubReturn(pid1);
		EasyMock.expect(mockPreference2.getPID()).andStubReturn(pid2);

		mockPreference1.setConfigFile(newConfigFilePath1);
		EasyMock.expectLastCall().once();

		mockPreference2.setConfigFile(newConfigFilePath2);
		EasyMock.expectLastCall().once();

		mockPreference1.flush();
		EasyMock.expectLastCall().andThrow(new IOException("An IO-Error occured")).once();

		mockPreference2.flush();
		EasyMock.expectLastCall().once();

		this.replayAll();

		preferencesManager.onModify(createConfig(newConfigDirString));

		this.verifyAll();
	}

	@Test
	public void testOnDeactivate() throws IOException {
		this.activate();

		String pid1 = "123.234.567";

		PreferencesImpl mockPreference1 = this.createMock(PreferencesImpl.class);

		preferencesManager.getManagedPreferences().put(pid1, mockPreference1);

		EasyMock.expect(mockPreference1.getPID()).andStubReturn(pid1);

		mockPreference1.flush();
		EasyMock.expectLastCall().once();

		this.replayAll();

		preferencesManager.onDeactivate();

		Assert.assertTrue(preferencesManager.getManagedPreferences().isEmpty());

		this.verifyAll();
	}

	@Test
	public void testOnDeactivateOneError() throws IOException {
		this.activate();

		String pid1 = "123.234.567";
		String pid2 = "314.156.asd";

		PreferencesImpl mockPreference1 = this.createMock(PreferencesImpl.class);
		PreferencesImpl mockPreference2 = this.createMock(PreferencesImpl.class);

		preferencesManager.getManagedPreferences().put(pid1, mockPreference1);
		preferencesManager.getManagedPreferences().put(pid2, mockPreference2);

		EasyMock.expect(mockPreference1.getPID()).andStubReturn(pid1);
		EasyMock.expect(mockPreference2.getPID()).andStubReturn(pid2);

		mockPreference1.flush();
		EasyMock.expectLastCall().andThrow(new IOException("IO Error")).once();
		mockPreference2.flush();
		EasyMock.expectLastCall().once();

		this.replayAll();

		preferencesManager.onDeactivate();

		Assert.assertTrue(preferencesManager.getManagedPreferences().isEmpty());

		this.verifyAll();
	}

	@Test
	public void testGetPreferencesAlreadyPresent() throws IOException {
		this.activate();

		String pid = "123.234.567";

		PreferencesImpl mockPreference = this.createMock(PreferencesImpl.class);

		preferencesManager.getManagedPreferences().put(pid, mockPreference);

		EasyMock.expect(mockPreference.getPID()).andStubReturn(pid);

		this.replayAll();

		Preferences prefs = preferencesManager.getPreferences(pid);

		Assert.assertEquals(mockPreference, prefs);

		this.verifyAll();
	}

	@Test
	public void testGetPreferencesNotPresent() throws Exception {
		this.activate();

		String pid = "asd.asdasdsadsadasdasd.0";

		Path configFilePath = Paths.get(Paths.get(configDirString).toString(),
				pid.replace(".", File.separator) + ".properties");

		EasyMock.expect(mockFileManager.getSeparator()).andStubReturn(FileSystems.getDefault().getSeparator());
		EasyMock.expect(mockFileManager.getPathAndCreateFile(Paths.get(configDirString).toString(),
				pid.replace(".", File.separator) + ".properties")).andReturn(configFilePath);

		PreferencesImpl mockPreference = this.createMock(PreferencesImpl.class);

		EasyMock.expect(mockPreference.getPID()).andStubReturn(pid);

		PowerMock.expectNew(PreferencesImpl.class, pid, configFilePath, mockFileManager).andReturn(mockPreference);

		mockPreference.sync();
		EasyMock.expectLastCall().once();

		this.replayAll();
		PowerMock.replayAll();

		Preferences prefs = preferencesManager.getPreferences(pid);

		Assert.assertEquals(prefs, preferencesManager.getManagedPreferences().get(pid));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test(expected = ServiceException.class)
	public void testGetPreferencesNotPresentConfigFileError() throws Exception {
		this.activate();

		String pid = "asd.asdasdsadsadasdasd.0";

		EasyMock.expect(mockFileManager.getSeparator()).andStubReturn(FileSystems.getDefault().getSeparator());
		EasyMock.expect(mockFileManager.getPathAndCreateFile(EasyMock.anyString(), EasyMock.anyString()))
				.andThrow(new IOException("Couldn't create the config file"));

		this.replayAll();

		preferencesManager.getPreferences(pid);
	}

	@Test(expected = ServiceException.class)
	public void testGetPreferencesSyncError() throws Exception {
		this.activate();

		String pid = "asd.asdasdsadsadasdasd.0";

		Path configFilePath = Paths.get(Paths.get(configDirString).toString(),
				pid.replace(".", File.separator) + ".properties");

		EasyMock.expect(mockFileManager.getSeparator()).andStubReturn(FileSystems.getDefault().getSeparator());
		EasyMock.expect(mockFileManager.getPathAndCreateFile(Paths.get(configDirString).toString(),
				pid.replace(".", File.separator) + ".properties")).andReturn(configFilePath);

		PreferencesImpl mockPreference = this.createMock(PreferencesImpl.class);

		EasyMock.expect(mockPreference.getPID()).andStubReturn(pid);

		PowerMock.expectNew(PreferencesImpl.class, pid, configFilePath, mockFileManager).andReturn(mockPreference);

		mockPreference.sync();
		EasyMock.expectLastCall().andThrow(new IOException("Couldn't sync the config file")).once();

		this.replayAll();
		PowerMock.replayAll();

		preferencesManager.getPreferences(pid);
	}

	private void activate() throws IOException {
		EasyMock.expect(mockFileManager.getPathAndCreateDir(configDirString)).andStubReturn(Paths.get(configDirString));

		this.replayAll();

		preferencesManager.onActivate(createConfig());

		this.resetAll();
	}

	private Configuration createConfig() {
		return createConfig(configDirString);
	}

	private Configuration createConfig(String configDirString) {
		return new Configuration() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return Configuration.class;
			}

			@Override
			public String configurationDirectory() {
				return configDirString;
			}
		};
	}

}

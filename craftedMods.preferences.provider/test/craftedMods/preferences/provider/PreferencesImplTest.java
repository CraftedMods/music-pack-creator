package craftedMods.preferences.provider;

import java.nio.file.Paths;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import craftedMods.fileManager.api.FileManager;

public class PreferencesImplTest extends EasyMockSupport {

	private PreferencesImpl impl;

	@Before
	public void setup() {
		impl = new PreferencesImpl("dummy", Paths.get("q"), this.createMock(FileManager.class));
		this.replayAll();
	}

	@Test(expected = NullPointerException.class)
	public void testCreateNullPID() {
		new PreferencesImpl(null, Paths.get("q"), this.createMock(FileManager.class));
	}

	@Test(expected = NullPointerException.class)
	public void testCreateNullConfigFile() {
		new PreferencesImpl("oid", null, this.createMock(FileManager.class));
	}

	@Test(expected = NullPointerException.class)
	public void testCreateNullFileManager() {
		new PreferencesImpl("oid", Paths.get("q"), null);
	}

	@Test(expected = NullPointerException.class)
	public void testSetConfigFileNull() {
		impl.setConfigFile(null);
	}

}

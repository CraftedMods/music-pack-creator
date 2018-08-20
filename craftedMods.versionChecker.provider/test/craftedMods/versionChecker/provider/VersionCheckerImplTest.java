package craftedMods.versionChecker.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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

import craftedMods.versionChecker.api.RemoteVersion;
import craftedMods.versionChecker.api.SemanticVersion;

@RunWith(PowerMockRunner.class)
@PrepareForTest(VersionCheckerImpl.class)
public class VersionCheckerImplTest extends EasyMockSupport {

	@TestSubject
	public VersionCheckerImpl versionChecker = new VersionCheckerImpl();

	@Mock(type = MockType.NICE)
	private LogService logger;

	@Before
	public void setup() {

	}

	@Test(expected = NullPointerException.class)
	public void testRetrieveRemoteVersionNullURL() {
		versionChecker.retrieveRemoteVersion(null);
	}

	@Test
	public void testRetrieveRemoteVersion() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		byte[] versionFile = "1.2.0-BETA|http://google.com/download|http://google.com/changelog".getBytes();

		URLConnection mockURLConnection = this.createNiceMock(URLConnection.class);

		EasyMock.expect(mockURL.openConnection()).andStubReturn(mockURLConnection);

		try (InputStream in = new ByteArrayInputStream(versionFile)) {

			EasyMock.expect(mockURL.openStream()).andReturn(in).once();

			this.replayAll();
			PowerMock.replayAll();

			RemoteVersion version = versionChecker.retrieveRemoteVersion(mockURL);

			Assert.assertEquals("1.2.0-BETA", version.getRemoteVersion().toString());
			Assert.assertEquals("http://google.com/download", version.getDownloadURL().toString());
			Assert.assertEquals("http://google.com/changelog", version.getChangelogURL().toString());

			this.verifyAll();
			PowerMock.verifyAll();
		}
	}

	@Test
	public void testRetrieveRemoteVersionNoDownload() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		byte[] versionFile = "1.2.0-BETA||http://google.com/changelog".getBytes();

		URLConnection mockURLConnection = this.createNiceMock(URLConnection.class);

		EasyMock.expect(mockURL.openConnection()).andStubReturn(mockURLConnection);

		try (InputStream in = new ByteArrayInputStream(versionFile)) {

			EasyMock.expect(mockURL.openStream()).andReturn(in).once();

			this.replayAll();
			PowerMock.replayAll();

			RemoteVersion version = versionChecker.retrieveRemoteVersion(mockURL);

			Assert.assertEquals("1.2.0-BETA", version.getRemoteVersion().toString());
			Assert.assertNull(version.getDownloadURL());
			Assert.assertEquals("http://google.com/changelog", version.getChangelogURL().toString());

			this.verifyAll();
			PowerMock.verifyAll();
		}
	}

	@Test
	public void testRetrieveRemoteVersionNoChangelog() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		byte[] versionFile = "1.2.0-BETA|http://google.com/download|".getBytes();

		URLConnection mockURLConnection = this.createNiceMock(URLConnection.class);

		EasyMock.expect(mockURL.openConnection()).andStubReturn(mockURLConnection);

		try (InputStream in = new ByteArrayInputStream(versionFile)) {

			EasyMock.expect(mockURL.openStream()).andReturn(in).once();

			this.replayAll();
			PowerMock.replayAll();

			RemoteVersion version = versionChecker.retrieveRemoteVersion(mockURL);

			Assert.assertEquals("1.2.0-BETA", version.getRemoteVersion().toString());
			Assert.assertEquals("http://google.com/download", version.getDownloadURL().toString());
			Assert.assertNull(version.getChangelogURL());

			this.verifyAll();
			PowerMock.verifyAll();
		}
	}

	@Test
	public void testRetrieveRemoteVersionNoChangelogNoSeparator() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		byte[] versionFile = "1.2.0-BETA|http://google.com/download".getBytes();

		URLConnection mockURLConnection = this.createNiceMock(URLConnection.class);

		EasyMock.expect(mockURL.openConnection()).andStubReturn(mockURLConnection);

		try (InputStream in = new ByteArrayInputStream(versionFile)) {

			EasyMock.expect(mockURL.openStream()).andReturn(in).once();

			this.replayAll();
			PowerMock.replayAll();

			RemoteVersion version = versionChecker.retrieveRemoteVersion(mockURL);

			Assert.assertEquals("1.2.0-BETA", version.getRemoteVersion().toString());
			Assert.assertEquals("http://google.com/download", version.getDownloadURL().toString());
			Assert.assertNull(version.getChangelogURL());

			this.verifyAll();
			PowerMock.verifyAll();
		}
	}

	@Test
	public void testRetrieveRemoteVersionNoDownloadNoChangelog() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		byte[] versionFile = "1.2.0-BETA||".getBytes();

		URLConnection mockURLConnection = this.createNiceMock(URLConnection.class);

		EasyMock.expect(mockURL.openConnection()).andStubReturn(mockURLConnection);

		try (InputStream in = new ByteArrayInputStream(versionFile)) {

			EasyMock.expect(mockURL.openStream()).andReturn(in).once();

			this.replayAll();
			PowerMock.replayAll();

			RemoteVersion version = versionChecker.retrieveRemoteVersion(mockURL);

			Assert.assertEquals("1.2.0-BETA", version.getRemoteVersion().toString());
			Assert.assertNull(version.getDownloadURL());
			Assert.assertNull(version.getChangelogURL());

			this.verifyAll();
			PowerMock.verifyAll();
		}
	}

	@Test
	public void testRetrieveRemoteVersionNoDownloadNoChangelogNoLastSeparator() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		byte[] versionFile = "1.2.0-BETA|".getBytes();

		URLConnection mockURLConnection = this.createNiceMock(URLConnection.class);

		EasyMock.expect(mockURL.openConnection()).andStubReturn(mockURLConnection);

		try (InputStream in = new ByteArrayInputStream(versionFile)) {

			EasyMock.expect(mockURL.openStream()).andReturn(in).once();

			this.replayAll();
			PowerMock.replayAll();

			RemoteVersion version = versionChecker.retrieveRemoteVersion(mockURL);

			Assert.assertEquals("1.2.0-BETA", version.getRemoteVersion().toString());
			Assert.assertNull(version.getDownloadURL());
			Assert.assertNull(version.getChangelogURL());

			this.verifyAll();
			PowerMock.verifyAll();
		}
	}

	@Test
	public void testRetrieveRemoteVersionNoDownloadNoChangelogSeparators() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		byte[] versionFile = "1.2.0-BETA".getBytes();

		URLConnection mockURLConnection = this.createNiceMock(URLConnection.class);

		EasyMock.expect(mockURL.openConnection()).andStubReturn(mockURLConnection);

		try (InputStream in = new ByteArrayInputStream(versionFile)) {

			EasyMock.expect(mockURL.openStream()).andReturn(in).once();

			this.replayAll();
			PowerMock.replayAll();

			RemoteVersion version = versionChecker.retrieveRemoteVersion(mockURL);

			Assert.assertEquals("1.2.0-BETA", version.getRemoteVersion().toString());
			Assert.assertNull(version.getDownloadURL());
			Assert.assertNull(version.getChangelogURL());

			this.verifyAll();
			PowerMock.verifyAll();
		}
	}

	@Test
	public void testRetrieveRemoteVersionEmptyVersionString() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		byte[] versionFile = "".getBytes();

		URLConnection mockURLConnection = this.createNiceMock(URLConnection.class);

		EasyMock.expect(mockURL.openConnection()).andStubReturn(mockURLConnection);

		try (InputStream in = new ByteArrayInputStream(versionFile)) {

			EasyMock.expect(mockURL.openStream()).andReturn(in).once();

			this.replayAll();
			PowerMock.replayAll();

			Assert.assertNull(versionChecker.retrieveRemoteVersion(mockURL));

			this.verifyAll();
			PowerMock.verifyAll();
		}
	}

	@Test
	public void testRetrieveRemoteVersionInvalidVersion() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		byte[] versionFile = "1.2.3.3-BETA".getBytes();

		URLConnection mockURLConnection = this.createNiceMock(URLConnection.class);

		EasyMock.expect(mockURL.openConnection()).andStubReturn(mockURLConnection);

		try (InputStream in = new ByteArrayInputStream(versionFile)) {

			EasyMock.expect(mockURL.openStream()).andReturn(in).once();

			this.replayAll();
			PowerMock.replayAll();

			Assert.assertNull(versionChecker.retrieveRemoteVersion(mockURL));

			this.verifyAll();
			PowerMock.verifyAll();
		}
	}

	@Test
	public void testRetrieveRemoteVersionInvalidDownloadURL() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		byte[] versionFile = "1.2.3-BETA|wer".getBytes();

		URLConnection mockURLConnection = this.createNiceMock(URLConnection.class);

		EasyMock.expect(mockURL.openConnection()).andStubReturn(mockURLConnection);

		try (InputStream in = new ByteArrayInputStream(versionFile)) {

			EasyMock.expect(mockURL.openStream()).andReturn(in).once();

			this.replayAll();
			PowerMock.replayAll();

			Assert.assertNull(versionChecker.retrieveRemoteVersion(mockURL));

			this.verifyAll();
			PowerMock.verifyAll();
		}
	}

	@Test
	public void testRetrieveRemoteVersionInvalidChangelogURL() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		byte[] versionFile = "1.2.3-BETA|http://google.de|kjkhnibz/u".getBytes();

		URLConnection mockURLConnection = this.createNiceMock(URLConnection.class);

		EasyMock.expect(mockURL.openConnection()).andStubReturn(mockURLConnection);

		try (InputStream in = new ByteArrayInputStream(versionFile)) {

			EasyMock.expect(mockURL.openStream()).andReturn(in).once();

			this.replayAll();
			PowerMock.replayAll();

			Assert.assertNull(versionChecker.retrieveRemoteVersion(mockURL));

			this.verifyAll();
			PowerMock.verifyAll();
		}
	}

	@Test
	public void testRetrieveRemoteVersionNoConnection() throws IOException {
		URL mockURL = PowerMock.createNiceMock(URL.class);

		EasyMock.expect(mockURL.openConnection()).andStubThrow(new IOException("IO Error"));

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertNull(versionChecker.retrieveRemoteVersion(mockURL));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test(expected = NullPointerException.class)
	public void testIsNewVersionAvailableNullLocalVersion() {
		versionChecker.isNewVersionAvailable(null, this.createMock(RemoteVersion.class));
	}

	@Test
	public void testIsNewVersionAvailableNullSemanticVersion() {
		Assert.assertFalse(versionChecker.isNewVersionAvailable(this.createMock(SemanticVersion.class), null));
	}

	@Test
	public void testIsNewVersionAvailableSameRemoteVersion() {
		SemanticVersion mockLocalVersion = this.createMock(SemanticVersion.class);
		RemoteVersion mockRemoteVersion = this.createMock(RemoteVersion.class);
		SemanticVersion mockRemoteSemanticVersion = this.createMock(SemanticVersion.class);

		EasyMock.expect(mockRemoteVersion.getRemoteVersion()).andStubReturn(mockRemoteSemanticVersion);
		EasyMock.expect(mockLocalVersion.compareTo(mockRemoteSemanticVersion)).andReturn(0).once();

		this.replayAll();

		Assert.assertFalse((versionChecker.isNewVersionAvailable(mockLocalVersion, mockRemoteVersion)));

		this.verifyAll();
	}

	@Test
	public void testIsNewVersionAvailableOlderRemoteVersion() {
		SemanticVersion mockLocalVersion = this.createMock(SemanticVersion.class);
		RemoteVersion mockRemoteVersion = this.createMock(RemoteVersion.class);
		SemanticVersion mockRemoteSemanticVersion = this.createMock(SemanticVersion.class);

		EasyMock.expect(mockRemoteVersion.getRemoteVersion()).andStubReturn(mockRemoteSemanticVersion);
		EasyMock.expect(mockLocalVersion.compareTo(mockRemoteSemanticVersion)).andReturn(1).once();

		this.replayAll();

		Assert.assertFalse((versionChecker.isNewVersionAvailable(mockLocalVersion, mockRemoteVersion)));

		this.verifyAll();
	}

	@Test
	public void testIsNewVersionAvailableNewerRemoteVersion() {
		SemanticVersion mockLocalVersion = this.createMock(SemanticVersion.class);
		RemoteVersion mockRemoteVersion = this.createMock(RemoteVersion.class);
		SemanticVersion mockRemoteSemanticVersion = this.createMock(SemanticVersion.class);

		EasyMock.expect(mockRemoteVersion.getRemoteVersion()).andStubReturn(mockRemoteSemanticVersion);
		EasyMock.expect(mockLocalVersion.compareTo(mockRemoteSemanticVersion)).andReturn(-1).once();

		this.replayAll();

		Assert.assertTrue((versionChecker.isNewVersionAvailable(mockLocalVersion, mockRemoteVersion)));

		this.verifyAll();
	}

}

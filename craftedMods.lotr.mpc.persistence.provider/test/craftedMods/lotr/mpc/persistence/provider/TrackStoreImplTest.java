package craftedMods.lotr.mpc.persistence.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.core.api.MusicPackProject;

@RunWith(EasyMockRunner.class)
public class TrackStoreImplTest extends EasyMockSupport {

	private TrackStoreImpl trackStore;

	@Mock
	private MusicPackProject mockMusicPackProject;

	private Path storeDir;

	@Mock
	private FileManager mockFileManager;

	@Before
	public void setup() {
		storeDir = Paths.get("tracks");
		trackStore = new TrackStoreImpl(mockMusicPackProject, storeDir, mockFileManager);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetStoredTracksIsUnmodifiable() {
		trackStore.getStoredTracks().add("");
	}

	@Test(expected = NullPointerException.class)
	public void tesOpenInputStreamNull() throws IOException {
		trackStore.openInputStream(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOpenInputStreamUnregistered() throws IOException {
		trackStore.openInputStream("Track1");
	}

	@Test
	public void testOpenInputStream() throws IOException {
		Path trackPath = storeDir.resolve("Track2");
		trackStore.getStoredTracksMap().put("Track2", trackPath);

		InputStream mockInputStream = this.createMock(InputStream.class);

		EasyMock.expect(mockFileManager.newInputStream(trackPath)).andReturn(mockInputStream).once();

		this.replayAll();

		InputStream in = trackStore.openInputStream("Track2");

		Assert.assertEquals(mockInputStream, in);

		this.verifyAll();
	}

	@Test(expected = NullPointerException.class)
	public void testOpenOutputStreamNull() throws IOException {
		trackStore.openOutputStream(null);
	}

	@Test
	public void testOpenOutputStreamUnregistered() throws IOException {
		Path trackPath = storeDir.resolve("Track3");

		OutputStream mockOutputStream = this.createMock(OutputStream.class);

		EasyMock.expect(mockFileManager.getPathAndCreateFile(storeDir.toString(), "Track3")).andReturn(trackPath)
				.once();
		EasyMock.expect(mockFileManager.newOutputStream(trackPath)).andReturn(mockOutputStream).once();

		this.replayAll();

		trackStore.openOutputStream("Track3");

		Assert.assertEquals(trackPath, trackStore.getStoredTracksMap().get("Track3"));

		this.verifyAll();
	}

	@Test
	public void testOpenOutputStreamRegistered() throws IOException {
		Path trackPath = storeDir.resolve("Track3");

		trackStore.getStoredTracksMap().put("Track3", trackPath);

		OutputStream mockOutputStream = this.createMock(OutputStream.class);

		EasyMock.expect(mockFileManager.newOutputStream(trackPath)).andReturn(mockOutputStream).once();

		this.replayAll();

		trackStore.openOutputStream("Track3");

		this.verifyAll();
	}

	@Test(expected = NullPointerException.class)
	public void testDeleteTrackNull() throws IOException {
		trackStore.deleteTrack(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteTrackUnregistered() throws IOException {
		trackStore.deleteTrack("Track4");
	}

	@Test
	public void testDeleteTrackRegistered() throws IOException {
		Path trackPath = storeDir.resolve("Track4");

		trackStore.getStoredTracksMap().put("Track4", trackPath);

		EasyMock.expect(mockFileManager.deleteFile(trackPath)).andReturn(true).once();

		this.replayAll();

		trackStore.deleteTrack("Track4");

		Assert.assertTrue(trackStore.getStoredTracksMap().isEmpty());

		this.verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testRefreshNoTracks() throws IOException {
		Path path = Paths.get("Path", "to", "Track");
		trackStore.getStoredTracksMap().put("Track", path);

		Stream<Path> mockStream = this.createMock(Stream.class);

		EasyMock.expect(mockFileManager.getPathsInDirectory(storeDir)).andReturn(mockStream).once();

		mockStream.forEach(EasyMock.anyObject(Consumer.class));
		EasyMock.expectLastCall().once();

		mockStream.close();
		EasyMock.expectLastCall().once();

		this.replayAll();

		trackStore.refresh();

		Assert.assertTrue(trackStore.getStoredTracksMap().isEmpty());

		this.verifyAll();
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testRefresh() throws IOException {
		Stream<Path> mockStream = this.createMock(Stream.class);

		EasyMock.expect(mockFileManager.getPathsInDirectory(storeDir)).andReturn(mockStream).once();

		Capture<Consumer> consumerCapture = Capture.newInstance();

		mockStream.forEach(EasyMock.capture(consumerCapture));
		EasyMock.expectLastCall().once();

		mockStream.close();
		EasyMock.expectLastCall().once();

		this.replayAll();

		trackStore.refresh();
		
		Consumer consumer = consumerCapture.getValue();

		Path track1 = storeDir.resolve("Track1");
		Path track2 = storeDir.resolve("Track2");
		Path track3 = storeDir.resolve("Track3");

		consumer.accept(track1);
		consumer.accept(track2);
		consumer.accept(track3);

		Assert.assertEquals(3, trackStore.getStoredTracks().size());
		Assert.assertEquals(track1, trackStore.getStoredTracksMap().get("Track1"));
		Assert.assertEquals(track2, trackStore.getStoredTracksMap().get("Track2"));
		Assert.assertEquals(track3, trackStore.getStoredTracksMap().get("Track3"));

		this.verifyAll();
	}

}

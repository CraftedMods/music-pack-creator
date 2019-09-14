package craftedMods.lotr.mpc.core.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.service.log.FormatterLogger;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.gson.JsonSyntaxException;

import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.eventManager.base.EventUtils;
import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.core.api.MusicPack;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.core.api.MusicPackProjectExporter;
import craftedMods.lotr.mpc.core.api.MusicPackProjectImporter;
import craftedMods.lotr.mpc.core.api.MusicPackProjectManager;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.lotr.mpc.persistence.api.TrackStore;
import craftedMods.lotr.mpc.persistence.api.TrackStoreManager;
import craftedMods.utils.Utils;
import craftedMods.utils.data.CollectionUtils;
import craftedMods.utils.data.ExtendedProperties;
import craftedMods.utils.data.NonNullSet;
import craftedMods.utils.data.PrimitiveProperties;
import craftedMods.versionChecker.api.SemanticVersion;
import junit.framework.AssertionFailedError;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MusicPackProjectImporterImpl.class, EventUtils.class, Utils.class, FileSystems.class })
public class MusicPackProjectImporterImplTest extends EasyMockSupport {

	@TestSubject
	public MusicPackProjectImporterImpl importer = new MusicPackProjectImporterImpl();

	@Mock(type = MockType.NICE)
	private FormatterLogger mockLogger;

	@Mock(type = MockType.NICE)
	private SemanticVersion mockVersion;

	@Mock
	private EventManager mockEventManager;

	@Mock
	private FileManager mockFileManager;

	@Mock
	private MusicPackJSONFileReader mockReader;

	@Mock
	private TrackStoreManager mockTrackStoreManager;

	@Mock
	private MusicPackProjectManager mockMusicPackProjectManager;

	private String packName;

	@Before
	public void setup() {
		packName = "pack";
	}

	@Test(expected = NullPointerException.class)
	public void testExportNullLocation() {
		importer.importMusicPackProject(null);
	}

	@Test
	public void testExportNonExistingLocation() {
		Path mockPath = this.createMock(Path.class);

		EasyMock.expect(mockFileManager.exists(mockPath)).andReturn(false).atLeastOnce();

		this.replayAll();

		try {
			importer.importMusicPackProject(mockPath);
			throw new AssertionFailedError("The function did not throw an exception");
		} catch (Exception e) {
			Assert.assertEquals(UncheckedIOException.class, e.getClass());
		}

		this.verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportPack() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn("pack2").times(2);

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack2")).andReturn("pack2").once();
		mockPackImpl.setName("pack2");
		EasyMock.expectLastCall().once();
		packName = "pack2";

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).once();
		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		byte[] readContent = new byte[] { 1, 2, 3 };
		List<Track> readTracksList = new ArrayList<>();
		MusicPack musicPack = this.createMock(MusicPack.class);
		NonNullSet<Track> packTracksSet = this.createMock(NonNullSet.class);

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andReturn(readTracksList).once();

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(musicPack).once();
		EasyMock.expect(musicPack.getTracks()).andReturn(packTracksSet);
		EasyMock.expect(packTracksSet.addAll(readTracksList)).andReturn(true).once();

		Path mockTracksDir = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.TRACKS_DIR)).andReturn(mockTracksDir).once();
		EasyMock.expect(mockFileManager.exists(mockTracksDir)).andReturn(true).once();

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockPackImpl)).andReturn(mockTrackStore).once();

		Path trackFile1 = Paths.get("track1.ogg");
		Path trackFile2 = Paths.get("track2.ogg");
		Path trackFile3 = Paths.get("track3.ogg");

		Collection<Path> trackFiles = new ArrayList<>();

		trackFiles.add(trackFile1);
		trackFiles.add(trackFile2);
		trackFiles.add(trackFile3);

		EasyMock.expect(mockFileManager.getPathsInDirectory(mockTracksDir)).andReturn(trackFiles.stream()).once();

		Capture<WriteableEventProperties> trackCountDeterminedEventPropertiesCapture = Capture
				.newInstance(CaptureType.ALL);
		EasyMock.expect(
				mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT),
						EasyMock.capture(trackCountDeterminedEventPropertiesCapture)))
				.andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_RESULT_PROCEED))).andReturn(true)
				.once();

		Capture<WriteableEventProperties> copyingTrackEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT),
				EasyMock.capture(copyingTrackEventPropertiesCapture))).andReturn(null).times(3);

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT_RESULT_PROCEED))).andReturn(true).times(3);

		InputStream track1InputStream = this.createMock(InputStream.class);
		InputStream track2InputStream = this.createMock(InputStream.class);
		InputStream track3InputStream = this.createMock(InputStream.class);

		track1InputStream.close();
		EasyMock.expectLastCall().once();

		track2InputStream.close();
		EasyMock.expectLastCall().once();

		track3InputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(trackFile1)).andReturn(track1InputStream).once();
		EasyMock.expect(mockFileManager.newInputStream(trackFile2)).andReturn(track2InputStream).once();
		EasyMock.expect(mockFileManager.newInputStream(trackFile3)).andReturn(track3InputStream).once();

		OutputStream track1OutputStream = this.createMock(OutputStream.class);
		OutputStream track2OutputStream = this.createMock(OutputStream.class);
		OutputStream track3OutputStream = this.createMock(OutputStream.class);

		track1OutputStream.close();
		EasyMock.expectLastCall().once();

		track2OutputStream.close();
		EasyMock.expectLastCall().once();

		track3OutputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockTrackStore.openOutputStream(trackFile1.getFileName().toString()))
				.andReturn(track1OutputStream).once();
		EasyMock.expect(mockTrackStore.openOutputStream(trackFile2.getFileName().toString()))
				.andReturn(track2OutputStream).once();
		EasyMock.expect(mockTrackStore.openOutputStream(trackFile3.getFileName().toString()))
				.andReturn(track3OutputStream).once();

		Utils.writeFromInputStreamToOutputStream(track1InputStream, track1OutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track2InputStream, track2OutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track3InputStream, track3OutputStream);
		EasyMock.expectLastCall().once();

		Capture<WriteableEventProperties> preSuccessEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT),
				EasyMock.capture(preSuccessEventPropertiesCapture))).andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT_RESULT_PROCEED))).andReturn(true);

		Capture<WriteableEventProperties> successEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.SUCCESS_EVENT),
				EasyMock.capture(successEventPropertiesCapture))).andReturn(null).once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals(mockPackImpl, importer.importMusicPackProject(importLocation));

		WriteableEventProperties packPropertiesEventProps = readingFileEventPropertiesCapture.getValues().get(0);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties trackCountDeterminedEventProps = trackCountDeterminedEventPropertiesCapture.getValue();
		Assert.assertEquals(3l, (long) trackCountDeterminedEventProps
				.getProperty(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_TRACK_COUNT));
		Assert.assertEquals(importLocation,
				trackCountDeterminedEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		for (int i = 0; i < 3; i++) {
			WriteableEventProperties copyingTrackEventProps = copyingTrackEventPropertiesCapture.getValues().get(i);
			Assert.assertEquals("track" + (i + 1) + ".ogg",
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COPYING_TRACK_EVENT_TRACK_NAME));
			Assert.assertEquals(importLocation,
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));
		}

		WriteableEventProperties preSuccessEventProps = preSuccessEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				preSuccessEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties successEventProps = successEventPropertiesCapture.getValue();
		Assert.assertEquals("pack2",
				successEventProps.getProperty(MusicPackProjectImporter.SUCCESS_EVENT_PROJECT_NAME));
		Assert.assertEquals(importLocation,
				successEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportPackNoPackFile() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(false).once();

		// Reading file event is captured above

		Capture<WriteableEventProperties> fileNotFoundEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.FILE_NOT_FOUND_EVENT),
				EasyMock.capture(fileNotFoundEventPropertiesCapture))).andReturn(null).times(1);

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		byte[] readContent = new byte[] { 1, 2, 3 };
		List<Track> readTracksList = new ArrayList<>();
		MusicPack musicPack = this.createMock(MusicPack.class);
		NonNullSet<Track> packTracksSet = this.createMock(NonNullSet.class);

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andReturn(readTracksList).once();

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(musicPack).once();
		EasyMock.expect(musicPack.getTracks()).andReturn(packTracksSet);
		EasyMock.expect(packTracksSet.addAll(readTracksList)).andReturn(true).once();

		Path mockTracksDir = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.TRACKS_DIR)).andReturn(mockTracksDir).once();
		EasyMock.expect(mockFileManager.exists(mockTracksDir)).andReturn(true).once();

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockPackImpl)).andReturn(mockTrackStore).once();

		Path trackFile1 = Paths.get("track1.ogg");
		Path trackFile2 = Paths.get("track2.ogg");
		Path trackFile3 = Paths.get("track3.ogg");

		Collection<Path> trackFiles = new ArrayList<>();

		trackFiles.add(trackFile1);
		trackFiles.add(trackFile2);
		trackFiles.add(trackFile3);

		EasyMock.expect(mockFileManager.getPathsInDirectory(mockTracksDir)).andReturn(trackFiles.stream()).once();

		Capture<WriteableEventProperties> trackCountDeterminedEventPropertiesCapture = Capture
				.newInstance(CaptureType.ALL);
		EasyMock.expect(
				mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT),
						EasyMock.capture(trackCountDeterminedEventPropertiesCapture)))
				.andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_RESULT_PROCEED))).andReturn(true)
				.once();

		Capture<WriteableEventProperties> copyingTrackEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT),
				EasyMock.capture(copyingTrackEventPropertiesCapture))).andReturn(null).times(3);

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT_RESULT_PROCEED))).andReturn(true).times(3);

		InputStream track1InputStream = this.createMock(InputStream.class);
		InputStream track2InputStream = this.createMock(InputStream.class);
		InputStream track3InputStream = this.createMock(InputStream.class);

		track1InputStream.close();
		EasyMock.expectLastCall().once();

		track2InputStream.close();
		EasyMock.expectLastCall().once();

		track3InputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(trackFile1)).andReturn(track1InputStream).once();
		EasyMock.expect(mockFileManager.newInputStream(trackFile2)).andReturn(track2InputStream).once();
		EasyMock.expect(mockFileManager.newInputStream(trackFile3)).andReturn(track3InputStream).once();

		OutputStream track1OutputStream = this.createMock(OutputStream.class);
		OutputStream track2OutputStream = this.createMock(OutputStream.class);
		OutputStream track3OutputStream = this.createMock(OutputStream.class);

		track1OutputStream.close();
		EasyMock.expectLastCall().once();

		track2OutputStream.close();
		EasyMock.expectLastCall().once();

		track3OutputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockTrackStore.openOutputStream(trackFile1.getFileName().toString()))
				.andReturn(track1OutputStream).once();
		EasyMock.expect(mockTrackStore.openOutputStream(trackFile2.getFileName().toString()))
				.andReturn(track2OutputStream).once();
		EasyMock.expect(mockTrackStore.openOutputStream(trackFile3.getFileName().toString()))
				.andReturn(track3OutputStream).once();

		Utils.writeFromInputStreamToOutputStream(track1InputStream, track1OutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track2InputStream, track2OutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track3InputStream, track3OutputStream);
		EasyMock.expectLastCall().once();

		Capture<WriteableEventProperties> preSuccessEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT),
				EasyMock.capture(preSuccessEventPropertiesCapture))).andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT_RESULT_PROCEED))).andReturn(true);

		Capture<WriteableEventProperties> successEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.SUCCESS_EVENT),
				EasyMock.capture(successEventPropertiesCapture))).andReturn(null).once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals(mockPackImpl, importer.importMusicPackProject(importLocation));

		WriteableEventProperties packPropertiesEventProps = readingFileEventPropertiesCapture.getValues().get(0);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties trackCountDeterminedEventProps = trackCountDeterminedEventPropertiesCapture.getValue();
		Assert.assertEquals(3l, (long) trackCountDeterminedEventProps
				.getProperty(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_TRACK_COUNT));
		Assert.assertEquals(importLocation,
				trackCountDeterminedEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		for (int i = 0; i < 3; i++) {
			WriteableEventProperties copyingTrackEventProps = copyingTrackEventPropertiesCapture.getValues().get(i);
			Assert.assertEquals("track" + (i + 1) + ".ogg",
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COPYING_TRACK_EVENT_TRACK_NAME));
			Assert.assertEquals(importLocation,
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));
		}

		WriteableEventProperties preSuccessEventProps = preSuccessEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				preSuccessEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties successEventProps = successEventPropertiesCapture.getValue();
		Assert.assertEquals("pack", successEventProps.getProperty(MusicPackProjectImporter.SUCCESS_EVENT_PROJECT_NAME));
		Assert.assertEquals(importLocation,
				successEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties fileNotFoundEventProps = fileNotFoundEventPropertiesCapture.getValue();
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				fileNotFoundEventProps.getProperty(MusicPackProjectImporter.FILE_NOT_FOUND_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				fileNotFoundEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportPackNoNameKey() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).times(1);

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		byte[] readContent = new byte[] { 1, 2, 3 };
		List<Track> readTracksList = new ArrayList<>();
		MusicPack musicPack = this.createMock(MusicPack.class);
		NonNullSet<Track> packTracksSet = this.createMock(NonNullSet.class);

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andReturn(readTracksList).once();

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(musicPack).once();
		EasyMock.expect(musicPack.getTracks()).andReturn(packTracksSet);
		EasyMock.expect(packTracksSet.addAll(readTracksList)).andReturn(true).once();

		Path mockTracksDir = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.TRACKS_DIR)).andReturn(mockTracksDir).once();
		EasyMock.expect(mockFileManager.exists(mockTracksDir)).andReturn(true).once();

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockPackImpl)).andReturn(mockTrackStore).once();

		Path trackFile1 = Paths.get("track1.ogg");
		Path trackFile2 = Paths.get("track2.ogg");
		Path trackFile3 = Paths.get("track3.ogg");

		Collection<Path> trackFiles = new ArrayList<>();

		trackFiles.add(trackFile1);
		trackFiles.add(trackFile2);
		trackFiles.add(trackFile3);

		EasyMock.expect(mockFileManager.getPathsInDirectory(mockTracksDir)).andReturn(trackFiles.stream()).once();

		Capture<WriteableEventProperties> trackCountDeterminedEventPropertiesCapture = Capture
				.newInstance(CaptureType.ALL);
		EasyMock.expect(
				mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT),
						EasyMock.capture(trackCountDeterminedEventPropertiesCapture)))
				.andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_RESULT_PROCEED))).andReturn(true)
				.once();

		Capture<WriteableEventProperties> copyingTrackEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT),
				EasyMock.capture(copyingTrackEventPropertiesCapture))).andReturn(null).times(3);

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT_RESULT_PROCEED))).andReturn(true).times(3);

		InputStream track1InputStream = this.createMock(InputStream.class);
		InputStream track2InputStream = this.createMock(InputStream.class);
		InputStream track3InputStream = this.createMock(InputStream.class);

		track1InputStream.close();
		EasyMock.expectLastCall().once();

		track2InputStream.close();
		EasyMock.expectLastCall().once();

		track3InputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(trackFile1)).andReturn(track1InputStream).once();
		EasyMock.expect(mockFileManager.newInputStream(trackFile2)).andReturn(track2InputStream).once();
		EasyMock.expect(mockFileManager.newInputStream(trackFile3)).andReturn(track3InputStream).once();

		OutputStream track1OutputStream = this.createMock(OutputStream.class);
		OutputStream track2OutputStream = this.createMock(OutputStream.class);
		OutputStream track3OutputStream = this.createMock(OutputStream.class);

		track1OutputStream.close();
		EasyMock.expectLastCall().once();

		track2OutputStream.close();
		EasyMock.expectLastCall().once();

		track3OutputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockTrackStore.openOutputStream(trackFile1.getFileName().toString()))
				.andReturn(track1OutputStream).once();
		EasyMock.expect(mockTrackStore.openOutputStream(trackFile2.getFileName().toString()))
				.andReturn(track2OutputStream).once();
		EasyMock.expect(mockTrackStore.openOutputStream(trackFile3.getFileName().toString()))
				.andReturn(track3OutputStream).once();

		Utils.writeFromInputStreamToOutputStream(track1InputStream, track1OutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track2InputStream, track2OutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track3InputStream, track3OutputStream);
		EasyMock.expectLastCall().once();

		Capture<WriteableEventProperties> preSuccessEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT),
				EasyMock.capture(preSuccessEventPropertiesCapture))).andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT_RESULT_PROCEED))).andReturn(true);

		Capture<WriteableEventProperties> successEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.SUCCESS_EVENT),
				EasyMock.capture(successEventPropertiesCapture))).andReturn(null).once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals(mockPackImpl, importer.importMusicPackProject(importLocation));

		WriteableEventProperties packPropertiesEventProps = readingFileEventPropertiesCapture.getValues().get(0);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties trackCountDeterminedEventProps = trackCountDeterminedEventPropertiesCapture.getValue();
		Assert.assertEquals(3l, (long) trackCountDeterminedEventProps
				.getProperty(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_TRACK_COUNT));
		Assert.assertEquals(importLocation,
				trackCountDeterminedEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		for (int i = 0; i < 3; i++) {
			WriteableEventProperties copyingTrackEventProps = copyingTrackEventPropertiesCapture.getValues().get(i);
			Assert.assertEquals("track" + (i + 1) + ".ogg",
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COPYING_TRACK_EVENT_TRACK_NAME));
			Assert.assertEquals(importLocation,
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));
		}

		WriteableEventProperties preSuccessEventProps = preSuccessEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				preSuccessEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties successEventProps = successEventPropertiesCapture.getValue();
		Assert.assertEquals("pack", successEventProps.getProperty(MusicPackProjectImporter.SUCCESS_EVENT_PROJECT_NAME));
		Assert.assertEquals(importLocation,
				successEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	public void testImportPackNoMusicJSONFile() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn("pack2").times(2);

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack2")).andReturn("pack2").once();
		mockPackImpl.setName("pack2");
		EasyMock.expectLastCall().once();
		packName = "pack2";

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).once();
		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(false).once();

		Capture<WriteableEventProperties> fileNotFoundEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.FILE_NOT_FOUND_EVENT),
				EasyMock.capture(fileNotFoundEventPropertiesCapture))).andReturn(null).times(1);

		Collection<MusicPackProject> registeredProjects = Arrays.asList();
		EasyMock.expect(mockMusicPackProjectManager.getRegisteredMusicPackProjects()).andReturn(registeredProjects)
				.once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertNull(importer.importMusicPackProject(importLocation));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties fileNotFoundEventProps = fileNotFoundEventPropertiesCapture.getValue();
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				fileNotFoundEventProps.getProperty(MusicPackProjectImporter.FILE_NOT_FOUND_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				fileNotFoundEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	public void testImportPackInvalidMusicJSONFile() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn("pack2").times(2);

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack2")).andReturn("pack2").once();
		mockPackImpl.setName("pack2");
		EasyMock.expectLastCall().once();
		packName = "pack2";

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).once();
		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		MusicPack pack = this.createMock(MusicPack.class);
		NonNullSet<Track> tracks = CollectionUtils.createNonNullHashSet();

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(pack).once();
		EasyMock.expect(pack.getTracks()).andReturn(tracks).once();

		byte[] readContent = new byte[] { 1, 2, 3 };

		JsonSyntaxException exception = new JsonSyntaxException("Invalid music.json");

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andThrow(exception).once();

		Capture<WriteableEventProperties> invalidMusicJsonEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.INVALID_MUSIC_JSON_EVENT),
				EasyMock.capture(invalidMusicJsonEventPropertiesCapture))).andReturn(null).times(1);

		Collection<MusicPackProject> registeredProjects = Arrays.asList(mockPackImpl);
		EasyMock.expect(mockMusicPackProjectManager.getRegisteredMusicPackProjects()).andReturn(registeredProjects)
				.once();
		mockMusicPackProjectManager.deleteMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertNull(importer.importMusicPackProject(importLocation));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties invalidMusicJsonEventProps = invalidMusicJsonEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				invalidMusicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportPackNoTracksDirProceed() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn("pack2").times(2);

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack2")).andReturn("pack2").once();
		mockPackImpl.setName("pack2");
		EasyMock.expectLastCall().once();
		packName = "pack2";

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).once();
		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		byte[] readContent = new byte[] { 1, 2, 3 };
		List<Track> readTracksList = new ArrayList<>();
		MusicPack musicPack = this.createMock(MusicPack.class);
		NonNullSet<Track> packTracksSet = this.createMock(NonNullSet.class);

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andReturn(readTracksList).once();

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(musicPack).once();
		EasyMock.expect(musicPack.getTracks()).andReturn(packTracksSet);
		EasyMock.expect(packTracksSet.addAll(readTracksList)).andReturn(true).once();

		Path mockTracksDir = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.TRACKS_DIR)).andReturn(mockTracksDir).once();
		EasyMock.expect(mockFileManager.exists(mockTracksDir)).andReturn(false).once();

		Capture<WriteableEventProperties> fileNotFoundEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.FILE_NOT_FOUND_EVENT),
				EasyMock.capture(fileNotFoundEventPropertiesCapture))).andReturn(null).times(1);
		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.FILE_NOT_FOUND_EVENT_RESULT_PROCEED))).andReturn(true).once();

		Capture<WriteableEventProperties> preSuccessEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT),
				EasyMock.capture(preSuccessEventPropertiesCapture))).andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT_RESULT_PROCEED))).andReturn(true);

		Capture<WriteableEventProperties> successEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.SUCCESS_EVENT),
				EasyMock.capture(successEventPropertiesCapture))).andReturn(null).once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals(mockPackImpl, importer.importMusicPackProject(importLocation));

		WriteableEventProperties packPropertiesEventProps = readingFileEventPropertiesCapture.getValues().get(0);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties preSuccessEventProps = preSuccessEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				preSuccessEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties successEventProps = successEventPropertiesCapture.getValue();
		Assert.assertEquals("pack2",
				successEventProps.getProperty(MusicPackProjectImporter.SUCCESS_EVENT_PROJECT_NAME));
		Assert.assertEquals(importLocation,
				successEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties fileNotFoundEventProps = fileNotFoundEventPropertiesCapture.getValue();
		Assert.assertEquals(MusicPackProjectExporter.TRACKS_DIR,
				fileNotFoundEventProps.getProperty(MusicPackProjectImporter.FILE_NOT_FOUND_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				fileNotFoundEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportPackNoTracksDirCancel() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn("pack2").times(2);

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack2")).andReturn("pack2").once();
		mockPackImpl.setName("pack2");
		EasyMock.expectLastCall().once();
		packName = "pack2";

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).once();
		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		byte[] readContent = new byte[] { 1, 2, 3 };
		List<Track> readTracksList = new ArrayList<>();
		MusicPack musicPack = this.createMock(MusicPack.class);
		NonNullSet<Track> packTracksSet = this.createMock(NonNullSet.class);

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andReturn(readTracksList).once();

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(musicPack).once();
		EasyMock.expect(musicPack.getTracks()).andReturn(packTracksSet);
		EasyMock.expect(packTracksSet.addAll(readTracksList)).andReturn(true).once();

		Path mockTracksDir = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.TRACKS_DIR)).andReturn(mockTracksDir).once();
		EasyMock.expect(mockFileManager.exists(mockTracksDir)).andReturn(false).once();

		Capture<WriteableEventProperties> fileNotFoundEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.FILE_NOT_FOUND_EVENT),
				EasyMock.capture(fileNotFoundEventPropertiesCapture))).andReturn(null).times(1);
		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.FILE_NOT_FOUND_EVENT_RESULT_PROCEED))).andReturn(false).once();

		Capture<WriteableEventProperties> cancelEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.CANCEL_EVENT),
				EasyMock.capture(cancelEventPropertiesCapture))).andReturn(null).times(1);

		Collection<MusicPackProject> registeredProjects = Arrays.asList(mockPackImpl);
		EasyMock.expect(mockMusicPackProjectManager.getRegisteredMusicPackProjects()).andReturn(registeredProjects)
				.once();
		mockMusicPackProjectManager.deleteMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals(null, importer.importMusicPackProject(importLocation));

		WriteableEventProperties packPropertiesEventProps = readingFileEventPropertiesCapture.getValues().get(0);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties fileNotFoundEventProps = fileNotFoundEventPropertiesCapture.getValue();
		Assert.assertEquals(MusicPackProjectExporter.TRACKS_DIR,
				fileNotFoundEventProps.getProperty(MusicPackProjectImporter.FILE_NOT_FOUND_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				fileNotFoundEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties cancelEventProps = cancelEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				cancelEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@SuppressWarnings(value = "unchecked")
	public void testImportPackNonOggFilesProceed() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn("pack2").times(2);

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack2")).andReturn("pack2").once();
		mockPackImpl.setName("pack2");
		EasyMock.expectLastCall().once();
		packName = "pack2";

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).once();
		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		byte[] readContent = new byte[] { 1, 2, 3 };
		List<Track> readTracksList = new ArrayList<>();
		MusicPack musicPack = this.createMock(MusicPack.class);
		NonNullSet<Track> packTracksSet = this.createMock(NonNullSet.class);

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andReturn(readTracksList).once();

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(musicPack).once();
		EasyMock.expect(musicPack.getTracks()).andReturn(packTracksSet);
		EasyMock.expect(packTracksSet.addAll(readTracksList)).andReturn(true).once();

		Path mockTracksDir = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.TRACKS_DIR)).andReturn(mockTracksDir).once();
		EasyMock.expect(mockFileManager.exists(mockTracksDir)).andReturn(true).once();

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockPackImpl)).andReturn(mockTrackStore).once();

		Path trackFile1 = Paths.get("track1.ogg");
		Path trackFile2 = Paths.get("track2.avi");
		Path trackFile3 = Paths.get("track3.ogg");

		Collection<Path> trackFiles = new ArrayList<>();

		trackFiles.add(trackFile1);
		trackFiles.add(trackFile2);
		trackFiles.add(trackFile3);

		EasyMock.expect(mockFileManager.getPathsInDirectory(mockTracksDir)).andReturn(trackFiles.stream()).once();

		Capture<WriteableEventProperties> nonOggFiledEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(
				mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.NON_OGG_TRACK_FILES_FOUND_EVENT),
						EasyMock.capture(nonOggFiledEventPropertiesCapture)))
				.andReturn(null).once();
		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.NON_OGG_TRACK_FILES_FOUND_EVENT_ENTRIES_RESULT_PROCEED)))
				.andReturn(true).once();

		Capture<WriteableEventProperties> trackCountDeterminedEventPropertiesCapture = Capture
				.newInstance(CaptureType.ALL);
		EasyMock.expect(
				mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT),
						EasyMock.capture(trackCountDeterminedEventPropertiesCapture)))
				.andReturn(null).once();
		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_RESULT_PROCEED))).andReturn(true)
				.once();

		Capture<WriteableEventProperties> copyingTrackEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT),
				EasyMock.capture(copyingTrackEventPropertiesCapture))).andReturn(null).times(2);

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT_RESULT_PROCEED))).andReturn(true).times(2);

		InputStream track1InputStream = this.createMock(InputStream.class);
		InputStream track3InputStream = this.createMock(InputStream.class);

		track1InputStream.close();
		EasyMock.expectLastCall().once();

		track3InputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(trackFile1)).andReturn(track1InputStream).once();
		EasyMock.expect(mockFileManager.newInputStream(trackFile3)).andReturn(track3InputStream).once();

		OutputStream track1OutputStream = this.createMock(OutputStream.class);
		OutputStream track3OutputStream = this.createMock(OutputStream.class);

		track1OutputStream.close();
		EasyMock.expectLastCall().once();

		track3OutputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockTrackStore.openOutputStream(trackFile1.getFileName().toString()))
				.andReturn(track1OutputStream).once();
		EasyMock.expect(mockTrackStore.openOutputStream(trackFile3.getFileName().toString()))
				.andReturn(track3OutputStream).once();

		Utils.writeFromInputStreamToOutputStream(track1InputStream, track1OutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track3InputStream, track3OutputStream);
		EasyMock.expectLastCall().once();

		Capture<WriteableEventProperties> preSuccessEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT),
				EasyMock.capture(preSuccessEventPropertiesCapture))).andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT_RESULT_PROCEED))).andReturn(true);

		Capture<WriteableEventProperties> successEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.SUCCESS_EVENT),
				EasyMock.capture(successEventPropertiesCapture))).andReturn(null).once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertEquals(mockPackImpl, importer.importMusicPackProject(importLocation));

		WriteableEventProperties packPropertiesEventProps = readingFileEventPropertiesCapture.getValues().get(0);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties nonOggTracksEventProps = nonOggFiledEventPropertiesCapture.getValue();
		Assert.assertEquals(Arrays.asList("track2.avi"),
				nonOggTracksEventProps.getProperty(MusicPackProjectImporter.NON_OGG_TRACK_FILES_FOUND_EVENT_FILENAMES));
		Assert.assertEquals(importLocation,
				nonOggTracksEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties trackCountDeterminedEventProps = trackCountDeterminedEventPropertiesCapture.getValue();
		Assert.assertEquals(2l, (long) trackCountDeterminedEventProps
				.getProperty(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_TRACK_COUNT));
		Assert.assertEquals(importLocation,
				trackCountDeterminedEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		for (int i = 0; i < 2; i++) {
			WriteableEventProperties copyingTrackEventProps = copyingTrackEventPropertiesCapture.getValues().get(i);
			Assert.assertEquals("track" + (i == 0 ? 1 : 3) + ".ogg",
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COPYING_TRACK_EVENT_TRACK_NAME));
			Assert.assertEquals(importLocation,
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));
		}

		WriteableEventProperties preSuccessEventProps = preSuccessEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				preSuccessEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties successEventProps = successEventPropertiesCapture.getValue();
		Assert.assertEquals("pack2",
				successEventProps.getProperty(MusicPackProjectImporter.SUCCESS_EVENT_PROJECT_NAME));
		Assert.assertEquals(importLocation,
				successEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportPackNonOggFilesCancel() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn("pack2").times(2);

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack2")).andReturn("pack2").once();
		mockPackImpl.setName("pack2");
		EasyMock.expectLastCall().once();
		packName = "pack2";

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).once();
		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		byte[] readContent = new byte[] { 1, 2, 3 };
		List<Track> readTracksList = new ArrayList<>();
		MusicPack musicPack = this.createMock(MusicPack.class);
		NonNullSet<Track> packTracksSet = this.createMock(NonNullSet.class);

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andReturn(readTracksList).once();

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(musicPack).once();
		EasyMock.expect(musicPack.getTracks()).andReturn(packTracksSet);
		EasyMock.expect(packTracksSet.addAll(readTracksList)).andReturn(true).once();

		Path mockTracksDir = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.TRACKS_DIR)).andReturn(mockTracksDir).once();
		EasyMock.expect(mockFileManager.exists(mockTracksDir)).andReturn(true).once();

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockPackImpl)).andReturn(mockTrackStore).once();

		Path trackFile1 = Paths.get("track1.ogg");
		Path trackFile2 = Paths.get("track2.avi");
		Path trackFile3 = Paths.get("track3.ogg");

		Collection<Path> trackFiles = new ArrayList<>();

		trackFiles.add(trackFile1);
		trackFiles.add(trackFile2);
		trackFiles.add(trackFile3);

		EasyMock.expect(mockFileManager.getPathsInDirectory(mockTracksDir)).andReturn(trackFiles.stream()).once();

		Capture<WriteableEventProperties> nonOggFiledEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(
				mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.NON_OGG_TRACK_FILES_FOUND_EVENT),
						EasyMock.capture(nonOggFiledEventPropertiesCapture)))
				.andReturn(null).once();
		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.NON_OGG_TRACK_FILES_FOUND_EVENT_ENTRIES_RESULT_PROCEED)))
				.andReturn(false).once();

		Capture<WriteableEventProperties> cancelEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.CANCEL_EVENT),
				EasyMock.capture(cancelEventPropertiesCapture))).andReturn(null).times(1);

		Collection<MusicPackProject> registeredProjects = Arrays.asList(mockPackImpl);
		EasyMock.expect(mockMusicPackProjectManager.getRegisteredMusicPackProjects()).andReturn(registeredProjects)
				.once();
		mockMusicPackProjectManager.deleteMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertNull(importer.importMusicPackProject(importLocation));

		WriteableEventProperties packPropertiesEventProps = readingFileEventPropertiesCapture.getValues().get(0);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties nonOggTracksEventProps = nonOggFiledEventPropertiesCapture.getValue();
		Assert.assertEquals(Arrays.asList("track2.avi"),
				nonOggTracksEventProps.getProperty(MusicPackProjectImporter.NON_OGG_TRACK_FILES_FOUND_EVENT_FILENAMES));
		Assert.assertEquals(importLocation,
				nonOggTracksEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties cancelEventProps = cancelEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				cancelEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportPackTracksCountDeterminedCancel() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn("pack2").times(2);

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack2")).andReturn("pack2").once();
		mockPackImpl.setName("pack2");
		EasyMock.expectLastCall().once();
		packName = "pack2";

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).once();
		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		byte[] readContent = new byte[] { 1, 2, 3 };
		List<Track> readTracksList = new ArrayList<>();
		MusicPack musicPack = this.createMock(MusicPack.class);
		NonNullSet<Track> packTracksSet = this.createMock(NonNullSet.class);

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andReturn(readTracksList).once();

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(musicPack).once();
		EasyMock.expect(musicPack.getTracks()).andReturn(packTracksSet);
		EasyMock.expect(packTracksSet.addAll(readTracksList)).andReturn(true).once();

		Path mockTracksDir = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.TRACKS_DIR)).andReturn(mockTracksDir).once();
		EasyMock.expect(mockFileManager.exists(mockTracksDir)).andReturn(true).once();

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockPackImpl)).andReturn(mockTrackStore).once();

		Path trackFile1 = Paths.get("track1.ogg");
		Path trackFile2 = Paths.get("track2.ogg");
		Path trackFile3 = Paths.get("track3.ogg");

		Collection<Path> trackFiles = new ArrayList<>();

		trackFiles.add(trackFile1);
		trackFiles.add(trackFile2);
		trackFiles.add(trackFile3);

		EasyMock.expect(mockFileManager.getPathsInDirectory(mockTracksDir)).andReturn(trackFiles.stream()).once();

		Capture<WriteableEventProperties> trackCountDeterminedEventPropertiesCapture = Capture
				.newInstance(CaptureType.ALL);
		EasyMock.expect(
				mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT),
						EasyMock.capture(trackCountDeterminedEventPropertiesCapture)))
				.andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_RESULT_PROCEED))).andReturn(false)
				.once();

		Capture<WriteableEventProperties> cancelEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.CANCEL_EVENT),
				EasyMock.capture(cancelEventPropertiesCapture))).andReturn(null).times(1);

		Collection<MusicPackProject> registeredProjects = Arrays.asList(mockPackImpl);
		EasyMock.expect(mockMusicPackProjectManager.getRegisteredMusicPackProjects()).andReturn(registeredProjects)
				.once();
		mockMusicPackProjectManager.deleteMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertNull(importer.importMusicPackProject(importLocation));

		WriteableEventProperties packPropertiesEventProps = readingFileEventPropertiesCapture.getValues().get(0);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties trackCountDeterminedEventProps = trackCountDeterminedEventPropertiesCapture.getValue();
		Assert.assertEquals(3l, (long) trackCountDeterminedEventProps
				.getProperty(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_TRACK_COUNT));
		Assert.assertEquals(importLocation,
				trackCountDeterminedEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties cancelEventProps = cancelEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				cancelEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportPackCopyingTrackCancel() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn("pack2").times(2);

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack2")).andReturn("pack2").once();
		mockPackImpl.setName("pack2");
		EasyMock.expectLastCall().once();
		packName = "pack2";

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).once();
		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		byte[] readContent = new byte[] { 1, 2, 3 };
		List<Track> readTracksList = new ArrayList<>();
		MusicPack musicPack = this.createMock(MusicPack.class);
		NonNullSet<Track> packTracksSet = this.createMock(NonNullSet.class);

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andReturn(readTracksList).once();

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(musicPack).once();
		EasyMock.expect(musicPack.getTracks()).andReturn(packTracksSet);
		EasyMock.expect(packTracksSet.addAll(readTracksList)).andReturn(true).once();

		Path mockTracksDir = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.TRACKS_DIR)).andReturn(mockTracksDir).once();
		EasyMock.expect(mockFileManager.exists(mockTracksDir)).andReturn(true).once();

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockPackImpl)).andReturn(mockTrackStore).once();

		Path trackFile1 = Paths.get("track1.ogg");
		Path trackFile2 = Paths.get("track2.ogg");
		Path trackFile3 = Paths.get("track3.ogg");

		Collection<Path> trackFiles = new ArrayList<>();

		trackFiles.add(trackFile1);
		trackFiles.add(trackFile2);
		trackFiles.add(trackFile3);

		EasyMock.expect(mockFileManager.getPathsInDirectory(mockTracksDir)).andReturn(trackFiles.stream()).once();

		Capture<WriteableEventProperties> trackCountDeterminedEventPropertiesCapture = Capture
				.newInstance(CaptureType.ALL);
		EasyMock.expect(
				mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT),
						EasyMock.capture(trackCountDeterminedEventPropertiesCapture)))
				.andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_RESULT_PROCEED))).andReturn(true)
				.once();

		Capture<WriteableEventProperties> copyingTrackEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT),
				EasyMock.capture(copyingTrackEventPropertiesCapture))).andReturn(null).times(3);

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT_RESULT_PROCEED))).andReturn(true).times(2)
				.andReturn(false).once();

		InputStream track1InputStream = this.createMock(InputStream.class);
		InputStream track2InputStream = this.createMock(InputStream.class);

		track1InputStream.close();
		EasyMock.expectLastCall().once();

		track2InputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(trackFile1)).andReturn(track1InputStream).once();
		EasyMock.expect(mockFileManager.newInputStream(trackFile2)).andReturn(track2InputStream).once();

		OutputStream track1OutputStream = this.createMock(OutputStream.class);
		OutputStream track2OutputStream = this.createMock(OutputStream.class);

		track1OutputStream.close();
		EasyMock.expectLastCall().once();

		track2OutputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockTrackStore.openOutputStream(trackFile1.getFileName().toString()))
				.andReturn(track1OutputStream).once();
		EasyMock.expect(mockTrackStore.openOutputStream(trackFile2.getFileName().toString()))
				.andReturn(track2OutputStream).once();

		Utils.writeFromInputStreamToOutputStream(track1InputStream, track1OutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track2InputStream, track2OutputStream);
		EasyMock.expectLastCall().once();

		Capture<WriteableEventProperties> cancelEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.CANCEL_EVENT),
				EasyMock.capture(cancelEventPropertiesCapture))).andReturn(null).times(1);

		Collection<MusicPackProject> registeredProjects = Arrays.asList(mockPackImpl);
		EasyMock.expect(mockMusicPackProjectManager.getRegisteredMusicPackProjects()).andReturn(registeredProjects)
				.once();
		mockMusicPackProjectManager.deleteMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertNull(importer.importMusicPackProject(importLocation));

		WriteableEventProperties packPropertiesEventProps = readingFileEventPropertiesCapture.getValues().get(0);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties trackCountDeterminedEventProps = trackCountDeterminedEventPropertiesCapture.getValue();
		Assert.assertEquals(3l, (long) trackCountDeterminedEventProps
				.getProperty(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_TRACK_COUNT));
		Assert.assertEquals(importLocation,
				trackCountDeterminedEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		for (int i = 0; i < 2; i++) {
			WriteableEventProperties copyingTrackEventProps = copyingTrackEventPropertiesCapture.getValues().get(i);
			Assert.assertEquals("track" + (i + 1) + ".ogg",
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COPYING_TRACK_EVENT_TRACK_NAME));
			Assert.assertEquals(importLocation,
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));
		}

		WriteableEventProperties cancelEventProps = cancelEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				cancelEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportPackPreSuccessCancel() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn("pack2").times(2);

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack2")).andReturn("pack2").once();
		mockPackImpl.setName("pack2");
		EasyMock.expectLastCall().once();
		packName = "pack2";

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).once();
		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		byte[] readContent = new byte[] { 1, 2, 3 };
		List<Track> readTracksList = new ArrayList<>();
		MusicPack musicPack = this.createMock(MusicPack.class);
		NonNullSet<Track> packTracksSet = this.createMock(NonNullSet.class);

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andReturn(readTracksList).once();

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(musicPack).once();
		EasyMock.expect(musicPack.getTracks()).andReturn(packTracksSet);
		EasyMock.expect(packTracksSet.addAll(readTracksList)).andReturn(true).once();

		Path mockTracksDir = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.TRACKS_DIR)).andReturn(mockTracksDir).once();
		EasyMock.expect(mockFileManager.exists(mockTracksDir)).andReturn(true).once();

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockPackImpl)).andReturn(mockTrackStore).once();

		Path trackFile1 = Paths.get("track1.ogg");
		Path trackFile2 = Paths.get("track2.ogg");
		Path trackFile3 = Paths.get("track3.ogg");

		Collection<Path> trackFiles = new ArrayList<>();

		trackFiles.add(trackFile1);
		trackFiles.add(trackFile2);
		trackFiles.add(trackFile3);

		EasyMock.expect(mockFileManager.getPathsInDirectory(mockTracksDir)).andReturn(trackFiles.stream()).once();

		Capture<WriteableEventProperties> trackCountDeterminedEventPropertiesCapture = Capture
				.newInstance(CaptureType.ALL);
		EasyMock.expect(
				mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT),
						EasyMock.capture(trackCountDeterminedEventPropertiesCapture)))
				.andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_RESULT_PROCEED))).andReturn(true)
				.once();

		Capture<WriteableEventProperties> copyingTrackEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT),
				EasyMock.capture(copyingTrackEventPropertiesCapture))).andReturn(null).times(3);

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.COPYING_TRACK_EVENT_RESULT_PROCEED))).andReturn(true).times(3);

		InputStream track1InputStream = this.createMock(InputStream.class);
		InputStream track2InputStream = this.createMock(InputStream.class);
		InputStream track3InputStream = this.createMock(InputStream.class);

		track1InputStream.close();
		EasyMock.expectLastCall().once();

		track2InputStream.close();
		EasyMock.expectLastCall().once();

		track3InputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(trackFile1)).andReturn(track1InputStream).once();
		EasyMock.expect(mockFileManager.newInputStream(trackFile2)).andReturn(track2InputStream).once();
		EasyMock.expect(mockFileManager.newInputStream(trackFile3)).andReturn(track3InputStream).once();

		OutputStream track1OutputStream = this.createMock(OutputStream.class);
		OutputStream track2OutputStream = this.createMock(OutputStream.class);
		OutputStream track3OutputStream = this.createMock(OutputStream.class);

		track1OutputStream.close();
		EasyMock.expectLastCall().once();

		track2OutputStream.close();
		EasyMock.expectLastCall().once();

		track3OutputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockTrackStore.openOutputStream(trackFile1.getFileName().toString()))
				.andReturn(track1OutputStream).once();
		EasyMock.expect(mockTrackStore.openOutputStream(trackFile2.getFileName().toString()))
				.andReturn(track2OutputStream).once();
		EasyMock.expect(mockTrackStore.openOutputStream(trackFile3.getFileName().toString()))
				.andReturn(track3OutputStream).once();

		Utils.writeFromInputStreamToOutputStream(track1InputStream, track1OutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track2InputStream, track2OutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track3InputStream, track3OutputStream);
		EasyMock.expectLastCall().once();

		Capture<WriteableEventProperties> preSuccessEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT),
				EasyMock.capture(preSuccessEventPropertiesCapture))).andReturn(null).once();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectImporter.PRE_SUCCESS_EVENT_RESULT_PROCEED))).andReturn(false);

		Capture<WriteableEventProperties> cancelEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.CANCEL_EVENT),
				EasyMock.capture(cancelEventPropertiesCapture))).andReturn(null).once();

		Collection<MusicPackProject> registeredProjects = Arrays.asList(mockPackImpl);
		EasyMock.expect(mockMusicPackProjectManager.getRegisteredMusicPackProjects()).andReturn(registeredProjects)
				.once();
		mockMusicPackProjectManager.deleteMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertNull(importer.importMusicPackProject(importLocation));

		WriteableEventProperties packPropertiesEventProps = readingFileEventPropertiesCapture.getValues().get(0);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties trackCountDeterminedEventProps = trackCountDeterminedEventPropertiesCapture.getValue();
		Assert.assertEquals(3l, (long) trackCountDeterminedEventProps
				.getProperty(MusicPackProjectImporter.TRACK_COUNT_DETERMINED_EVENT_TRACK_COUNT));
		Assert.assertEquals(importLocation,
				trackCountDeterminedEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		for (int i = 0; i < 3; i++) {
			WriteableEventProperties copyingTrackEventProps = copyingTrackEventPropertiesCapture.getValues().get(i);
			Assert.assertEquals("track" + (i + 1) + ".ogg",
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COPYING_TRACK_EVENT_TRACK_NAME));
			Assert.assertEquals(importLocation,
					copyingTrackEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));
		}

		WriteableEventProperties preSuccessEventProps = preSuccessEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				preSuccessEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties cancelEventProps = cancelEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				cancelEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	public void testImportPackUnexpectedErrorUnregisteredProject() throws IOException {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		IOException exception = new IOException("IO ERROR");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andThrow(exception).once();

		Capture<WriteableEventProperties> errorEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.UNEXPECTED_ERROR_EVENT),
				EasyMock.capture(errorEventPropertiesCapture))).andReturn(null).once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertNull(importer.importMusicPackProject(importLocation));

		WriteableEventProperties errorEventProperties = errorEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				errorEventProperties.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));
		Assert.assertEquals(exception,
				errorEventProperties.getProperty(MusicPackProjectImporter.UNEXPECTED_ERROR_EVENT_EXCEPTION));

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testImportPackUnexpectedErrorRegisteredProject() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");
		PowerMock.mockStatic(Utils.class);

		Path importLocation = Paths.get("path", "to", "pack.zip");

		EasyMock.expect(mockFileManager.exists(importLocation)).andReturn(true).atLeastOnce();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(FileSystems.newFileSystem(importLocation, null)).andReturn(mockFileSystem).once();

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack")).andReturn("pack").once();
		MusicPackProjectImpl mockPackImpl = this.createMock(MusicPackProjectImpl.class);
		PowerMock.expectNew(MusicPackProjectImpl.class, "pack").andReturn(mockPackImpl).once();
		EasyMock.expect(mockPackImpl.getName()).andStubAnswer(new IAnswer<String>() {

			@Override
			public String answer() throws Throwable {
				return packName;
			}

		});

		Capture<WriteableEventProperties> readingFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.READING_FILE_EVENT),
				EasyMock.capture(readingFileEventPropertiesCapture))).andReturn(null).times(2);

		Path mockPackFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.PACK_FILE)).andReturn(mockPackFile).once();
		EasyMock.expect(mockFileManager.exists(mockPackFile)).andReturn(true).once();

		InputStream mockInputStream = this.createMock(InputStream.class);
		mockInputStream.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileManager.newInputStream(mockPackFile)).andReturn(mockInputStream);

		ExtendedProperties props = this.createMock(ExtendedProperties.class);
		PowerMock.expectNew(ExtendedProperties.class).andReturn(props).once();

		props.load(mockInputStream);
		EasyMock.expectLastCall().once();

		EasyMock.expect(props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn("pack2").times(2);

		EasyMock.expect(mockMusicPackProjectManager.getUnusedMusicPackProjectName("pack2")).andReturn("pack2").once();
		mockPackImpl.setName("pack2");
		EasyMock.expectLastCall().once();
		packName = "pack2";

		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)).andReturn(null).once();
		EasyMock.expect(props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY)).andReturn(null).once();

		PrimitiveProperties mockProperties = this.createMock(PrimitiveProperties.class);
		EasyMock.expect(mockPackImpl.getProperties()).andReturn(mockProperties).once();
		mockProperties.putAll(props);
		EasyMock.expectLastCall().once();

		// Reading file event is captured above

		Path mockMusicJSONFile = this.createMock(Path.class);
		EasyMock.expect(mockFileSystem.getPath(MusicPackProjectExporter.BASE_FILE)).andReturn(mockMusicJSONFile).once();
		EasyMock.expect(mockFileManager.exists(mockMusicJSONFile)).andReturn(true).once();

		EasyMock.expect(mockMusicPackProjectManager.registerMusicPackProject(mockPackImpl)).andReturn(mockPackImpl)
				.once();
		mockMusicPackProjectManager.saveMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		byte[] readContent = new byte[] { 1, 2, 3 };
		MusicPack musicPack = this.createMock(MusicPack.class);
		NonNullSet<Track> packTracksSet = this.createMock(NonNullSet.class);

		EasyMock.expect(mockPackImpl.getMusicPack()).andReturn(musicPack).once();
		EasyMock.expect(musicPack.getTracks()).andReturn(packTracksSet);

		RuntimeException exception = new RuntimeException("Error");

		EasyMock.expect(mockFileManager.read(mockMusicJSONFile)).andReturn(readContent).once();
		EasyMock.expect(mockReader.readJSONFile(readContent)).andThrow(exception).once();

		Capture<WriteableEventProperties> errorEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);
		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectImporter.UNEXPECTED_ERROR_EVENT),
				EasyMock.capture(errorEventPropertiesCapture))).andReturn(null).once();

		Collection<MusicPackProject> registeredProjects = Arrays.asList(mockPackImpl);
		EasyMock.expect(mockMusicPackProjectManager.getRegisteredMusicPackProjects()).andReturn(registeredProjects)
				.once();
		mockMusicPackProjectManager.deleteMusicPackProject(mockPackImpl);
		EasyMock.expectLastCall().once();

		this.replayAll();
		PowerMock.replayAll();

		Assert.assertNull(importer.importMusicPackProject(importLocation));

		WriteableEventProperties packPropertiesEventProps = readingFileEventPropertiesCapture.getValues().get(0);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				packPropertiesEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties musicJsonEventProps = readingFileEventPropertiesCapture.getValues().get(1);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.READING_FILE_EVENT_FILENAME));
		Assert.assertEquals(importLocation,
				musicJsonEventProps.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));

		WriteableEventProperties errorEventProperties = errorEventPropertiesCapture.getValue();
		Assert.assertEquals(importLocation,
				errorEventProperties.getProperty(MusicPackProjectImporter.COMMON_EVENT_LOCATION));
		Assert.assertEquals(exception,
				errorEventProperties.getProperty(MusicPackProjectImporter.UNEXPECTED_ERROR_EVENT_EXCEPTION));

		this.verifyAll();
		PowerMock.verifyAll();
	}
}

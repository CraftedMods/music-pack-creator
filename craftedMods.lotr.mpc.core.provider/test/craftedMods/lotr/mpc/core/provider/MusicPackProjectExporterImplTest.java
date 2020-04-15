package craftedMods.lotr.mpc.core.provider;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.FileSystem;
import java.util.*;

import org.easymock.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.osgi.service.log.FormatterLogger;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.base.EventUtils;
import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.core.api.*;
import craftedMods.lotr.mpc.core.base.DefaultTrack;
import craftedMods.lotr.mpc.persistence.api.*;
import craftedMods.utils.Utils;
import craftedMods.utils.data.*;
import craftedMods.versionChecker.api.SemanticVersion;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MusicPackProjectExporterImpl.class, EventUtils.class, Utils.class })
public class MusicPackProjectExporterImplTest extends EasyMockSupport {

	@TestSubject
	public MusicPackProjectExporterImpl exporter = new MusicPackProjectExporterImpl();

	@Mock(type = MockType.NICE)
	private FormatterLogger mockLogger;

	@Mock(type = MockType.NICE)
	private SemanticVersion mockVersion;

	@Mock
	private EventManager mockEventManager;

	@Mock
	private FileManager mockFileManager;

	@Mock
	private MusicPackJSONFileWriter mockWriter;

	@Mock
	private TrackStoreManager mockTrackStoreManager;

	@Mock
	private MusicPackProjectManager mockMusicPackProjectManager;

	private Path exportLocation;

	private MusicPackProject mockMusicPackProject;
	private MusicPack mockMusicPack;
	private PrimitiveProperties musicPackProjectProperties;
	private NonNullSet<Track> tracksSet;

	private Track track1;
	private Track track2;
	private Track track3;

	private String trackName1;
	private String trackName2;
	private String trackName3;

	private Collection<MusicPackProject> registeredMPPs;

	private Map<String, Object> envs;

	@Before
	public void setup() {
		exportLocation = Paths.get("test", "music", "pack.zip");

		envs = new HashMap<>();
		envs.put("create", "true");
		envs.put("useTempFile", Boolean.TRUE);

		mockMusicPackProject = this.createMock(MusicPackProject.class);
		mockMusicPack = this.createMock(MusicPack.class);
		tracksSet = CollectionUtils.createNonNullLinkedHashSet();
		
		trackName1 = "Tracl1_2";
		trackName2 = "Track2_2";
		trackName3 = "Tracl4_4";

		track1 = new DefaultTrack(trackName1);
		track2 = new DefaultTrack(trackName2);
		track3 = new DefaultTrack(trackName3);

		tracksSet.add(track1);
		tracksSet.add(track2);
		tracksSet.add(track3);

		musicPackProjectProperties = this.createMock(PrimitiveProperties.class);

		EasyMock.expect(mockMusicPackProject.getMusicPack()).andStubReturn(mockMusicPack);
		EasyMock.expect(mockMusicPackProject.getName()).andStubReturn("Names3");
		EasyMock.expect(mockMusicPackProject.getProperties()).andStubReturn(musicPackProjectProperties);

		EasyMock.expect(mockMusicPack.getTracks()).andStubReturn(tracksSet);

		registeredMPPs = new ArrayList<>();

		EasyMock.expect(mockMusicPackProjectManager.getRegisteredMusicPackProjects()).andReturn(registeredMPPs)
				.atLeastOnce();
	}

	@Test(expected = NullPointerException.class)
	public void testExportNullLocation() {
		exporter.exportMusicPackProject(null, this.createMock(MusicPackProject.class));
	}

	@Test(expected = NullPointerException.class)
	public void testExportNullPack() {
		exporter.exportMusicPackProject(Paths.get("path", "toPack.zip"), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testExportUnregisteredPack() {
		this.replayAll();

		exporter.exportMusicPackProject(Paths.get("path", "toPack.zip"), mockMusicPackProject);
	}

	@Test
	public void testExportPack() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");

		registeredMPPs.add(mockMusicPackProject);

		EasyMock.expect(mockFileManager.exists(exportLocation)).andReturn(false).once();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(mockFileSystem.getPath(EasyMock.anyString(), EasyMock.anyString()))
				.andStubAnswer(new IAnswer<Path>() {
					@Override
					public Path answer() throws Throwable {
						return Paths.get(EasyMock.getCurrentArguments()[0].toString(),
								EasyMock.getCurrentArguments()[1].toString());
					}
				});

		EasyMock.expect(FileSystems.newFileSystem(URI.create("jar:" + exportLocation.toUri()), envs))
				.andReturn(mockFileSystem).once();

		Capture<LockableTypedProperties> createFileEventPropertiesCapture = Capture.newInstance(CaptureType.ALL);

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.CREATING_FILE_EVENT),
				EasyMock.capture(createFileEventPropertiesCapture))).andReturn(null).times(2);

		byte[] mockArray = new byte[] {};

		EasyMock.expect(mockWriter.writeJSONFile(tracksSet)).andReturn(mockArray).once();
		mockFileManager.write(Paths.get(".", MusicPackProjectExporter.BASE_FILE), mockArray);
		EasyMock.expectLastCall().once();

		ExtendedProperties mockPackProperties = this.createMock(ExtendedProperties.class);

		PowerMock.expectNew(ExtendedProperties.class).andReturn(mockPackProperties).once();

		mockPackProperties.setString(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY, "Names3");
		EasyMock.expectLastCall().once();
		mockPackProperties.setInteger(MusicPackProjectExporter.PACK_TRACKS_KEY, 3);
		EasyMock.expectLastCall().once();
		mockPackProperties.putAll(musicPackProjectProperties);
		EasyMock.expectLastCall().once();

		ByteArrayOutputStream mockByteArrayOutStream = this.createMock(ByteArrayOutputStream.class);

		PowerMock.expectNew(ByteArrayOutputStream.class).andReturn(mockByteArrayOutStream).once();

		byte[] propsArray = new byte[] {};

		mockPackProperties.store(EasyMock.eq(mockByteArrayOutStream), EasyMock.anyString());
		EasyMock.expectLastCall().once();

		EasyMock.expect(mockByteArrayOutStream.toByteArray()).andReturn(propsArray).once();

		mockFileManager.write(EasyMock.eq(Paths.get(".", MusicPackProjectExporter.PACK_FILE)), EasyMock.eq(propsArray));
		EasyMock.expectLastCall().once();

		mockByteArrayOutStream.close();
		EasyMock.expectLastCall().asStub();

		Path tracksDir = Paths.get(".", MusicPackProjectExporter.TRACKS_DIR);

		EasyMock.expect(mockFileManager.createDir(tracksDir)).andReturn(true).once();

		Capture<LockableTypedProperties> copyTrackEventProperties = Capture.newInstance(CaptureType.ALL);

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectExporter.COPYING_TRACK_EVENT_RESULT_PROCEED))).andReturn(true).times(3);
		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.COPYING_TRACK_EVENT),
				EasyMock.capture(copyTrackEventProperties))).andReturn(Arrays.asList()).times(3);

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockMusicPackProject)).andReturn(mockTrackStore).once();

		InputStream track1MockInputStream = this.createMock(InputStream.class);
		InputStream track2MockInputStream = this.createMock(InputStream.class);
		InputStream track3MockInputStream = this.createMock(InputStream.class);

		EasyMock.expect(mockTrackStore.openInputStream(trackName1)).andReturn(track1MockInputStream);
		EasyMock.expect(mockTrackStore.openInputStream(trackName2)).andReturn(track2MockInputStream);
		EasyMock.expect(mockTrackStore.openInputStream(trackName3)).andReturn(track3MockInputStream);

		track1MockInputStream.close();
		EasyMock.expectLastCall().once();

		track2MockInputStream.close();
		EasyMock.expectLastCall().once();

		track3MockInputStream.close();
		EasyMock.expectLastCall().once();

		OutputStream track1MockOutputStream = this.createMock(OutputStream.class);
		OutputStream track2MockOutputStream = this.createMock(OutputStream.class);
		OutputStream track3MockOutputStream = this.createMock(OutputStream.class);

		EasyMock.expect(mockFileManager.newOutputStream(tracksDir.resolve(trackName1)))
				.andReturn(track1MockOutputStream).once();
		EasyMock.expect(mockFileManager.newOutputStream(tracksDir.resolve(trackName2)))
				.andReturn(track2MockOutputStream).once();
		EasyMock.expect(mockFileManager.newOutputStream(tracksDir.resolve(trackName3)))
				.andReturn(track3MockOutputStream).once();

		track1MockOutputStream.close();
		EasyMock.expectLastCall().once();

		track2MockOutputStream.close();
		EasyMock.expectLastCall().once();

		track3MockOutputStream.close();
		EasyMock.expectLastCall().once();

		PowerMock.mockStatic(Utils.class);

		Utils.writeFromInputStreamToOutputStream(track1MockInputStream, track1MockOutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track2MockInputStream, track2MockOutputStream);
		EasyMock.expectLastCall().once();

		Utils.writeFromInputStreamToOutputStream(track3MockInputStream, track3MockOutputStream);
		EasyMock.expectLastCall().once();

		Capture<LockableTypedProperties> preSuccessEventProperties = Capture.newInstance();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectExporter.PRE_SUCCESS_EVENT_RESULT_PROCEED))).andReturn(true).once();
		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.PRE_SUCCESS_EVENT),
				EasyMock.capture(preSuccessEventProperties))).andReturn(Arrays.asList()).once();

		Capture<LockableTypedProperties> successEventProperties = Capture.newInstance();

		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.SUCCESS_EVENT),
				EasyMock.capture(successEventProperties))).andReturn(Arrays.asList()).once();

		this.replayAll();
		PowerMock.replayAll();

		exporter.exportMusicPackProject(exportLocation, mockMusicPackProject);

		LockableTypedProperties createJSONFileEventProperties = createFileEventPropertiesCapture.getValues().get(0);
		this.checkForStandardProperties(createJSONFileEventProperties);
		Assert.assertEquals(MusicPackProjectExporter.BASE_FILE,
				createJSONFileEventProperties.getProperty(MusicPackProjectExporter.CREATING_FILE_EVENT_FILENAME));

		LockableTypedProperties createPackFileEventProperties = createFileEventPropertiesCapture.getValues().get(1);
		this.checkForStandardProperties(createPackFileEventProperties);
		Assert.assertEquals(MusicPackProjectExporter.PACK_FILE,
				createPackFileEventProperties.getProperty(MusicPackProjectExporter.CREATING_FILE_EVENT_FILENAME));

		for (int i = 0; i < 3; i++) {
			LockableTypedProperties copyTrackEventPropertiesValue = copyTrackEventProperties.getValues().get(i);
			this.checkForStandardProperties(copyTrackEventPropertiesValue);
			Assert.assertEquals(i == 0 ? trackName1 : i == 1 ? trackName2 : trackName3,
					copyTrackEventPropertiesValue.getProperty(MusicPackProjectExporter.COPYING_TRACK_EVENT_TRACK_NAME));
		}

		this.checkForStandardProperties(preSuccessEventProperties.getValue());

		this.checkForStandardProperties(successEventProperties.getValue());

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	public void testExportPackOverrideNotPermitted() throws Exception {
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");

		registeredMPPs.add(mockMusicPackProject);

		EasyMock.expect(mockFileManager.exists(exportLocation)).andReturn(true).once();

		Capture<LockableTypedProperties> overrideEventProperties = Capture.newInstance();
		EasyMock.expect(
				this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.EXPORT_LOCATION_EXISTS_EVENT),
						EasyMock.capture(overrideEventProperties)))
				.andReturn(Arrays.asList()).once();
		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectExporter.EXPORT_LOCATION_EXISTS_EVENT_RESULT_OVERRIDE), EasyMock.eq(false)))
				.andReturn(false).once();

		Capture<LockableTypedProperties> cancelEventProperties = Capture.newInstance();

		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.CANCEL_EVENT),
				EasyMock.capture(cancelEventProperties))).andReturn(Arrays.asList()).once();

		this.replayAll();
		PowerMock.replayAll();

		exporter.exportMusicPackProject(exportLocation, mockMusicPackProject);

		this.checkForStandardProperties(overrideEventProperties.getValue());
		this.checkForStandardProperties(cancelEventProperties.getValue());

		this.verifyAll();
		PowerMock.verifyAll();

	}

	@Test
	public void testExportPackOverridePermitted() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartialNice(EventUtils.class, "proceed");

		registeredMPPs.add(mockMusicPackProject);

		EasyMock.resetToNice(this.mockWriter);

		EasyMock.expect(mockFileManager.exists(exportLocation)).andReturn(true).once();

		Capture<LockableTypedProperties> overrideEventProperties = Capture.newInstance();
		EasyMock.expect(
				this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.EXPORT_LOCATION_EXISTS_EVENT),
						EasyMock.capture(overrideEventProperties)))
				.andReturn(Arrays.asList()).once();
		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectExporter.EXPORT_LOCATION_EXISTS_EVENT_RESULT_OVERRIDE), EasyMock.eq(false)))
				.andReturn(true).once();

		EasyMock.expect(this.mockFileManager.deleteFile(exportLocation)).andReturn(true).once();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().asStub();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(mockFileSystem.getPath(EasyMock.anyString(), EasyMock.anyString()))
				.andStubAnswer(new IAnswer<Path>() {
					@Override
					public Path answer() throws Throwable {
						return Paths.get(EasyMock.getCurrentArguments()[0].toString(),
								EasyMock.getCurrentArguments()[1].toString());
					}
				});

		EasyMock.expect(FileSystems.newFileSystem(URI.create("jar:" + exportLocation.toUri()), envs))
				.andStubReturn(mockFileSystem);

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.CREATING_FILE_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andReturn(null).times(2);

		mockFileManager.write(EasyMock.eq(Paths.get(".", MusicPackProjectExporter.BASE_FILE)),
				EasyMock.anyObject(byte[].class));
		EasyMock.expectLastCall().asStub();

		PowerMock.expectNew(ExtendedProperties.class).andStubReturn(this.createNiceMock(ExtendedProperties.class));

		PowerMock.expectNew(ByteArrayOutputStream.class)
				.andStubReturn(this.createNiceMock(ByteArrayOutputStream.class));

		mockFileManager.write(EasyMock.eq(Paths.get(".", MusicPackProjectExporter.PACK_FILE)),
				EasyMock.anyObject(byte[].class));
		EasyMock.expectLastCall().asStub();

		EasyMock.expect(mockFileManager.createDir(EasyMock.anyObject(Path.class))).andStubReturn(true);

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockMusicPackProject)).andStubReturn(mockTrackStore);

		InputStream track1MockInputStream = this.createMock(InputStream.class);

		EasyMock.expect(mockTrackStore.openInputStream(EasyMock.anyString())).andStubReturn(track1MockInputStream);

		track1MockInputStream.close();
		EasyMock.expectLastCall().asStub();

		OutputStream track1MockOutputStream = this.createMock(OutputStream.class);

		EasyMock.expect(mockFileManager.newOutputStream(EasyMock.anyObject(Path.class)))
				.andStubReturn(track1MockOutputStream);

		track1MockOutputStream.close();
		EasyMock.expectLastCall().asStub();

		PowerMock.mockStatic(Utils.class);

		Utils.writeFromInputStreamToOutputStream(track1MockInputStream, track1MockOutputStream);
		EasyMock.expectLastCall().asStub();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectExporter.COPYING_TRACK_EVENT_RESULT_PROCEED))).andReturn(true).times(3);
		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.COPYING_TRACK_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andReturn(Arrays.asList()).times(3);

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectExporter.PRE_SUCCESS_EVENT_RESULT_PROCEED))).andStubReturn(true);

		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.PRE_SUCCESS_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andReturn(Arrays.asList()).once();

		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.SUCCESS_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andReturn(Arrays.asList()).once();

		this.replayAll();
		PowerMock.replayAll();

		exporter.exportMusicPackProject(exportLocation, mockMusicPackProject);

		this.checkForStandardProperties(overrideEventProperties.getValue());

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	public void testExportPackCopyTrackCancel() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartialNice(EventUtils.class, "proceed");

		registeredMPPs.add(mockMusicPackProject);

		EasyMock.resetToNice(this.mockWriter);

		EasyMock.expect(mockFileManager.exists(exportLocation)).andReturn(false).once();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().asStub();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(mockFileSystem.getPath(EasyMock.anyString(), EasyMock.anyString()))
				.andStubAnswer(new IAnswer<Path>() {
					@Override
					public Path answer() throws Throwable {
						return Paths.get(EasyMock.getCurrentArguments()[0].toString(),
								EasyMock.getCurrentArguments()[1].toString());
					}
				});

		EasyMock.expect(FileSystems.newFileSystem(URI.create("jar:" + exportLocation.toUri()), envs))
				.andStubReturn(mockFileSystem);

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.CREATING_FILE_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andReturn(null).times(2);

		mockFileManager.write(EasyMock.eq(Paths.get(".", MusicPackProjectExporter.BASE_FILE)),
				EasyMock.anyObject(byte[].class));
		EasyMock.expectLastCall().asStub();

		PowerMock.expectNew(ExtendedProperties.class).andStubReturn(this.createNiceMock(ExtendedProperties.class));

		PowerMock.expectNew(ByteArrayOutputStream.class)
				.andStubReturn(this.createNiceMock(ByteArrayOutputStream.class));

		mockFileManager.write(EasyMock.eq(Paths.get(".", MusicPackProjectExporter.PACK_FILE)),
				EasyMock.anyObject(byte[].class));
		EasyMock.expectLastCall().asStub();

		EasyMock.expect(mockFileManager.createDir(EasyMock.anyObject(Path.class))).andStubReturn(true);

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockMusicPackProject)).andStubReturn(mockTrackStore);

		InputStream track1MockInputStream = this.createMock(InputStream.class);

		EasyMock.expect(mockTrackStore.openInputStream(EasyMock.anyString())).andStubReturn(track1MockInputStream);

		track1MockInputStream.close();
		EasyMock.expectLastCall().asStub();

		OutputStream track1MockOutputStream = this.createMock(OutputStream.class);

		EasyMock.expect(mockFileManager.newOutputStream(EasyMock.anyObject(Path.class)))
				.andStubReturn(track1MockOutputStream);

		track1MockOutputStream.close();
		EasyMock.expectLastCall().asStub();

		PowerMock.mockStatic(Utils.class);

		Utils.writeFromInputStreamToOutputStream(track1MockInputStream, track1MockOutputStream);
		EasyMock.expectLastCall().asStub();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectExporter.COPYING_TRACK_EVENT_RESULT_PROCEED))).andReturn(true).times(2)
				.andReturn(false).once();
		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.COPYING_TRACK_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andReturn(Arrays.asList()).atLeastOnce();

		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.CANCEL_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andReturn(Arrays.asList()).once();

		EasyMock.expect(mockFileManager.deleteFile(exportLocation)).andReturn(true).once();

		this.replayAll();
		PowerMock.replayAll();

		exporter.exportMusicPackProject(exportLocation, mockMusicPackProject);

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	public void testExportPackPreSuccessCancel() throws Exception {
		PowerMock.mockStatic(FileSystems.class);
		PowerMock.mockStaticPartialNice(EventUtils.class, "proceed");

		registeredMPPs.add(mockMusicPackProject);

		EasyMock.resetToNice(this.mockWriter);

		EasyMock.expect(mockFileManager.exists(exportLocation)).andReturn(false).once();

		FileSystem mockFileSystem = this.createMock(FileSystem.class);

		mockFileSystem.close();
		EasyMock.expectLastCall().asStub();

		EasyMock.expect(mockFileSystem.getSeparator()).andStubReturn("/");

		EasyMock.expect(mockFileSystem.getPath(EasyMock.anyString(), EasyMock.anyString()))
				.andStubAnswer(new IAnswer<Path>() {
					@Override
					public Path answer() throws Throwable {
						return Paths.get(EasyMock.getCurrentArguments()[0].toString(),
								EasyMock.getCurrentArguments()[1].toString());
					}
				});

		EasyMock.expect(FileSystems.newFileSystem(URI.create("jar:" + exportLocation.toUri()), envs))
				.andStubReturn(mockFileSystem);

		EasyMock.expect(mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.CREATING_FILE_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andReturn(null).times(2);

		mockFileManager.write(EasyMock.eq(Paths.get(".", MusicPackProjectExporter.BASE_FILE)),
				EasyMock.anyObject(byte[].class));
		EasyMock.expectLastCall().asStub();

		PowerMock.expectNew(ExtendedProperties.class).andStubReturn(this.createNiceMock(ExtendedProperties.class));

		PowerMock.expectNew(ByteArrayOutputStream.class)
				.andStubReturn(this.createNiceMock(ByteArrayOutputStream.class));

		mockFileManager.write(EasyMock.eq(Paths.get(".", MusicPackProjectExporter.PACK_FILE)),
				EasyMock.anyObject(byte[].class));
		EasyMock.expectLastCall().asStub();

		EasyMock.expect(mockFileManager.createDir(EasyMock.anyObject(Path.class))).andStubReturn(true);

		TrackStore mockTrackStore = this.createMock(TrackStore.class);

		EasyMock.expect(mockTrackStoreManager.getTrackStore(mockMusicPackProject)).andStubReturn(mockTrackStore);

		InputStream track1MockInputStream = this.createMock(InputStream.class);

		EasyMock.expect(mockTrackStore.openInputStream(EasyMock.anyString())).andStubReturn(track1MockInputStream);

		track1MockInputStream.close();
		EasyMock.expectLastCall().asStub();

		OutputStream track1MockOutputStream = this.createMock(OutputStream.class);

		EasyMock.expect(mockFileManager.newOutputStream(EasyMock.anyObject(Path.class)))
				.andStubReturn(track1MockOutputStream);

		track1MockOutputStream.close();
		EasyMock.expectLastCall().asStub();

		PowerMock.mockStatic(Utils.class);

		Utils.writeFromInputStreamToOutputStream(track1MockInputStream, track1MockOutputStream);
		EasyMock.expectLastCall().asStub();

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectExporter.COPYING_TRACK_EVENT_RESULT_PROCEED))).andReturn(true).times(3);
		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.COPYING_TRACK_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andReturn(Arrays.asList()).times(3);

		EasyMock.expect(EventUtils.proceed(EasyMock.anyObject(),
				EasyMock.eq(MusicPackProjectExporter.PRE_SUCCESS_EVENT_RESULT_PROCEED))).andStubReturn(false);

		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.PRE_SUCCESS_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andReturn(Arrays.asList()).once();

		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.CANCEL_EVENT),
				EasyMock.anyObject(LockableTypedProperties.class))).andReturn(Arrays.asList()).once();

		EasyMock.expect(mockFileManager.deleteFile(exportLocation)).andReturn(true).once();

		this.replayAll();
		PowerMock.replayAll();

		exporter.exportMusicPackProject(exportLocation, mockMusicPackProject);

		this.verifyAll();
		PowerMock.verifyAll();
	}

	@Test
	public void testExportPackException() throws Exception {
		PowerMock.mockStaticPartial(EventUtils.class, "proceed");

		registeredMPPs.add(mockMusicPackProject);

		RuntimeException exception = new RuntimeException("IO Error");

		EasyMock.expect(mockFileManager.exists(exportLocation)).andThrow(exception).once();

		Capture<LockableTypedProperties> exceptionEventProperties = Capture.newInstance();
		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.eq(MusicPackProjectExporter.ERROR_EVENT),
				EasyMock.capture(exceptionEventProperties))).andReturn(Arrays.asList()).once();

		EasyMock.expect(mockFileManager.deleteFile(exportLocation)).andReturn(true).once();

		this.replayAll();
		PowerMock.replayAll();

		exporter.exportMusicPackProject(exportLocation, mockMusicPackProject);

		LockableTypedProperties properties = exceptionEventProperties.getValue();
		Assert.assertEquals(exception, properties.getProperty(MusicPackProjectExporter.ERROR_EVENT_EXCEPTION));
		this.checkForStandardProperties(properties);

		this.verifyAll();
		PowerMock.verifyAll();

	}

	private void checkForStandardProperties(LockableTypedProperties properties) {
		Assert.assertEquals(mockMusicPackProject,
				properties.getProperty(MusicPackProjectExporter.COMMON_EVENT_MUSIC_PACK_PROJECT));
		Assert.assertEquals(exportLocation, properties.getProperty(MusicPackProjectExporter.COMMON_EVENT_LOCATION));
	}

}

package craftedMods.lotr.mpc.core.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.FormatterLogger;
import org.osgi.service.log.LoggerFactory;

import com.google.gson.JsonSyntaxException;

import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.eventManager.base.DefaultWriteableEventProperties;
import craftedMods.eventManager.base.EventUtils;
import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.core.api.MusicPackProjectExporter;
import craftedMods.lotr.mpc.core.api.MusicPackProjectImporter;
import craftedMods.lotr.mpc.core.api.MusicPackProjectManager;
import craftedMods.lotr.mpc.persistence.api.TrackStore;
import craftedMods.lotr.mpc.persistence.api.TrackStoreManager;
import craftedMods.utils.Utils;
import craftedMods.utils.data.ExtendedProperties;
import craftedMods.utils.data.ReadOnlyTypedProperties;
import craftedMods.utils.data.TypedPropertyKey;
import craftedMods.utils.exceptions.InvalidInputException;
import craftedMods.versionChecker.api.SemanticVersion;

@Component
public class MusicPackProjectImporterImpl implements MusicPackProjectImporter {

	@Reference(target = "(application=mpc)")
	private SemanticVersion version;

	@Reference(service = LoggerFactory.class)
	private FormatterLogger logger;

	@Reference
	private EventManager eventManager;

	@Reference
	private FileManager fileManager;

	@Reference
	private MusicPackProjectManager musicPackProjectManager;

	@Reference
	private MusicPackJSONFileReader reader;

	@Reference
	private TrackStoreManager trackStoreManager;

	@Override
	public MusicPackProject importMusicPackProject(Path location) {
		Objects.requireNonNull(location);
		if (!fileManager.exists(location))
			throw new UncheckedIOException(new NoSuchFileException(location.toString()));

		boolean delete = false;
		boolean cancel = false;
		boolean error = false;

		MusicPackProjectImpl project = null;

		try (FileSystem zip = FileSystems.newFileSystem(location, null)) {
			project = new MusicPackProjectImpl(musicPackProjectManager
					.getUnusedMusicPackProjectName(location.getFileName().toString().replaceAll(".zip", "")));

			this.readPackPropertiesFile(location, zip, project);

			error = !readMusicJSON(location, zip, project);

			if (!error) {
				cancel = !copyTracks(location, zip, project);

				if (!cancel) {
					cancel = !EventUtils.proceed(this.dispatchEvent(PRE_SUCCESS_EVENT, location),
							MusicPackProjectImporter.PRE_SUCCESS_EVENT_RESULT_PROCEED);

					if (!cancel) {
						dispatchEvent(SUCCESS_EVENT, location, SUCCESS_EVENT_PROJECT_NAME, project.getName());
						this.logger.info("Successfully imported the Music Pack Project \"%s\" from \"%s\"",
								project.getName(), location);
						return project;
					}
				}

				this.dispatchEvent(CANCEL_EVENT, location);
			}

			delete = true;

		} catch (Exception e) {
			delete = true;
			this.dispatchEvent(UNEXPECTED_ERROR_EVENT, location,
					MusicPackProjectImporter.UNEXPECTED_ERROR_EVENT_EXCEPTION, e);

			this.logger.error("Couldn't import the Music Pack from \"%s\"", location.toString(), e);
		} finally {
			if (delete && project != null) {
				if (musicPackProjectManager.getRegisteredMusicPackProjects().contains(project)) {
					musicPackProjectManager.deleteMusicPackProject(project);
				}
			}
		}
		return null;
	}

	private void readPackPropertiesFile(Path packLocation, FileSystem zip, MusicPackProjectImpl project)
			throws IOException {
		dispatchEvent(READING_FILE_EVENT, packLocation, MusicPackProjectImporter.READING_FILE_EVENT_FILENAME,
				MusicPackProjectExporter.PACK_FILE);

		Path packFile = zip.getPath(MusicPackProjectExporter.PACK_FILE);

		if (fileManager.exists(packFile)) {
			try (InputStream propertiesIn = fileManager.newInputStream(packFile)) {
				ExtendedProperties props = new ExtendedProperties();
				props.load(propertiesIn);

				if (props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY) != null) {
					project.setName(musicPackProjectManager.getUnusedMusicPackProjectName(
							props.getProperty(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY)));
					props.remove(MusicPackProjectExporter.PACK_PROJECT_NAME_KEY);
				}

				// The track count property isn't needed by this importer
				props.remove(MusicPackProjectExporter.PACK_TRACKS_KEY);

				project.getProperties().putAll(props);
			}
		} else {
			this.logger.warn("The Music Pack at \"%s\" doesn't contain the optional file %s", packLocation,
					MusicPackProjectExporter.PACK_FILE);
			this.dispatchEvent(FILE_NOT_FOUND_EVENT, packLocation, FILE_NOT_FOUND_EVENT_FILENAME,
					MusicPackProjectExporter.PACK_FILE);
		}
	}

	private boolean readMusicJSON(Path packLocation, FileSystem zip, MusicPackProjectImpl project)
			throws InvalidInputException, IOException {
		dispatchEvent(READING_FILE_EVENT, packLocation, MusicPackProjectImporter.READING_FILE_EVENT_FILENAME,
				MusicPackProjectExporter.BASE_FILE);

		Path musicJson = zip.getPath(MusicPackProjectExporter.BASE_FILE);

		if (fileManager.exists(musicJson)) {

			this.musicPackProjectManager.registerMusicPackProject(project);
			this.musicPackProjectManager.saveMusicPackProject(project);

			try {
				project.getMusicPack().getTracks().addAll(reader.readJSONFile(fileManager.read(musicJson)));
				return true;

			} catch (JsonSyntaxException e) {
				this.logger.error("The track data at the file %s of the Music Pack at \"%s\" are invalid",
						MusicPackProjectExporter.BASE_FILE, packLocation);
				this.dispatchEvent(INVALID_MUSIC_JSON_EVENT, packLocation);
			}

		} else {
			this.logger.error("The Music Pack at \"%s\" doesn't contain the required file %s", packLocation,
					MusicPackProjectExporter.BASE_FILE);
			this.dispatchEvent(FILE_NOT_FOUND_EVENT, packLocation, FILE_NOT_FOUND_EVENT_FILENAME,
					MusicPackProjectExporter.BASE_FILE);
		}
		return false;
	}

	private boolean copyTracks(Path packLocation, FileSystem zip, MusicPackProjectImpl project) throws IOException {
		Path tracksDir = zip.getPath(MusicPackProjectExporter.TRACKS_DIR);
		if (fileManager.exists(tracksDir)) {
			TrackStore store = trackStoreManager.getTrackStore(project);

			Collection<Path> allTrackFiles = fileManager.getPathsInDirectory(tracksDir).collect(Collectors.toList());
			Collection<String> nonOggFiles = allTrackFiles.stream().map(path -> path.getFileName().toString())
					.filter(filename -> !filename.endsWith(".ogg")).collect(Collectors.toList());
			List<Path> oggFiles = allTrackFiles.stream()
					.filter(path -> path.getFileName().toString().endsWith((".ogg"))).collect(Collectors.toList());

			if (!nonOggFiles.isEmpty()) {
				if (!EventUtils.proceed(
						this.dispatchEvent(NON_OGG_TRACK_FILES_FOUND_EVENT, packLocation,
								NON_OGG_TRACK_FILES_FOUND_EVENT_FILENAMES, nonOggFiles),
						NON_OGG_TRACK_FILES_FOUND_EVENT_ENTRIES_RESULT_PROCEED))
					return false;
			}

			if (!EventUtils.proceed(
					this.dispatchEvent(TRACK_COUNT_DETERMINED_EVENT, packLocation,
							TRACK_COUNT_DETERMINED_EVENT_TRACK_COUNT, oggFiles.size()),
					TRACK_COUNT_DETERMINED_EVENT_RESULT_PROCEED))
				return false;

			for (Path path : oggFiles) {

				if (!EventUtils.proceed(
						this.dispatchEvent(COPYING_TRACK_EVENT, packLocation,
								MusicPackProjectImporter.COPYING_TRACK_EVENT_TRACK_NAME, path.getFileName().toString()),
						MusicPackProjectImporter.COPYING_TRACK_EVENT_RESULT_PROCEED)) {
					return false;
				}

				try (InputStream in = fileManager.newInputStream(path);
						OutputStream out = store.openOutputStream(path.getFileName().toString());) {
					Utils.writeFromInputStreamToOutputStream(in, out);
				}

			}
			return true;
		} else {
			this.logger.warn("The Music Pack at \"%s\" doesn't contain the tracks directory \"%s\"", packLocation,
					MusicPackProjectExporter.TRACKS_DIR);
			if (EventUtils.proceed(
					this.dispatchEvent(FILE_NOT_FOUND_EVENT, packLocation, FILE_NOT_FOUND_EVENT_FILENAME,
							MusicPackProjectExporter.TRACKS_DIR),
					MusicPackProjectImporter.FILE_NOT_FOUND_EVENT_RESULT_PROCEED)) {
				return true;
			}
			return false;
		}
	}

	private <T> Collection<ReadOnlyTypedProperties> dispatchEvent(EventInfo info, Path path) {
		return this.dispatchEvent(info, path, null, null);
	}

	private <T> Collection<ReadOnlyTypedProperties> dispatchEvent(EventInfo info, Path path, TypedPropertyKey<T> key, T value) {
		WriteableEventProperties properties = new DefaultWriteableEventProperties();
		properties.put(MusicPackProjectImporter.COMMON_EVENT_LOCATION, path);
		if (key != null)
			properties.put(key, value);
		return this.eventManager.dispatchEvent(info, properties);
	}

}

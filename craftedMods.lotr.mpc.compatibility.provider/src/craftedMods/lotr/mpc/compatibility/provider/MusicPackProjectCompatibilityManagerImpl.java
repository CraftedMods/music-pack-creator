package craftedMods.lotr.mpc.compatibility.provider;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;

import org.osgi.service.component.annotations.*;
import org.osgi.service.log.*;

import craftedMods.eventManager.api.*;
import craftedMods.eventManager.base.*;
import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.compatibility.api.MusicPackProjectCompatibilityManager;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.persistence.api.*;
import craftedMods.utils.Utils;
import craftedMods.utils.data.*;

@Component
public class MusicPackProjectCompatibilityManagerImpl implements MusicPackProjectCompatibilityManager {

	@Reference
	private EventManager eventManager;

	@Reference
	private SerializedWorkspaceToJSONConverter serializedWorkspaceToJsonConverter;

	@Reference(service=LoggerFactory.class)
	private FormatterLogger logger;

	@Reference
	private FileManager fileManager;

	@Reference
	private TrackStoreManager trackStoreManager;

	@Override
	public void applyPreLoadFixes(Path workspacePath) {
		this.fixSerializedWorkspace(workspacePath);
	}

	private void fixSerializedWorkspace(Path projectDir) {
		Objects.requireNonNull(projectDir);
		if (fileManager.exists(Paths.get(projectDir.toString(), SerializedWorkspaceToJSONConverter.OLD_PROJECT_FILE))) {
			this.logger.warn("Found a Music Pack Project at \"%s\" which contains a serialized project file",
							projectDir.toString());
			
			LockableTypedProperties properties = new DefaultTypedProperties();
			properties.put(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_PATH,
					projectDir);
			if (EventUtils.proceed(this.eventManager.dispatchEvent(
					MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT, properties),
					MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_RESULT_PROCEED))
				try {
					this.serializedWorkspaceToJsonConverter.convertWorkspace(projectDir);
					this.logger.info("Converted the Music Pack Project at \"%s\" to the JSON format", projectDir);
					properties = new DefaultTypedProperties();
					properties.put(
							MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_CONVERTED_EVENT_PATH,
							projectDir);
					this.eventManager.dispatchEvent(
							MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_CONVERTED_EVENT,
							properties);
				} catch (Exception e) {
					this.logger.error("Couldn't convert the Music Pack Project at \"%s\": ", projectDir.toString(), e);
					properties = new DefaultTypedProperties();
					properties.put(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT_PATH,
							projectDir);
					properties.put(
							MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT_EXCEPTION,
							e);
					this.eventManager.dispatchEvent(
							MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT, properties);
				}
			else
				this.logger.debug("The user skipped the conversion of the Music Pack Project - it won't be loaded");
		}

	}

	@Override
	public void applyPostLoadFixes(Path workspacePath, MusicPackProject project, String loadedVersion) {
		Objects.requireNonNull(workspacePath);
		Objects.requireNonNull(project);
		if (serializedWorkspaceToJsonConverter.getOldProjects().containsKey(workspacePath))
			copyTracksToStore(workspacePath, project);
	}
	
	private void copyTracksToStore(Path projectPath, MusicPackProject project) {
		try {
			TrackStore store = trackStoreManager.getTrackStore(project);
			store.refresh(); // Load present tracks
			int copiedTracks = 0;
			int presentTracks = 0;
			int notFoundTracks = 0;
	
			for (craftedMods.lotrTools.musicPackCreator.data.Track oldTrack : serializedWorkspaceToJsonConverter
					.getOldProjects().get(projectPath)) {
				/*
				 * Ignore missing tracks and don't write tracks which are already present in the
				 * old track store
				 */
				boolean exists = fileManager.exists(oldTrack.getTrackPath());
				boolean isPresent = store.getStoredTracks().contains(oldTrack.getFilename());
				if (exists && !isPresent) {
					try (InputStream in = fileManager.newInputStream(oldTrack.getTrackPath());
							OutputStream out = store.openOutputStream(oldTrack.getFilename())) {
						Utils.writeFromInputStreamToOutputStream(in, out);
					}
					copiedTracks++;
				} else if (isPresent) {
					++presentTracks;
				} else if (!exists) {
					++notFoundTracks;
				}
			}
			this.logger.info(
					"Copied %d of %d tracks of the old Music Pack Project \"%s\" to the track store (%s were already present and %s weren't found)",
					copiedTracks, project.getMusicPack().getTracks().size(), project.getName(), presentTracks,
					notFoundTracks);
		} catch (Exception e) {
			this.logger.error("Couldn't copy the tracks of the old Music Pack Project \"%s\" to the track store",
							project.getName(),
					e);
			LockableTypedProperties properties = new DefaultTypedProperties();
			properties.put(POST_LOAD_SERIALIZED_WORKSPACE_TRACK_COPY_ERROR_EVENT_MUSIC_PACK_PROJECT, project);
			properties.put(POST_LOAD_SERIALIZED_WORKSPACE_TRACK_COPY_ERROR_EVENT_EXCEPTION, e);
			this.eventManager.dispatchEvent(POST_LOAD_SERIALIZED_WORKSPACE_TRACK_COPY_ERROR_EVENT, properties);
		} finally {
			serializedWorkspaceToJsonConverter.getOldProjects().remove(projectPath);
		}
	}

	@Override
	public void applyPreRegisterFixes(MusicPackProject project, String loadedVersion) {
		Objects.requireNonNull(project);
		if (loadedVersion != null) {
			
		}
	}

}

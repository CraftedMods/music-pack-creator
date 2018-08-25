package craftedMods.lotr.mpc.compatibility.provider;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.eventManager.base.DefaultWriteableEventProperties;
import craftedMods.eventManager.base.EventUtils;
import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.compatibility.api.MusicPackProjectCompatibilityManager;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.lotr.mpc.persistence.api.TrackStore;
import craftedMods.lotr.mpc.persistence.api.TrackStoreManager;
import craftedMods.utils.Utils;

@Component
public class MusicPackProjectCompatibilityManagerImpl implements MusicPackProjectCompatibilityManager {

	private static final String ANDRST_FIX_VERSION = "Music Pack Creator Beta 3.3";

	@Reference
	private EventManager eventManager;

	@Reference
	private SerializedWorkspaceToJSONConverter serializedWorkspaceToJsonConverter;

	@Reference
	private LogService logger;

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
			this.logger.log(LogService.LOG_WARNING,
					String.format("Found a Music Pack Project at \"%s\" which contains a serialized project file",
							projectDir.toString()));
			// if (GuiUtils.showWarningConfirmDialog(null,
			// this.creator.getLanguageRegistry()
			// .getEntry("musicPackCreator.musicPackProjectManager.loadProject.convertProject.serializedWorkspace.confirmation",
			// projectDir.toString()))) {
			WriteableEventProperties properties = new DefaultWriteableEventProperties();
			properties.put(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_PATH,
					projectDir);
			if (EventUtils.proceed(this.eventManager.dispatchEvent(
					MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT, properties),
					MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_RESULT_PROCEED))
				try {
					this.serializedWorkspaceToJsonConverter.convertWorkspace(projectDir);
					this.logger.log(LogService.LOG_INFO,
							String.format("Converted the Music Pack Project at \"%s\" to the JSON format", projectDir));
					properties = new DefaultWriteableEventProperties();
					properties.put(
							MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_CONVERTED_EVENT_PATH,
							projectDir);
					this.eventManager.dispatchEvent(
							MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_CONVERTED_EVENT,
							properties);
				} catch (Exception e) {
					this.logger.log(LogService.LOG_ERROR,
							String.format("Couldn't convert the Music Pack Project at \"%s\": ", projectDir.toString()),
							e);
					properties = new DefaultWriteableEventProperties();
					properties.put(MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT_PATH,
							projectDir);
					properties.put(
							MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT_EXCEPTION,
							e);
					this.eventManager.dispatchEvent(
							MusicPackProjectCompatibilityManager.PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT, properties);
					// GuiUtils.showExceptionMessageDialog(null, this.creator.getEntry(
					// "musicPackCreator.musicPackProjectManager.loadProject.convertProject.serializedWorkspace.error",
					// projectDir.toString()), e);
				}
			else
				this.logger.log(LogService.LOG_DEBUG,
						"The user skipped the conversion of the Music Pack Project - it won't be loaded");
		}

	}

	@Override
	public void applyPostLoadFixes(Path workspacePath, MusicPackProject project, String loadedVersion) {
		Objects.requireNonNull(workspacePath);
		Objects.requireNonNull(project);
		if (loadedVersion != null) {
			if (loadedVersion.startsWith("Music Pack Creator")
					&& loadedVersion.compareTo(MusicPackProjectCompatibilityManagerImpl.ANDRST_FIX_VERSION) < 0)
				this.fixAndrastRegion(project);
		}
		if (serializedWorkspaceToJsonConverter.getOldProjects().containsKey(workspacePath))
			copyTracksToStore(workspacePath, project);
	}

	private void fixAndrastRegion(MusicPackProject project) {
		for (Track track : project.getMusicPack().getTracks())
			for (Region region : track.getRegions())
				if (region.getName().equals("andrast")) {
					region.setName("pukel");
					WriteableEventProperties properties = new DefaultWriteableEventProperties();
					properties.put(MusicPackProjectCompatibilityManager.POST_LOAD_ANDRAST_FIX_EVENT_MUSIC_PACK_PROJECT,
							project);
					this.eventManager.dispatchEvent(MusicPackProjectCompatibilityManager.POST_LOAD_ANDRAST_FIX_EVENT,
							properties);
					// GuiUtils.showInformationMessageDialog(null,
					// this.creator.getLanguageRegistry().getEntry(
					// "musicPackCreator.musicPackProjectManager.fixProject.andrastRegion.success",
					// project.getName()));
					this.logger.log(LogService.LOG_INFO, String.format(
							"The region \"andrast\" was found in the Music Pack Project \"%s\" - it was changed to \"pukel\".",
							project.getName()));
				}
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
			this.logger.log(LogService.LOG_INFO, String.format(
					"Copied %d of %d tracks of the old Music Pack Project \"%s\" to the track store (%s were already present and %s weren't found)",
					copiedTracks, project.getMusicPack().getTracks().size(), project.getName(), presentTracks,
					notFoundTracks));
		} catch (Exception e) {
			this.logger.log(LogService.LOG_ERROR,
					String.format("Couldn't copy the tracks of the old Music Pack Project \"%s\" to the track store",
							project.getName()),
					e);
			WriteableEventProperties properties = new DefaultWriteableEventProperties();
			properties.put(POST_LOAD_SERIALIZED_WORKSPACE_TRACK_COPY_ERROR_EVENT_MUSIC_PACK_PROJECT, project);
			properties.put(POST_LOAD_SERIALIZED_WORKSPACE_TRACK_COPY_ERROR_EVENT_EXCEPTION, e);
			this.eventManager.dispatchEvent(POST_LOAD_SERIALIZED_WORKSPACE_TRACK_COPY_ERROR_EVENT, properties);
		} finally {
			serializedWorkspaceToJsonConverter.getOldProjects().remove(projectPath);
		}
	}

}

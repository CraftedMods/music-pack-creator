package craftedMods.lotr.mpc.compatibility.provider;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import org.osgi.service.component.annotations.Activate;
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
import craftedMods.lotr.mpc.core.api.MusicPackProjectFactory;
import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectWriter;
import craftedMods.versionChecker.api.SemanticVersion;

@Component
public class MusicPackProjectCompatibilityManagerImpl implements MusicPackProjectCompatibilityManager {

	private static final String ANDRST_FIX_VERSION = "Music Pack Creator Beta 3.3";

	@Reference
	private EventManager eventManager;

	@Reference(target = "(application=mpc)")
	private SemanticVersion mpcVersion;

	@Reference
	private MusicPackProjectFactory factory;

	@Reference
	private MusicPackProjectWriter writer;

	private SerializedWorkspaceToJSONConverter serializedWorkspaceToJsonConverter = new SerializedWorkspaceToJSONConverter();

	@Reference
	private LogService logger;

	@Reference
	private FileManager fileManager;

	@Activate
	public void onActivate() {
		this.serializedWorkspaceToJsonConverter.onActivate(this.mpcVersion, this.factory, this.writer,
				this.fileManager);
	}

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
	public void applyPostLoadFixes(MusicPackProject project, String loadedVersion) {
		Objects.requireNonNull(project);
		Objects.requireNonNull(loadedVersion);
		if (loadedVersion.startsWith("Music Pack Creator")
				&& loadedVersion.compareTo(MusicPackProjectCompatibilityManagerImpl.ANDRST_FIX_VERSION) < 0)
			this.fixAndrastRegion(project);
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

}

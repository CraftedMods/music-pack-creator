package craftedMods.lotr.mpc.persistence.provider;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.osgi.framework.ServiceException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.eventManager.base.DefaultWriteableEventProperties;
import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.compatibility.api.MusicPackProjectCompatibilityManager;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectPersistenceManager;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectReader;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectWriter;
import craftedMods.lotr.mpc.persistence.api.TrackStoreManager;
import craftedMods.versionChecker.api.SemanticVersion;
import craftedMods.versionChecker.base.DefaultSemanticVersion;

@Component(configurationPolicy = ConfigurationPolicy.REQUIRE)
public class MusicPackProjectPersistenceManagerImpl implements MusicPackProjectPersistenceManager {

	public @interface Configuration {
		String projectsDirectory();
	}

	public static final String PROJECT_FILE_NAME = "project.json";

	@Reference(target = "(application=mpc)")
	SemanticVersion mpcVersion;

	@Reference
	private MusicPackProjectReader reader;

	@Reference
	private MusicPackProjectWriter writer;

	@Reference
	private LogService logger;

	@Reference
	private EventManager eventManager;

	@Reference
	private MusicPackProjectCompatibilityManager compatibilityManager;

	@Reference
	private FileManager fileManager;

	private Path projectsDir;

	@Reference
	private MusicPackProjectManager musicPackProjectManager;

	@Reference
	private TrackStoreManager trackStoreManager;

	@Activate
	public void onActivate(Configuration configuration) throws IOException {
		this.projectsDir = this.fileManager.getPathAndCreateDir(configuration.projectsDirectory());
		this.logger.log(LogService.LOG_INFO,
				String.format("The projects directory is located at \"%s\"", this.projectsDir.toString()));
		this.logger.log(LogService.LOG_INFO,
				this.compatibilityManager == null ? "No compatibility manager service was found"
						: "Found a compatibility manager service");
	}

	@Override
	public Collection<MusicPackProject> loadMusicPackProjects() {
		this.musicPackProjectManager.getManagedMusicPackProjects().clear();
		try {
			List<Path> projectFolders = fileManager.getPathsInDirectory(this.projectsDir)
					.filter(fileManager::isDirectory).collect(Collectors.toList());
			for (Path projectFolder : projectFolders) {
				try {
					this.loadMusicPackProject(projectFolder);
				} catch (Exception e) {
					this.logger.log(LogService.LOG_ERROR,
							String.format("The Music Pack Project at \"%s\" couldn't be loaded: ", projectFolder), e);
					WriteableEventProperties properties = new DefaultWriteableEventProperties();
					properties.put(MusicPackProjectPersistenceManager.LOAD_ALL_PROJECT_ERROR_EVENT_EXCEPTION, e);
					this.eventManager.dispatchEvent(MusicPackProjectPersistenceManager.LOAD_ALL_PROJECT_ERROR_EVENT,
							properties);
				}
			}
			this.logger.log(LogService.LOG_INFO, String.format("Loaded %d Music Pack Projects",
					this.musicPackProjectManager.getManagedMusicPackProjects().keySet().size()));
		} catch (IOException e) {
			throw new ServiceException("Couldn't load the Music Pack Projects from the workspace: ", e);
		}
		return this.musicPackProjectManager.getManagedMusicPackProjects().keySet();
	}

	private void loadMusicPackProject(Path projectFolder) {
		if (this.compatibilityManager != null) {
			this.compatibilityManager.applyPreLoadFixes(projectFolder);
		}
		if (fileManager.exists(
				Paths.get(projectFolder.toString(), MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME))) {
			try {
				MusicPackProject project = this.reader.readMusicPackProject(fileManager.newInputStream(
						Paths.get(projectFolder.toString(), MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME)));
				String version = project.getProperties().getString(MusicPackProject.PROPERTY_MPC_VERSION, null);
				if (version != null) {
					// A non-semantic version is handled like an older version
					boolean isSemanticVersion = DefaultSemanticVersion.isSemanticVersion(version);
					int comp = !isSemanticVersion ? -1 : DefaultSemanticVersion.of(version).compareTo(mpcVersion);
					if (comp != 0) {
						WriteableEventProperties properties = new DefaultWriteableEventProperties();
						boolean newer = comp > 0;
						properties.put(newer
								? MusicPackProjectPersistenceManager.NEWER_SAVE_VERSION_EVENT_MUSIC_PACK_PROJECT
								: MusicPackProjectPersistenceManager.OLDER_SAVE_VERSION_EVENT_MUSIC_PACK_PROJECT,
								project);
						properties.put(
								newer ? MusicPackProjectPersistenceManager.NEWER_SAVE_VERSION_EVENT_DETECTED_VERSION
										: MusicPackProjectPersistenceManager.OLDER_SAVE_VERSION_EVENT_DETECTED_VERSION,
								version);
						this.eventManager
								.dispatchEvent(
										newer ? MusicPackProjectPersistenceManager.NEWER_SAVE_VERSION_EVENT
												: MusicPackProjectPersistenceManager.OLDER_SAVE_VERSION_EVENT,
										properties);
						// this.creator.getLogger().warning(String.format(newer
						// ? "The Music Pack Project at \"%s\" was created with a newer version (%s) of
						// Music Pack Creator"
						// : "The Music Pack Project at \"%s\" was created with an older version (%s) of
						// Music Pack Creator",
						// projectDir.toString(), version));
						// GuiUtils.showWarningMessageDialog(null,
						// this.creator.getLanguageRegistry().getEntry(
						// newer ?
						// "musicPackCreator.musicPackProjectManager.loadProject.warning.newerVersion"
						// :
						// "musicPackCreator.musicPackProjectManager.loadProject.warning.olderVersion",
						// projectDir.getFileName().toString(), version));
					}
				}
				this.musicPackProjectManager.getManagedMusicPackProjects().put(project, projectFolder);
				if (this.compatibilityManager != null) {
					this.compatibilityManager.applyPostLoadFixes(projectFolder, project, version);
				}
			} catch (IOException e) {
				throw new ServiceException(
						String.format("Couldn't load the Music Pack Project from \"%s\"", projectFolder.toString()), e);
			}
		} else {
			this.logger.log(LogService.LOG_WARNING,
					String.format(
							"Found a directory \"%s\" in the projects folder which didn't contain the project file",
							projectFolder.toString()));
		}

	}

	@Override
	public void saveMusicPackProject(MusicPackProject project) {
		Objects.requireNonNull(project);
		if (!this.musicPackProjectManager.getManagedMusicPackProjects().containsKey(project)) {
			try {
				Path projectDir = this.getUnusedMusicPackProjectDir(project.getName());
				fileManager.createDir(projectDir);
				this.musicPackProjectManager.getManagedMusicPackProjects().put(project, projectDir);
			} catch (IOException e) {
				throw new ServiceException(
						String.format("Couldn't create the saves directory for the Music Pack Project \"%s\": ",
								project.getName()),
						e);
			}
		}
		try {
			this.writer.writeMusicPackProject(project,
					fileManager.newOutputStream(Paths.get(
							this.musicPackProjectManager.getManagedMusicPackProjects().get(project).toString(),
							MusicPackProjectPersistenceManagerImpl.PROJECT_FILE_NAME)));
		} catch (IOException e) {
			throw new ServiceException(
					String.format("Couldn't write the Music Pack Project \"%s\": ", project.getName()), e);
		}
	}

	private Path getUnusedMusicPackProjectDir(String projectName) {
		Path projectDir;
		for (int i = 0; fileManager.exists(
				projectDir = Paths.get(this.projectsDir.toString(), projectName + (i == 0 ? "" : "_" + i))); i++) {
		}
		return projectDir;
	}

	@Override
	public boolean deleteMusicPackProject(MusicPackProject project) {
		Objects.requireNonNull(project);
		if (this.musicPackProjectManager.getManagedMusicPackProjects().containsKey(project)) {
			try {
				this.fileManager
						.deleteDirAndContent(this.musicPackProjectManager.getManagedMusicPackProjects().get(project));
				this.trackStoreManager.deleteTrackStore(project);
				this.musicPackProjectManager.getManagedMusicPackProjects().remove(project);
				return true;
			} catch (IOException e) {
				throw new ServiceException(String.format(
						"Couldn't delete the directory of the Music Pack Project \"%s\": ", project.getName()), e);
			}
		}
		return false;
	}
}

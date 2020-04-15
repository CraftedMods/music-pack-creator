package craftedMods.lotr.mpc.persistence.api;

import java.nio.file.Path;
import java.util.Collection;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.utils.data.TypedPropertyKey;

@ProviderType
public interface MusicPackProjectPersistenceManager {

	public static final EventInfo LOAD_ALL_PROJECT_ERROR_EVENT = new DefaultEventInfo(
			MusicPackProjectPersistenceManager.class, "LOAD_ALL_PROJECT_ERROR");

	public static final EventInfo OLDER_SAVE_VERSION_EVENT = new DefaultEventInfo(
			MusicPackProjectPersistenceManager.class, "OLDER_SAVE_VERSION");
	public static final EventInfo NEWER_SAVE_VERSION_EVENT = new DefaultEventInfo(
			MusicPackProjectPersistenceManager.class, "NEWER_SAVE_VERSION");

	public static final TypedPropertyKey<Exception> LOAD_ALL_PROJECT_ERROR_EVENT_EXCEPTION = TypedPropertyKey
			.createPropertyKey(Exception.class);
	public static final TypedPropertyKey<Path> LOAD_ALL_PROJECT_ERROR_EVENT_MUSIC_PACK_PROJECT_PATH = TypedPropertyKey
        .createPropertyKey(Path.class);

	public static final TypedPropertyKey<MusicPackProject> OLDER_SAVE_VERSION_EVENT_MUSIC_PACK_PROJECT = TypedPropertyKey
			.createPropertyKey(MusicPackProject.class);
	public static final TypedPropertyKey<String> OLDER_SAVE_VERSION_EVENT_DETECTED_VERSION = TypedPropertyKey
			.createStringPropertyKey();

	public static final TypedPropertyKey<MusicPackProject> NEWER_SAVE_VERSION_EVENT_MUSIC_PACK_PROJECT = TypedPropertyKey
			.createPropertyKey(MusicPackProject.class);
	public static final TypedPropertyKey<String> NEWER_SAVE_VERSION_EVENT_DETECTED_VERSION = TypedPropertyKey
			.createStringPropertyKey();

	/**
	 * Load all discovered Music Pack Project from the default workspace. If a
	 * project fails to load, the {@link #LOAD_ALL_PROJECT_ERROR_EVENT} will be
	 * fired.
	 * 
	 * @return A collection of loaded Music Pack Projects
	 */
	public Collection<MusicPackProject> loadMusicPackProjects();

	/**
	 * Saves a registered Music Pack Project to the workspace
	 * 
	 * @param project The registered Music Pack Project
	 */
	public void saveMusicPackProject(MusicPackProject project);

	/**
	 * Deletes a registered Music Pack Project from the workspace
	 * 
	 * @param project The registered Music Pack Project
	 */
	public boolean deleteMusicPackProject(MusicPackProject project);

}

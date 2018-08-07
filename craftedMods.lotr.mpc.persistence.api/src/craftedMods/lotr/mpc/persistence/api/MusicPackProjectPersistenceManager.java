package craftedMods.lotr.mpc.persistence.api;

import java.util.Collection;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.api.PropertyKey;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.eventManager.base.DefaultPropertyKey;
import craftedMods.lotr.mpc.core.api.MusicPackProject;

@ProviderType
public interface MusicPackProjectPersistenceManager {

	public static final EventInfo LOAD_ALL_PROJECT_ERROR_EVENT = new DefaultEventInfo(
			MusicPackProjectPersistenceManager.class, "LOAD_ALL_ERROR");

	public static final EventInfo OLDER_SAVE_VERSION_EVENT = new DefaultEventInfo(
			MusicPackProjectPersistenceManager.class, "OLDER_SAVE_VERSION");
	public static final EventInfo NEWER_SAVE_VERSION_EVENT = new DefaultEventInfo(
			MusicPackProjectPersistenceManager.class, "NEWER_SAVE_VERSION");

	public static final PropertyKey<Exception> LOAD_ALL_PROJECT_ERROR_EVENT_EXCEPTION = DefaultPropertyKey
			.createPropertyKey(Exception.class);

	public static final PropertyKey<MusicPackProject> OLDER_SAVE_VERSION_EVENT_MUSIC_PACK_PROJECT = DefaultPropertyKey
			.createPropertyKey(MusicPackProject.class);
	public static final PropertyKey<String> OLDER_SAVE_VERSION_EVENT_DETECTED_VERSION = DefaultPropertyKey
			.createStringPropertyKey();

	public static final PropertyKey<MusicPackProject> NEWER_SAVE_VERSION_EVENT_MUSIC_PACK_PROJECT = DefaultPropertyKey
			.createPropertyKey(MusicPackProject.class);
	public static final PropertyKey<String> NEWER_SAVE_VERSION_EVENT_DETECTED_VERSION = DefaultPropertyKey
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

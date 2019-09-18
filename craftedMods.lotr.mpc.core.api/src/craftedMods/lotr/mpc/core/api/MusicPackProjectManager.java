package craftedMods.lotr.mpc.core.api;

import java.util.Collection;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.utils.data.TypedPropertyKey;
import craftedMods.utils.exceptions.ErrorCode;
import craftedMods.utils.exceptions.InvalidInputException;

@ProviderType
public interface MusicPackProjectManager {

	public static final EventInfo LOAD_ALL_REGISTER_PROJECT_ERROR_EVENT = new DefaultEventInfo(
			MusicPackProjectManager.class, "LOAD_ALL_ERROR");
	public static final EventInfo SAVE_ALL_PROJECT_ERROR_EVENT = new DefaultEventInfo(MusicPackProjectManager.class,
			"SAVE_ALL_ERROR");

	public static final TypedPropertyKey<Exception> LOAD_ALL_REGISTER_PROJECT_ERROR_EVENT_EXCEPTION = TypedPropertyKey
			.createPropertyKey(Exception.class);

	public static final TypedPropertyKey<MusicPackProject> SAVE_ALL_PROJECT_ERROR_EVENT_MUSIC_PACK_PROJECT = TypedPropertyKey
			.createPropertyKey(MusicPackProject.class);
	public static final TypedPropertyKey<Exception> SAVE_ALL_PROJECT_ERROR_EVENT_EXCEPTION = TypedPropertyKey
			.createPropertyKey(Exception.class);

	public MusicPackProject registerMusicPackProject(MusicPackProject suggestedInstance) throws InvalidInputException;

	public String getUnusedMusicPackProjectName(String suggestedName);

	public boolean renameMusicPackProject(MusicPackProject project, String newName) throws InvalidInputException;

	public void deleteMusicPackProject(MusicPackProject project);

	public Collection<MusicPackProject> saveAllMusicPackProjects();

	public void saveMusicPackProject(MusicPackProject project);

	public Collection<MusicPackProject> getRegisteredMusicPackProjects();

	public enum MusicPackProjectNameErrors implements ErrorCode {
		EMPTY, DUPLICATED;
	}

}

package craftedMods.lotr.mpc.compatibility.api;

import java.nio.file.Path;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.eventManager.api.*;
import craftedMods.eventManager.base.*;
import craftedMods.lotr.mpc.core.api.MusicPackProject;

@ProviderType
public interface MusicPackProjectCompatibilityManager {

	public static final EventInfo PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT = new DefaultEventInfo(
			MusicPackProjectCompatibilityManager.class, "PRE_LOAD/SERIALIZED_WORKSPACE/DETECTED",
			EventDispatchPolicy.SYNCHRONOUS);
	public static final EventInfo PRE_LOAD_SERIALIZED_WORKSPACE_CONVERTED_EVENT = new DefaultEventInfo(
			MusicPackProjectCompatibilityManager.class, "PRE_LOAD/SERIALIZED_WORKSPACE/CONVERTED");
	public static final EventInfo PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT = new DefaultEventInfo(
			MusicPackProjectCompatibilityManager.class, "PRE_LOAD/SERIALIZED_WORKSPACE/ERROR");

	public static final EventInfo POST_LOAD_ANDRAST_FIX_EVENT = new DefaultEventInfo(
			MusicPackProjectCompatibilityManager.class, "POST_LOAD/ANDRAST_FIX");
	public static final EventInfo POST_LOAD_SERIALIZED_WORKSPACE_TRACK_COPY_ERROR_EVENT = new DefaultEventInfo(
			MusicPackProjectCompatibilityManager.class, "POST_LOAD/SERIALIZED_WORKSPACE/TRACK_COPY_ERROR");

	public static final PropertyKey<Path> PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_PATH = DefaultPropertyKey
			.createPropertyKey(Path.class);
	public static final PropertyKey<Boolean> PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_RESULT_PROCEED = DefaultPropertyKey
			.createBooleanPropertyKey();

	public static final PropertyKey<Path> PRE_LOAD_SERIALIZED_WORKSPACE_CONVERTED_EVENT_PATH = DefaultPropertyKey
			.createPropertyKey(Path.class);

	public static final PropertyKey<Path> PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT_PATH = DefaultPropertyKey
			.createPropertyKey(Path.class);
	public static final PropertyKey<Exception> PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT_EXCEPTION = DefaultPropertyKey
			.createPropertyKey(Exception.class);

	public static final PropertyKey<MusicPackProject> POST_LOAD_ANDRAST_FIX_EVENT_MUSIC_PACK_PROJECT = DefaultPropertyKey
			.createPropertyKey(MusicPackProject.class);

	public static final PropertyKey<MusicPackProject> POST_LOAD_SERIALIZED_WORKSPACE_TRACK_COPY_ERROR_EVENT_MUSIC_PACK_PROJECT = DefaultPropertyKey
			.createPropertyKey(MusicPackProject.class);
	public static final PropertyKey<Exception> POST_LOAD_SERIALIZED_WORKSPACE_TRACK_COPY_ERROR_EVENT_EXCEPTION = DefaultPropertyKey
			.createPropertyKey(Exception.class);

	public void applyPreLoadFixes(Path workspacePath);

	public void applyPostLoadFixes(Path workspacePath, MusicPackProject project, String loadedVersion);// Version can be
																										// null
	public void applyPreRegisterFixes(MusicPackProject project, String loadedVersion);// Version can be
	// null

}

package craftedMods.lotr.mpc.compatibility.api;

import java.nio.file.Path;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.eventManager.api.EventDispatchPolicy;
import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.utils.data.TypedPropertyKey;

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

	public static final TypedPropertyKey<Path> PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_PATH = TypedPropertyKey
			.createPropertyKey(Path.class);
	public static final TypedPropertyKey<Boolean> PRE_LOAD_SERIALIZED_WORKSPACE_DETECTED_EVENT_RESULT_PROCEED = TypedPropertyKey
			.createBooleanPropertyKey();

	public static final TypedPropertyKey<Path> PRE_LOAD_SERIALIZED_WORKSPACE_CONVERTED_EVENT_PATH = TypedPropertyKey
			.createPropertyKey(Path.class);

	public static final TypedPropertyKey<Path> PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT_PATH = TypedPropertyKey
			.createPropertyKey(Path.class);
	public static final TypedPropertyKey<Exception> PRE_LOAD_SERIALIZED_WORKSPACE_ERROR_EVENT_EXCEPTION = TypedPropertyKey
			.createPropertyKey(Exception.class);

	public static final TypedPropertyKey<MusicPackProject> POST_LOAD_ANDRAST_FIX_EVENT_MUSIC_PACK_PROJECT = TypedPropertyKey
			.createPropertyKey(MusicPackProject.class);

	public static final TypedPropertyKey<MusicPackProject> POST_LOAD_SERIALIZED_WORKSPACE_TRACK_COPY_ERROR_EVENT_MUSIC_PACK_PROJECT = TypedPropertyKey
			.createPropertyKey(MusicPackProject.class);
	public static final TypedPropertyKey<Exception> POST_LOAD_SERIALIZED_WORKSPACE_TRACK_COPY_ERROR_EVENT_EXCEPTION = TypedPropertyKey
			.createPropertyKey(Exception.class);

	public void applyPreLoadFixes(Path workspacePath);

	public void applyPostLoadFixes(Path workspacePath, MusicPackProject project, String loadedVersion);// Version can be
																										// null
	public void applyPreRegisterFixes(MusicPackProject project, String loadedVersion);// Version can be
	// null

}

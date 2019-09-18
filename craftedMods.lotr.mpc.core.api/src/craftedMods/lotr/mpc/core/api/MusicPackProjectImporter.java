package craftedMods.lotr.mpc.core.api;

import java.nio.file.Path;
import java.util.Collection;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.eventManager.api.EventDispatchPolicy;
import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.utils.data.TypedPropertyKey;

@ProviderType
public interface MusicPackProjectImporter {

	public static final EventInfo READING_FILE_EVENT = new DefaultEventInfo(MusicPackProjectImporter.class,
			"READING_FILE", EventDispatchPolicy.SYNCHRONOUS);

	public static final EventInfo FILE_NOT_FOUND_EVENT = new DefaultEventInfo(MusicPackProjectImporter.class,
			"FILE_NOT_FOUND", EventDispatchPolicy.SYNCHRONOUS);

	public static final EventInfo INVALID_MUSIC_JSON_EVENT = new DefaultEventInfo(MusicPackProjectImporter.class,
			"INVALID_MUSIC_JSON", EventDispatchPolicy.SYNCHRONOUS);

	public static final EventInfo NON_OGG_TRACK_FILES_FOUND_EVENT = new DefaultEventInfo(MusicPackProjectImporter.class,
			"NON_OGG_TRACK_FILES_FOUND", EventDispatchPolicy.SYNCHRONOUS);

	public static final EventInfo TRACK_COUNT_DETERMINED_EVENT = new DefaultEventInfo(MusicPackProjectImporter.class,
			"TRACK_COUNT_DETERMINED", EventDispatchPolicy.SYNCHRONOUS);

	public static final EventInfo COPYING_TRACK_EVENT = new DefaultEventInfo(MusicPackProjectImporter.class,
			"COPYING_TRACK", EventDispatchPolicy.SYNCHRONOUS);

	public static final EventInfo PRE_SUCCESS_EVENT = new DefaultEventInfo(MusicPackProjectImporter.class,
			"SUCCESS/PRE", EventDispatchPolicy.SYNCHRONOUS);
	public static final EventInfo SUCCESS_EVENT = new DefaultEventInfo(MusicPackProjectImporter.class, "SUCCESS");
	public static final EventInfo CANCEL_EVENT = new DefaultEventInfo(MusicPackProjectImporter.class, "CANCEL");
	public static final EventInfo UNEXPECTED_ERROR_EVENT = new DefaultEventInfo(MusicPackProjectImporter.class,
			"ERROR");

	public static final TypedPropertyKey<Path> COMMON_EVENT_LOCATION = TypedPropertyKey.createPropertyKey(Path.class);

	public static final TypedPropertyKey<String> READING_FILE_EVENT_FILENAME = TypedPropertyKey.createStringPropertyKey();

	public static final TypedPropertyKey<String> FILE_NOT_FOUND_EVENT_FILENAME = TypedPropertyKey
			.createStringPropertyKey();
	public static final TypedPropertyKey<Boolean> FILE_NOT_FOUND_EVENT_RESULT_PROCEED = TypedPropertyKey
			.createBooleanPropertyKey();

	@SuppressWarnings("unchecked")
	public static final TypedPropertyKey<Collection<String>> NON_OGG_TRACK_FILES_FOUND_EVENT_FILENAMES = (TypedPropertyKey<Collection<String>>) (TypedPropertyKey<?>) TypedPropertyKey
			.createPropertyKey(Collection.class);
	public static final TypedPropertyKey<Boolean> NON_OGG_TRACK_FILES_FOUND_EVENT_ENTRIES_RESULT_PROCEED = TypedPropertyKey
			.createBooleanPropertyKey();

	public static final TypedPropertyKey<Integer> TRACK_COUNT_DETERMINED_EVENT_TRACK_COUNT = TypedPropertyKey
			.createIntegerPropertyKey();
	public static final TypedPropertyKey<Boolean> TRACK_COUNT_DETERMINED_EVENT_RESULT_PROCEED = TypedPropertyKey
			.createBooleanPropertyKey();

	public static final TypedPropertyKey<String> COPYING_TRACK_EVENT_TRACK_NAME = TypedPropertyKey
			.createStringPropertyKey();
	public static final TypedPropertyKey<Boolean> COPYING_TRACK_EVENT_RESULT_PROCEED = TypedPropertyKey
			.createBooleanPropertyKey();

	public static final TypedPropertyKey<Boolean> PRE_SUCCESS_EVENT_RESULT_PROCEED = TypedPropertyKey
			.createBooleanPropertyKey();

	public static final TypedPropertyKey<String> SUCCESS_EVENT_PROJECT_NAME = TypedPropertyKey.createStringPropertyKey();

	public static final TypedPropertyKey<Exception> UNEXPECTED_ERROR_EVENT_EXCEPTION = TypedPropertyKey
			.createPropertyKey(Exception.class);

	public MusicPackProject importMusicPackProject(Path location);

}

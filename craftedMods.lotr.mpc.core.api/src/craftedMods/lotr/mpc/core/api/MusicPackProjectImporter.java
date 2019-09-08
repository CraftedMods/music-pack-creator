package craftedMods.lotr.mpc.core.api;

import java.nio.file.Path;
import java.util.Collection;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.eventManager.api.EventDispatchPolicy;
import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.api.PropertyKey;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.eventManager.base.DefaultPropertyKey;

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

	public static final PropertyKey<Path> COMMON_EVENT_LOCATION = DefaultPropertyKey.createPropertyKey(Path.class);

	public static final PropertyKey<String> READING_FILE_EVENT_FILENAME = DefaultPropertyKey.createStringPropertyKey();

	public static final PropertyKey<String> FILE_NOT_FOUND_EVENT_FILENAME = DefaultPropertyKey
			.createStringPropertyKey();
	public static final PropertyKey<Boolean> FILE_NOT_FOUND_EVENT_RESULT_PROCEED = DefaultPropertyKey
			.createBooleanPropertyKey();

	@SuppressWarnings("unchecked")
	public static final PropertyKey<Collection<String>> NON_OGG_TRACK_FILES_FOUND_EVENT_FILENAMES = (PropertyKey<Collection<String>>) (PropertyKey<?>) DefaultPropertyKey
			.createPropertyKey(Collection.class);
	public static final PropertyKey<Boolean> NON_OGG_TRACK_FILES_FOUND_EVENT_ENTRIES_RESULT_PROCEED = DefaultPropertyKey
			.createBooleanPropertyKey();

	public static final PropertyKey<Integer> TRACK_COUNT_DETERMINED_EVENT_TRACK_COUNT = DefaultPropertyKey
			.createIntegerPropertyKey();
	public static final PropertyKey<Boolean> TRACK_COUNT_DETERMINED_EVENT_RESULT_PROCEED = DefaultPropertyKey
			.createBooleanPropertyKey();

	public static final PropertyKey<String> COPYING_TRACK_EVENT_TRACK_NAME = DefaultPropertyKey
			.createStringPropertyKey();
	public static final PropertyKey<Boolean> COPYING_TRACK_EVENT_RESULT_PROCEED = DefaultPropertyKey
			.createBooleanPropertyKey();

	public static final PropertyKey<Boolean> PRE_SUCCESS_EVENT_RESULT_PROCEED = DefaultPropertyKey
			.createBooleanPropertyKey();

	public static final PropertyKey<String> SUCCESS_EVENT_PROJECT_NAME = DefaultPropertyKey.createStringPropertyKey();

	public static final PropertyKey<Exception> UNEXPECTED_ERROR_EVENT_EXCEPTION = DefaultPropertyKey
			.createPropertyKey(Exception.class);

	public MusicPackProject importMusicPackProject(Path location);

}

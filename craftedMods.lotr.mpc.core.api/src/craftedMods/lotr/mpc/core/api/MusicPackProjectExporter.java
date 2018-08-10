package craftedMods.lotr.mpc.core.api;

import java.nio.file.Path;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.eventManager.api.EventDispatchPolicy;
import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.api.PropertyKey;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.eventManager.base.DefaultPropertyKey;

@ProviderType
public interface MusicPackProjectExporter {

	public static final EventInfo EXPORT_LOCATION_EXISTS_EVENT = new DefaultEventInfo(MusicPackProjectExporter.class,
			"EXPORT_LOCATION_EXISTS", EventDispatchPolicy.SYNCHRONOUS);
	public static final EventInfo CREATING_FILE_EVENT = new DefaultEventInfo(MusicPackProjectExporter.class,
			"CREATING_FILE", EventDispatchPolicy.SYNCHRONOUS);
	public static final EventInfo COPYING_TRACK_EVENT = new DefaultEventInfo(MusicPackProjectExporter.class,
			"COPYING_TRACK", EventDispatchPolicy.SYNCHRONOUS);
	public static final EventInfo PRE_SUCCESS_EVENT = new DefaultEventInfo(MusicPackProjectExporter.class,
			"SUCCESS/PRE", EventDispatchPolicy.SYNCHRONOUS);
	public static final EventInfo SUCCESS_EVENT = new DefaultEventInfo(MusicPackProjectExporter.class, "SUCCESS");
	public static final EventInfo CANCEL_EVENT = new DefaultEventInfo(MusicPackProjectExporter.class, "CANCEL");
	public static final EventInfo ERROR_EVENT = new DefaultEventInfo(MusicPackProjectExporter.class, "ERROR");

	public static final PropertyKey<Path> COMMON_EVENT_LOCATION = DefaultPropertyKey.createPropertyKey(Path.class);
	public static final PropertyKey<MusicPackProject> COMMON_EVENT_MUSIC_PACK_PROJECT = DefaultPropertyKey
			.createPropertyKey(MusicPackProject.class);

	public static final PropertyKey<Boolean> EXPORT_LOCATION_EXISTS_EVENT_RESULT_OVERRIDE = DefaultPropertyKey
			.createBooleanPropertyKey();

	public static final PropertyKey<String> CREATING_FILE_EVENT_FILENAME = DefaultPropertyKey.createStringPropertyKey();

	public static final PropertyKey<Path> COPYING_TRACK_EVENT_TRACK_PATH = DefaultPropertyKey
			.createPropertyKey(Path.class);
	public static final PropertyKey<Boolean> COPYING_TRACK_EVENT_RESULT_PROCEED = DefaultPropertyKey
			.createBooleanPropertyKey();

	public static final PropertyKey<Boolean> PRE_SUCCESS_EVENT_RESULT_PROCEED = DefaultPropertyKey
			.createBooleanPropertyKey();
	public static final PropertyKey<Exception> ERROR_EVENT_EXCEPTION = DefaultPropertyKey
			.createPropertyKey(Exception.class);

	// LOTR specification
	public static final String BASE_FILE = "music.json";
	public static final String JSON_TRACKS = "tracks";
	public static final String JSON_TRACK_FILE_NAME = "file";
	public static final String JSON_TRACK_TITLE = "title";
	public static final String JSON_TRACK_REGIONS = "regions";
	public static final String JSON_REGION_NAME = "name";
	public static final String JSON_REGION_SUB = "sub";
	public static final String JSON_REGION_CATEGORIES = "categories";
	public static final String JSON_REGION_WEIGHT = "weight";
	public static final String JSON_TRACK_AUTHORS = "authors";
	public static final String TRACKS_DIR = "assets/lotrmusic/";

	// MPC specification
	public static final String PACK_FILE = "pack.properties";
	public static final String PACK_PROJECT_NAME_KEY = "projectName";
	public static final String PACK_TRACKS_KEY = "trackCount";

	public void exportMusicPackProject(Path location, MusicPackProject packProject);

}

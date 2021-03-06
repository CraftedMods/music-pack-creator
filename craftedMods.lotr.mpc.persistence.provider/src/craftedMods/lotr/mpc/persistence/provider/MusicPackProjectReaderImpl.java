package craftedMods.lotr.mpc.persistence.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.core.api.MusicPackProjectFactory;
import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.lotr.mpc.core.base.DefaultRegion;
import craftedMods.lotr.mpc.core.base.DefaultTrack;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectReader;

@Component
public class MusicPackProjectReaderImpl implements MusicPackProjectReader {

	@Reference
	private MusicPackProjectFactory factory;

	@Override
	public MusicPackProject readMusicPackProject(InputStream projectData) throws IOException {
		Objects.requireNonNull(projectData);
		MusicPackProject project = null;
		try (InputStreamReader bridge = new InputStreamReader(projectData);
				JsonReader reader = new JsonReader(bridge)) {
			JsonParser parser = new JsonParser();
			JsonObject root = parser.parse(reader).getAsJsonObject();
			JsonObject projectObj = root.getAsJsonObject(MusicPackProjectWriterImpl.JSON_PROJECT);
			project = this.factory.createMusicPackProjectInstance(
					projectObj.get(MusicPackProjectWriterImpl.JSON_PROJECT_NAME).getAsString());
			project.getProperties()
					.putAll(this.readStringMap(projectObj, MusicPackProjectWriterImpl.JSON_PROJECT_PROPERTIES));
			project.getMusicPack().getTracks().addAll(this.readTracks(projectObj));
		}
		projectData.close();
		return project;
	}

	private List<Track> readTracks(JsonObject projectObject) {
		List<Track> tracks = new ArrayList<>();
		projectObject.getAsJsonArray(MusicPackProjectWriterImpl.JSON_PROJECT_TRACKS).forEach(trackElement -> {
			JsonObject trackObj = trackElement.getAsJsonObject();
			tracks.add(new DefaultTrack(trackObj.get(MusicPackProjectWriterImpl.JSON_TRACK_NAME).getAsString(),
					trackObj.has(MusicPackProjectWriterImpl.JSON_TRACK_TITLE)
							? trackObj.get(MusicPackProjectWriterImpl.JSON_TRACK_TITLE).getAsString()
							: null,
					this.readRegions(trackObj),
					this.readStringArray(trackObj, MusicPackProjectWriterImpl.JSON_TRACK_AUTHORS)));
		});
		return tracks;
	}

	private List<Region> readRegions(JsonObject trackObject) {
		List<Region> regions = new ArrayList<>();
		trackObject.getAsJsonArray(MusicPackProjectWriterImpl.JSON_TRACK_REGIONS).forEach(regionElement -> {
			JsonObject regionObj = regionElement.getAsJsonObject();
			regions.add(new DefaultRegion(regionObj.get(MusicPackProjectWriterImpl.JSON_REGION_NAME).getAsString(),
					this.readStringArray(regionObj, MusicPackProjectWriterImpl.JSON_REGION_SUBREGIONS),
					this.readStringArray(regionObj, MusicPackProjectWriterImpl.JSON_REGION_CATEGORIES),
					regionObj.has(MusicPackProjectWriterImpl.JSON_REGION_WEIGHT)
							? Float.valueOf(regionObj.get(MusicPackProjectWriterImpl.JSON_REGION_WEIGHT).getAsString())
							: null));
		});
		return regions;
	}

	private List<String> readStringArray(JsonObject object, String name) {
		List<String> entries = new ArrayList<>();
		object.getAsJsonArray(name).forEach(element -> entries.add(element.getAsString()));
		return entries;
	}

	private Map<String, String> readStringMap(JsonObject object, String name) {
		Map<String, String> ret = new HashMap<>();
		for (JsonElement element : object.getAsJsonArray(name)) {
			JsonObject elementObj = element.getAsJsonObject();
			elementObj.entrySet().forEach(entry -> {
				ret.put(entry.getKey(), entry.getValue().getAsString());
			});
		}
		return ret;
	}

}

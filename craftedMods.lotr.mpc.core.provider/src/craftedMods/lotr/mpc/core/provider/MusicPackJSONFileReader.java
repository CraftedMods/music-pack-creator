package craftedMods.lotr.mpc.core.provider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import craftedMods.lotr.mpc.core.api.MusicPackProjectExporter;
import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.lotr.mpc.core.base.DefaultRegion;
import craftedMods.lotr.mpc.core.base.DefaultTrack;

@Component(service = MusicPackJSONFileReader.class)
public class MusicPackJSONFileReader {

	public List<Track> readJSONFile(byte[] content) throws IOException {
		ArrayList<Track> tracks = new ArrayList<>();
		try (ByteArrayInputStream in = new ByteArrayInputStream(content);
				JsonReader reader = new JsonReader(new InputStreamReader(in))) {
			JsonParser parser = new JsonParser();
			JsonElement root = parser.parse(reader);
			root.getAsJsonObject().getAsJsonArray(MusicPackProjectExporter.JSON_TRACKS).forEach(track -> {
				JsonObject trackObj = track.getAsJsonObject();
				String trackName = trackObj.get(MusicPackProjectExporter.JSON_TRACK_NAME).getAsString();
				String title = null;
				if (trackObj.get(MusicPackProjectExporter.JSON_TRACK_TITLE) != null) {
					title = trackObj.get(MusicPackProjectExporter.JSON_TRACK_TITLE).getAsString();
				}
				ArrayList<Region> regions = new ArrayList<>();
				trackObj.getAsJsonArray(MusicPackProjectExporter.JSON_TRACK_REGIONS).forEach(element -> {
					JsonObject region = element.getAsJsonObject();
					String name = region.get(MusicPackProjectExporter.JSON_REGION_NAME).getAsString();
					ArrayList<String> subRegions = new ArrayList<>();
					JsonArray sub = region.getAsJsonArray(MusicPackProjectExporter.JSON_REGION_SUB);
					if (sub != null) {
						sub.forEach(element2 -> {
							subRegions.add(element2.getAsString());
						});
					}
					ArrayList<String> categories = new ArrayList<>();
					JsonArray cat = region.getAsJsonArray(MusicPackProjectExporter.JSON_REGION_CATEGORIES);
					if (cat != null) {
						cat.forEach(element2 -> {
							categories.add(element2.getAsString());
						});
					}
					Float weight = null;
					if (region.get(MusicPackProjectExporter.JSON_REGION_WEIGHT) != null) {
						weight = Float.valueOf(region.get(MusicPackProjectExporter.JSON_REGION_WEIGHT).getAsString());
					}
					regions.add(new DefaultRegion(name, subRegions, categories, weight));
				});
				ArrayList<String> authors = new ArrayList<>();
				JsonArray aut = trackObj.getAsJsonArray(MusicPackProjectExporter.JSON_TRACK_AUTHORS);
				if (aut != null) {
					aut.forEach(author -> {
						authors.add(author.getAsString());
					});
				}
				tracks.add(new DefaultTrack(trackName, title, regions, authors));
			});

		}
		return tracks;
	}

}

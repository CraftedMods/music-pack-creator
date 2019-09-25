package craftedMods.lotr.mpc.persistence.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import org.osgi.service.component.annotations.Component;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectWriter;

@Component
public class MusicPackProjectWriterImpl implements MusicPackProjectWriter {

	public static final String JSON_PROJECT = "project";
	public static final String JSON_PROJECT_NAME = "name";
	public static final String JSON_PROJECT_TRACKS = "tracks";
	public static final String JSON_PROJECT_PROPERTIES = "properties";
	public static final String JSON_TRACK_NAME = "name";
	public static final String JSON_TRACK_TITLE = "title";
	public static final String JSON_TRACK_REGIONS = "regions";
	public static final String JSON_TRACK_AUTHORS = "authors";
	public static final String JSON_REGION_NAME = "name";
	public static final String JSON_REGION_SUBREGIONS = "subregions";
	public static final String JSON_REGION_CATEGORIES = "categories";
	public static final String JSON_REGION_WEIGHT = "weight";

	@Override
	public void writeMusicPackProject(MusicPackProject project, OutputStream output) throws IOException {
		Objects.requireNonNull(project);
		Objects.requireNonNull(output);
		try (OutputStreamWriter bridge = new OutputStreamWriter(output);
				JsonWriter writer = new GsonBuilder().setPrettyPrinting().create().newJsonWriter(bridge)) {
			writer.beginObject();
			writer.name(MusicPackProjectWriterImpl.JSON_PROJECT);
			writer.beginObject();
			writer.name(MusicPackProjectWriterImpl.JSON_PROJECT_NAME).value(project.getName());
			this.writeTracks(project, writer);
			this.writeStringMap(writer, MusicPackProjectWriterImpl.JSON_PROJECT_PROPERTIES, project.getProperties());
			writer.endObject();
			writer.endObject();
			writer.flush();
		}
		output.flush();
		output.close();
	}

	private void writeTracks(MusicPackProject project, JsonWriter writer) throws IOException {
		writer.name(MusicPackProjectWriterImpl.JSON_PROJECT_TRACKS);
		writer.beginArray();
		for (Track track : project.getMusicPack().getTracks()) {
			writer.beginObject();
			writer.name(MusicPackProjectWriterImpl.JSON_TRACK_NAME).value(track.getName());
			if (track.hasTitle())
				writer.name(MusicPackProjectWriterImpl.JSON_TRACK_TITLE).value(track.getTitle());
			this.writeRegions(track, writer);
			this.writeStrinCollection(writer, MusicPackProjectWriterImpl.JSON_TRACK_AUTHORS, track.getAuthors());
			writer.endObject();
		}
		writer.endArray();
	}

	private void writeRegions(Track track, JsonWriter writer) throws IOException {
		writer.name(MusicPackProjectWriterImpl.JSON_TRACK_REGIONS);
		writer.beginArray();
		for (Region region : track.getRegions()) {
			writer.beginObject();
			writer.name(MusicPackProjectWriterImpl.JSON_REGION_NAME).value(region.getName());
			this.writeStrinCollection(writer, MusicPackProjectWriterImpl.JSON_REGION_SUBREGIONS,
					region.getSubregions());
			this.writeStrinCollection(writer, MusicPackProjectWriterImpl.JSON_REGION_CATEGORIES,
					region.getCategories());
			if (region.getWeight() != null)
				writer.name(MusicPackProjectWriterImpl.JSON_REGION_WEIGHT).value(region.getWeight());
			writer.endObject();
		}
		writer.endArray();
	}

	private void writeStrinCollection(JsonWriter writer, String name, Collection<String> strings) throws IOException {
		writer.name(name);
		writer.beginArray();
		for (String string : strings)
			writer.value(string);
		writer.endArray();
	}

	private void writeStringMap(JsonWriter writer, String name, Map<Object, Object> map) throws IOException {
		writer.name(name);
		writer.beginArray();
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			writer.beginObject();
			writer.name(entry.getKey().toString()).value(entry.getValue().toString());
			writer.endObject();
		}
		writer.endArray();
	}
}

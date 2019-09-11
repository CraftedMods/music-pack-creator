package craftedMods.lotr.mpc.core.provider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

import org.osgi.service.component.annotations.Component;

import com.google.gson.stream.JsonWriter;

import craftedMods.lotr.mpc.core.api.MusicPackProjectExporter;
import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.lotr.mpc.core.api.Track;

@Component(service = MusicPackJSONFileWriter.class)
public class MusicPackJSONFileWriter {

	public byte[] writeJSONFile(Collection<Track> tracks) throws IOException {
		byte[] ret = null;
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();
				JsonWriter writer = new JsonWriter(new OutputStreamWriter(out))) {
			writer.beginObject();
			writer.name(MusicPackProjectExporter.JSON_TRACKS);
			writer.beginArray();
			for (Track track : tracks) {
				writer.beginObject();
				writer.name(MusicPackProjectExporter.JSON_TRACK_NAME).value(track.getName());

				if (track.hasTitle()) {
					writer.name(MusicPackProjectExporter.JSON_TRACK_TITLE).value(track.getTitle());
				}

				writer.name(MusicPackProjectExporter.JSON_TRACK_REGIONS);
				writer.beginArray();
				for (Region region : track.getRegions()) {
					writer.beginObject();
					writer.name(MusicPackProjectExporter.JSON_REGION_NAME).value(region.getName());

					if (!region.getSubregions().isEmpty()) {
						writer.name(MusicPackProjectExporter.JSON_REGION_SUB);
						writer.beginArray();
						for (String sub : region.getSubregions()) {
							writer.value(sub);
						}
						writer.endArray();
					}

					if (!region.getCategories().isEmpty()) {
						writer.name(MusicPackProjectExporter.JSON_REGION_CATEGORIES);
						writer.beginArray();
						for (String category : region.getCategories()) {
							writer.value(category);
						}
						writer.endArray();
					}

					if (region.getWeight() != null) {
						writer.name(MusicPackProjectExporter.JSON_REGION_WEIGHT).value(region.getWeight());
					}
					writer.endObject();
				}
				writer.endArray();

				if (!track.getAuthors().isEmpty()) {
					writer.name(MusicPackProjectExporter.JSON_TRACK_AUTHORS);
					writer.beginArray();
					for (String author : track.getAuthors()) {
						writer.value(author);
					}
					writer.endArray();
				}
				writer.endObject();
			}
			writer.endArray();
			writer.endObject();
			writer.flush();

			ret = out.toByteArray();
		}
		return ret;
	}

}

package craftedMods.lotr.mpc.core.api;

import java.nio.file.Path;
import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Track {

	public Path getTrackPath();

	public void setTrackPath(Path newTrackPath);

	public boolean hasTitle();

	public String getTitle();

	public void setTitle(String title);

	public List<Region> getRegions();

	public List<String> getAuthors();

}

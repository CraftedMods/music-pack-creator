package craftedMods.lotr.mpc.core.api;

import java.util.List;
import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Track {

	public String getName();

	public void setName(String name);

	public boolean hasTitle();

	public String getTitle();

	public void setTitle(String title);

	public List<Region> getRegions();

	public Set<String> getAuthors();

}

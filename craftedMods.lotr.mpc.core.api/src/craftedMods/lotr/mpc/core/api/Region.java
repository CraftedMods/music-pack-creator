package craftedMods.lotr.mpc.core.api;

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Region {

	public String getName();

	public void setName(String name);

	public List<String> getSubregions();

	public List<String> getCategories();

	public Float getWeight();

}

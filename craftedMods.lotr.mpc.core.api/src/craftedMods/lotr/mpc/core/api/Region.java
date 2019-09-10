package craftedMods.lotr.mpc.core.api;

import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Region {

	public String getName();

	public void setName(String name);

	public Set<String> getSubregions();

	public Set<String> getCategories();

	public Float getWeight();
	
	public void setWeight(Float weight);

}

package craftedMods.lotr.mpc.core.api;

import craftedMods.utils.data.NonNullSet;

public interface ReadOnlyRegion {
	
	public String getName();

	public NonNullSet<String> getSubregions();

	public NonNullSet<String> getCategories();

	public Float getWeight();
	
}

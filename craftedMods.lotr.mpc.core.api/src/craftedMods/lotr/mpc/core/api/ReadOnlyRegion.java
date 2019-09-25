package craftedMods.lotr.mpc.core.api;

import craftedMods.utils.data.NonNullSet;
import craftedMods.utils.lang.CloneableObject;

public interface ReadOnlyRegion extends CloneableObject<ReadOnlyRegion>{
	
	public String getName();

	public NonNullSet<String> getSubregions();

	public NonNullSet<String> getCategories();

	public Float getWeight();
	
}

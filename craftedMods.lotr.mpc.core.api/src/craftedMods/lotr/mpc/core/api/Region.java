package craftedMods.lotr.mpc.core.api;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface Region extends ReadOnlyRegion {

	public void setName(String name);

	public void setWeight(Float weight);

}

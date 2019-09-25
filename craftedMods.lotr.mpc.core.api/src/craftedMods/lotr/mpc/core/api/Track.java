package craftedMods.lotr.mpc.core.api;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.utils.data.NonNullSet;

@ProviderType
public interface Track extends ReadOnlyTrack{

	public void setName(String name);

	public void setTitle(String title);

	@Override
	public NonNullSet<Region> getRegions();
	
	@Override
	public Track clone();
	
}

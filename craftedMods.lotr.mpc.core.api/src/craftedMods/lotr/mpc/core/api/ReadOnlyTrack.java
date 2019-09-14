package craftedMods.lotr.mpc.core.api;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.utils.data.NonNullSet;

@ProviderType
public interface ReadOnlyTrack {
	
	public String getName();

	public boolean hasTitle();

	public String getTitle();

	public NonNullSet<? extends ReadOnlyRegion> getRegions();

	public NonNullSet<String> getAuthors();

}

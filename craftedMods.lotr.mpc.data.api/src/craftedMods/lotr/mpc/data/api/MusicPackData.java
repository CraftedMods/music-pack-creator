package craftedMods.lotr.mpc.data.api;

import java.util.Collection;
import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface MusicPackData {

	public String getLOTRModVersion();

	public Map<String, Collection<String>> getRegions();
	
	public String getDefaultRegion();

	public Collection<String> getCategories();

}

package craftedMods.preferences.api;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.utils.data.PrimitiveProperties;

@ProviderType
public interface Preferences extends PrimitiveProperties {

	public String getPID();

	public void sync();

	public void flush();

}

package craftedMods.preferences.api;

import java.io.IOException;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.utils.data.PrimitiveProperties;

@ProviderType
public interface Preferences extends PrimitiveProperties {

	public String getPID();

	public void sync() throws IOException;

	public void flush() throws IOException;

}

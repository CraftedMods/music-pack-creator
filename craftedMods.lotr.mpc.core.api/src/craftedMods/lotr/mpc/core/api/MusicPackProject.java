package craftedMods.lotr.mpc.core.api;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.utils.data.PrimitiveProperties;

@ProviderType
public interface MusicPackProject {

	public static final String PROPERTY_MPC_VERSION = "mpcVersion";
	public static final String PROPERTY_LOTR_VERSION = "lotrVersion";
	public static final String PROPERTY_AUTHOR = "author";
	public static final String PROPERTY_PACK_VERSION = "packVersion";

	public String getName();

	public MusicPack getMusicPack();

	public PrimitiveProperties getProperties();

}

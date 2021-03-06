package craftedMods.lotr.mpc.core.provider;

import craftedMods.lotr.mpc.core.api.MusicPack;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.core.base.DefaultMusicPack;
import craftedMods.utils.data.ExtendedProperties;
import craftedMods.utils.data.PrimitiveProperties;

public class MusicPackProjectImpl implements MusicPackProject {

	private String name;
	private final MusicPack musicPack = new DefaultMusicPack();
	private final PrimitiveProperties properties = new ExtendedProperties();

	public MusicPackProjectImpl(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	@Override
	public MusicPack getMusicPack() {
		return this.musicPack;
	}

	@Override
	public PrimitiveProperties getProperties() {
		return this.properties;
	}

	@Override
	public String toString() {
		return name;
	}

}

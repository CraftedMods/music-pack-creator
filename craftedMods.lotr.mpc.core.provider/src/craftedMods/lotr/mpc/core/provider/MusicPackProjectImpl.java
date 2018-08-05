package craftedMods.lotr.mpc.core.provider;

import craftedMods.lotr.mpc.core.api.*;
import craftedMods.utils.data.PrimitiveProperties;

public class MusicPackProjectImpl implements MusicPackProject {

	private String name;
	private final MusicPack musicPack = new MusicPackImpl();
	private final PrimitiveProperties properties = new PrimitiveProperties();

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

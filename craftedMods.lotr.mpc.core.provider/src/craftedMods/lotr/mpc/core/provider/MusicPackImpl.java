package craftedMods.lotr.mpc.core.provider;

import java.util.*;

import craftedMods.lotr.mpc.core.api.*;

public class MusicPackImpl implements MusicPack {

	private List<Track> tracksList = new ArrayList<>();

	@Override
	public List<Track> getTracks() {
		return this.tracksList;
	}

}

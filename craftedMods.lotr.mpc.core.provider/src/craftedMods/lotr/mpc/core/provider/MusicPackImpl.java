package craftedMods.lotr.mpc.core.provider;

import java.util.*;

import craftedMods.lotr.mpc.core.api.*;

public class MusicPackImpl implements MusicPack {

	private Set<Track> tracksSet = new HashSet<>();

	@Override
	public Set<Track> getTracks() {
		return this.tracksSet;
	}

}

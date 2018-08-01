package craftedMods.lotrTools.musicPackCreator.data;

import java.io.Serializable;
import java.util.*;

public class MusicPack implements Serializable {

	private static final long serialVersionUID = 1L;

	private ArrayList<Track> tracks = new ArrayList<>();

	public MusicPack() {}

	public MusicPack(ArrayList<Track> tracks) {
		this.tracks = tracks;
	}

	public List<Track> getTracks() {
		return this.tracks;
	}

}

package craftedMods.lotr.mpc.core.base;

import java.nio.file.Path;
import java.util.*;

import craftedMods.lotr.mpc.core.api.*;

public class DefaultTrack implements Track {

	private Path trackPath;
	private String title;
	private List<Region> regions = new ArrayList<>();
	private List<String> authors = new ArrayList<>();

	public DefaultTrack(Path trackPath, String title, List<Region> regions, List<String> authors) {
		this.trackPath = trackPath;
		this.title = title;
		this.regions = regions;
		this.authors = authors;
	}

	@Override
	public Path getTrackPath() {
		return this.trackPath;
	}

	@Override
	public void setTrackPath(Path newTrackPath) {
		this.trackPath = newTrackPath;
	}

	@Override
	public boolean hasTitle() {
		return this.title != null;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public List<Region> getRegions() {
		return this.regions;
	}

	@Override
	public List<String> getAuthors() {
		return this.authors;
	}

	@Override
	public String toString() {
		return title != null ? title : trackPath.getFileName().toString();
	}

}

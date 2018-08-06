package craftedMods.lotr.mpc.core.base;

import java.nio.file.Path;
import java.util.*;

import craftedMods.lotr.mpc.core.api.*;

public class DefaultTrack implements Track {

	private Path trackPath;
	private String title;
	private List<Region> regions = new ArrayList<>();
	private List<String> authors = new ArrayList<>();

	public DefaultTrack() {
	}

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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authors == null) ? 0 : authors.hashCode());
		result = prime * result + ((regions == null) ? 0 : regions.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((trackPath == null) ? 0 : trackPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultTrack other = (DefaultTrack) obj;
		if (authors == null) {
			if (other.authors != null)
				return false;
		} else if (!authors.equals(other.authors))
			return false;
		if (regions == null) {
			if (other.regions != null)
				return false;
		} else if (!regions.equals(other.regions))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (trackPath == null) {
			if (other.trackPath != null)
				return false;
		} else if (!trackPath.equals(other.trackPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.title != null ? this.title : this.trackPath.getFileName().toString();
	}

}

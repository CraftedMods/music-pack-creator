package craftedMods.lotr.mpc.core.base;

import java.util.ArrayList;
import java.util.List;

import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.lotr.mpc.core.api.Track;

public class DefaultTrack implements Track {

	private String name;
	private String title;
	private List<Region> regions = new ArrayList<>();
	private List<String> authors = new ArrayList<>();

	public DefaultTrack() {
	}

	public DefaultTrack(String name, String title, List<Region> regions, List<String> authors) {
		this.name = name;
		this.title = title;
		this.regions = regions;
		this.authors = authors;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
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
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((regions == null) ? 0 : regions.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
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
		return true;
	}

	@Override
	public String toString() {
		return this.title != null ? this.title : this.name;
	}

}

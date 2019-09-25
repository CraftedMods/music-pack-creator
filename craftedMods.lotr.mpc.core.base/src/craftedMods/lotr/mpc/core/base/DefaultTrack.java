package craftedMods.lotr.mpc.core.base;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.lotr.mpc.core.api.Track;
import craftedMods.utils.data.CollectionUtils;
import craftedMods.utils.data.NonNullSet;

public class DefaultTrack implements Track {

	private String name;
	private String title;
	private NonNullSet<Region> regions = CollectionUtils.createNonNullHashSet();
	private NonNullSet<String> authors = CollectionUtils.createNonNullHashSet();

	public DefaultTrack(String name) {
		this.setName(name);
	}

	public DefaultTrack(String name, String title, Collection<Region> regions, Collection<String> authors) {
		this.setName(name);
		this.title = title;
		this.regions.addAll(regions);
		this.authors.addAll(authors);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		Objects.requireNonNull(name);

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
	public NonNullSet<Region> getRegions() {
		return this.regions;
	}

	@Override
	public NonNullSet<String> getAuthors() {
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
	public DefaultTrack clone() {
		return new DefaultTrack(this.name, this.title,
				this.regions.stream().map(region -> region.clone()).collect(Collectors.toList()), this.authors);
	}

	@Override
	public String toString() {
		return this.title != null ? this.title : this.name;
	}

}

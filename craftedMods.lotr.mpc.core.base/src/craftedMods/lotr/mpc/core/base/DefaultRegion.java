package craftedMods.lotr.mpc.core.base;

import java.util.Collection;
import java.util.Objects;

import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.utils.data.CollectionUtils;
import craftedMods.utils.data.NonNullSet;

public class DefaultRegion implements Region {

	private String name;
	private NonNullSet<String> subregions = CollectionUtils.createNonNullHashSet();
	private NonNullSet<String> categories = CollectionUtils.createNonNullHashSet();
	private Float weight;

	public DefaultRegion() {
	}

	public DefaultRegion(String name, Collection<String> subregions, Collection<String> categories, Float weight) {
		Objects.requireNonNull(name);
		
		this.name = name;
		this.subregions.addAll(subregions);
		this.categories.addAll(categories);
		this.weight = weight;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		Objects.requireNonNull(name);
		
		this.name = name;
	}

	@Override
	public NonNullSet<String> getSubregions() {
		return this.subregions;
	}

	@Override
	public NonNullSet<String> getCategories() {
		return this.categories;
	}

	@Override
	public Float getWeight() {
		return this.weight;
	}

	@Override
	public void setWeight(Float weight) {
		this.weight = weight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.categories == null ? 0 : this.categories.hashCode());
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
		result = prime * result + (this.subregions == null ? 0 : this.subregions.hashCode());
		result = prime * result + (this.weight == null ? 0 : this.weight.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		DefaultRegion other = (DefaultRegion) obj;
		if (this.categories == null) {
			if (other.categories != null)
				return false;
		} else if (!this.categories.equals(other.categories))
			return false;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		if (this.subregions == null) {
			if (other.subregions != null)
				return false;
		} else if (!this.subregions.equals(other.subregions))
			return false;
		if (this.weight == null) {
			if (other.weight != null)
				return false;
		} else if (!this.weight.equals(other.weight))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.name;
	}

}

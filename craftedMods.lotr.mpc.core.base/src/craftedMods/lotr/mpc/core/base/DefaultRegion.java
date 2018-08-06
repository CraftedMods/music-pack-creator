package craftedMods.lotr.mpc.core.base;

import java.util.*;

import craftedMods.lotr.mpc.core.api.Region;

public class DefaultRegion implements Region {

	private String name;
	private List<String> subregions = new ArrayList<>();
	private List<String> categories = new ArrayList<>();
	private Float weight;

	public DefaultRegion(String name, List<String> subregions, List<String> categories, Float weight) {
		this.name = name;
		this.subregions = subregions;
		this.categories = categories;
		this.weight = weight;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public List<String> getSubregions() {
		return this.subregions;
	}

	@Override
	public List<String> getCategories() {
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

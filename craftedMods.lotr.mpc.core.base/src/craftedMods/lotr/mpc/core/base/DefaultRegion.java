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
	public String toString() {
		return this.name;
	}

}

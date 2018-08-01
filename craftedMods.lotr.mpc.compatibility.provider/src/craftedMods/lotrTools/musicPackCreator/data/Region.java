package craftedMods.lotrTools.musicPackCreator.data;

import java.io.Serializable;
import java.util.*;

public class Region implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;

	private String name;
	private ArrayList<String> subregions = new ArrayList<>();
	private ArrayList<String> categories = new ArrayList<>();
	private Float weight;

	public Region() {}

	public Region(String name, ArrayList<String> subregions, ArrayList<String> categories, Float weight) {
		this.name = name;
		this.subregions = subregions;
		this.categories = categories;
		this.weight = weight;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getSubregions() {
		return this.subregions;
	}

	public List<String> getCategories() {
		return this.categories;
	}

	public Float getWeight() {
		return this.weight;
	}

	public void setWeight(Float weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return this.name;
	}
}

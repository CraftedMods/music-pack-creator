package craftedMods.lotrTools.musicPackCreator.data;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Track implements Serializable {

	private static final long serialVersionUID = 1;

	private transient Path trackPath;
	private String title;
	private ArrayList<Region> regions = new ArrayList<>();
	private ArrayList<String> authors = new ArrayList<>();

	public Track() {}

	public Track(Path path, String title, ArrayList<Region> regions, ArrayList<String> authors) {
		this.trackPath = path;
		this.title = title;
		this.regions = regions;
		this.authors = authors;
	}

	public Path getTrackPath() {
		return this.trackPath;
	}

	public void setTrackPath(Path trackPath) {
		this.trackPath = trackPath;
	}

	public String getFilename() {
		if (this.trackPath == null) return null;
		return this.trackPath.getFileName().toString();
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Region> getRegions() {
		return this.regions;
	}

	public List<String> getAuthors() {
		return this.authors;
	}

	@Override
	public String toString() {
		return this.title != null ? this.title : this.getFilename();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeUTF(this.trackPath.toString());
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.trackPath = Paths.get(in.readUTF());
	}

}

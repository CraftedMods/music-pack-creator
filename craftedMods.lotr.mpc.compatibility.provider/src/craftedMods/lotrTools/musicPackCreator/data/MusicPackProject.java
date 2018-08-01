package craftedMods.lotrTools.musicPackCreator.data;

import java.io.*;
import java.nio.file.*;

public class MusicPackProject implements Serializable {

	private static final long serialVersionUID = 2865985899722576282L;

	public static final String PACK_VERSION_KEY = "mpcVersion";
	public static final String PACK_TRACKS_KEY = "trackCount";

	private String name;
	private transient Path projectPath;
	private MusicPack musicPack;

	@Deprecated
	public MusicPackProject(String name, Path projectPath) {
		this.name = name;
		this.projectPath = projectPath;
		this.musicPack = new MusicPack();
	}

	public String getName() {
		return this.name;
	}

	@Deprecated
	public void setName(String name) {
		this.name = name;
	}

	public Path getProjectPath() {
		return this.projectPath;
	}

	public MusicPack getMusicPack() {
		return this.musicPack;
	}

	@Override
	public String toString() {
		return this.name;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		out.writeUTF(this.projectPath.toString());
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.projectPath = Paths.get(in.readUTF());
	}

}

package craftedMods.lotr.mpc.compatibility.provider;

import java.io.*;
import java.nio.file.*;

import craftedMods.lotrTools.musicPackCreator.data.MusicPackProject;

class OldMusicPackProjectReader {

	private final Path path;
	private String version;

	public OldMusicPackProjectReader(Path path) {
		this.path = path;
	}

	public MusicPackProject read() throws IOException, ClassNotFoundException {
		try (ObjectInputStream in = new ObjectInputStream(
				Files.newInputStream(Paths.get(this.path.toString(), SerializedWorkspaceToJSONConverter.OLD_PROJECT_FILE)))) {
			this.version = (String) in.readObject();
			return (MusicPackProject) in.readObject();
		}
	}

	public String getVersion() {
		return this.version;
	}

}

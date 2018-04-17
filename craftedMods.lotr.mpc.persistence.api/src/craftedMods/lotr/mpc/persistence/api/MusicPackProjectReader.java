package craftedMods.lotr.mpc.persistence.api;

import java.io.IOException;
import java.nio.file.Path;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.lotr.mpc.core.api.MusicPackProject;

@ProviderType
public interface MusicPackProjectReader {
	
	/**
	 * Reads a Music Pack Project from any location
	 * 
	 * @param projectPath
	 *            The location
	 * @return The Music Pack Project
	 * @throws IOException
	 *             If the Music Pack Project couldn't be loaded due IO issues
	 */
	public MusicPackProject readMusicPackProject(Path projectPath) throws IOException;

}

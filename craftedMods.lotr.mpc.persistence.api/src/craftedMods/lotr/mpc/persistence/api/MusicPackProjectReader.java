package craftedMods.lotr.mpc.persistence.api;

import java.io.*;

import org.osgi.annotation.versioning.ProviderType;
import craftedMods.lotr.mpc.core.api.MusicPackProject;

@ProviderType
public interface MusicPackProjectReader {

	/**
	 * Reads a Music Pack Project from any location. The supplied stream will be closed.
	 * 
	 * @param projectData
	 *            The read Music Pack Project data
	 * @return The Music Pack Project
	 * @throws IOException
	 *             If the Music Pack Project couldn't be loaded due IO issues
	 */
	public MusicPackProject readMusicPackProject(InputStream projectData) throws IOException;

}

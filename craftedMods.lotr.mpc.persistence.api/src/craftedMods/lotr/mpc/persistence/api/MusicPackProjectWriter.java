package craftedMods.lotr.mpc.persistence.api;

import java.io.*;

import org.osgi.annotation.versioning.ProviderType;
import craftedMods.lotr.mpc.core.api.MusicPackProject;

@ProviderType
public interface MusicPackProjectWriter {

	/**
	 * Writes the Music Pack Project to any location. The supplied stream will be closed.
	 * 
	 * @param project
	 *            The Music Pack Project
	 * @param output
	 *            The stream to which the Music Pack Project data will be written
	 * @throws IOException
	 *             If the Music Pack Project couldn't be saved due IO issues
	 */
	public void writeMusicPackProject(MusicPackProject project, OutputStream output) throws IOException;

}

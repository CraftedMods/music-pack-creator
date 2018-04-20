package craftedMods.lotr.mpc.persistence.api;

import java.io.IOException;
import java.nio.file.Path;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.lotr.mpc.core.api.MusicPackProject;

@ProviderType
public interface MusicPackProjectWriter {

	/**
	 * Writes the Music Pack Project to any location
	 * 
	 * @param project
	 *            The Music Pack Project
	 * @param location
	 *            The location
	 * @throws IOException
	 *             If the Music Pack Project couldn't be saved due IO issues
	 */
	public void writeMusicPackProject(MusicPackProject project, Path location) throws IOException;

}

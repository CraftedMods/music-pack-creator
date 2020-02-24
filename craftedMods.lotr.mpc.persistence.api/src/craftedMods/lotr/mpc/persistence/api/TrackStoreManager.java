package craftedMods.lotr.mpc.persistence.api;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.lotr.mpc.core.api.MusicPackProject;

@ProviderType
public interface TrackStoreManager {

	/**
	 * Gets or creates the track store for the specified, managed Music Pack Project
	 * 
	 * @param project The managed Music Pack Project
	 * @return The track store
	 */
	public TrackStore getTrackStore(MusicPackProject project);

	/**
	 * Deletes the track store for the specified, managed Music Pack Project
	 * 
	 * @param project The managed Music Pack Project
	 */
	public void deleteTrackStore(MusicPackProject project);

}

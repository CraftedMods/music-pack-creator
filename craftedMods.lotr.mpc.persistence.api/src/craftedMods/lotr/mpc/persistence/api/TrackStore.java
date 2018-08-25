package craftedMods.lotr.mpc.persistence.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.osgi.annotation.versioning.ProviderType;

import craftedMods.lotr.mpc.core.api.MusicPackProject;

@ProviderType
public interface TrackStore {

	public MusicPackProject getMusicPackProject();

	public Collection<String> getStoredTracks();

	public void refresh() throws IOException;

	public InputStream openInputStream(String name) throws IOException;

	public OutputStream openOutputStream(String name) throws IOException;

	public void deleteTrack(String name) throws IOException;

}

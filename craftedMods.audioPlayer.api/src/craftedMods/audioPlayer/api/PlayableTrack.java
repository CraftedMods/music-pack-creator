package craftedMods.audioPlayer.api;

import java.io.IOException;
import java.io.InputStream;

import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface PlayableTrack {

	public String getName();

	public InputStream openInputStream(String playingMode) throws IOException;

}

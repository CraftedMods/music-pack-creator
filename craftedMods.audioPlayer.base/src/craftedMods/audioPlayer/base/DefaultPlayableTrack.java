package craftedMods.audioPlayer.base;

import java.io.IOException;
import java.io.InputStream;

import craftedMods.audioPlayer.api.PlayableTrack;
import craftedMods.utils.function.FailableSupplier;

public class DefaultPlayableTrack implements PlayableTrack {

	private final String name;
	private final FailableSupplier<InputStream, IOException> trackStreamCreator;

	public DefaultPlayableTrack(String name, FailableSupplier<InputStream, IOException> trackStreamCreator) {
		this.name = name;
		this.trackStreamCreator = trackStreamCreator;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		return trackStreamCreator.get();
	}

}

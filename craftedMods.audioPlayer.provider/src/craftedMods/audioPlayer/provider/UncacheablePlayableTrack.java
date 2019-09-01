package craftedMods.audioPlayer.provider;

import java.io.IOException;
import java.io.InputStream;

import craftedMods.audioPlayer.base.DefaultPlayableTrack;
import craftedMods.utils.function.FailableSupplier;

public class UncacheablePlayableTrack extends DefaultPlayableTrack {

	public UncacheablePlayableTrack(String name, FailableSupplier<InputStream, IOException> trackStreamCreator) {
		super(name, trackStreamCreator);
	}

}

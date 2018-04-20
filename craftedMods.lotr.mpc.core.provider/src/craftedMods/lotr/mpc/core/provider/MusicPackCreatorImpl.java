package craftedMods.lotr.mpc.core.provider;

import java.nio.file.*;

import org.osgi.service.component.annotations.Component;

import craftedMods.lotr.mpc.core.api.MusicPackCreator;

@Component
public class MusicPackCreatorImpl implements MusicPackCreator {

	@Override
	public String getVersion() {
		return "Beta 5.0";
	}

	@Override
	public boolean isPreRelease() {
		return false;
	}

	@Override
	public Path getWorkspaceRoot() {
		return Paths.get(".\\LOTRMusicPackCreator");
	}

}

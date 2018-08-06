package craftedMods.lotr.mpc.core.provider;

import org.osgi.service.component.annotations.Component;

import craftedMods.lotr.mpc.core.api.MusicPackCreator;
import craftedMods.versionChecker.api.SemanticVersion;
import craftedMods.versionChecker.base.DefaultSemanticVersion;

@Component
public class MusicPackCreatorImpl implements MusicPackCreator {

	private static final SemanticVersion VERSION = DefaultSemanticVersion.of("5.0.0-BETA");

	@Override
	public SemanticVersion getVersion() {
		return VERSION;
	}

}

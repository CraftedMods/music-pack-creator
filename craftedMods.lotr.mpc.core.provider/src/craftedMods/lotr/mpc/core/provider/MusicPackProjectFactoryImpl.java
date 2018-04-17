package craftedMods.lotr.mpc.core.provider;

import org.osgi.service.component.annotations.Component;

import craftedMods.lotr.mpc.core.api.*;

@Component
public class MusicPackProjectFactoryImpl implements MusicPackProjectFactory {

	@Override
	public MusicPackProject createMusicPackProjectInstance(String name) {
		return new MusicPackProjectImpl(name);
	}

}

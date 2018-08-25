package craftedMods.lotr.mpc.persistence.provider;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import craftedMods.lotr.mpc.core.api.MusicPackProject;

@Component(service = MusicPackProjectManager.class)
public class MusicPackProjectManager {

	private Map<MusicPackProject, Path> managedMusicPackProjects;

	@Activate
	public void onActivate() {
		managedMusicPackProjects = new HashMap<>();
	}

	@Deactivate
	public void onDeactivate() {
		managedMusicPackProjects.clear();
	}

	public Map<MusicPackProject, Path> getManagedMusicPackProjects() {
		return managedMusicPackProjects;
	}

}

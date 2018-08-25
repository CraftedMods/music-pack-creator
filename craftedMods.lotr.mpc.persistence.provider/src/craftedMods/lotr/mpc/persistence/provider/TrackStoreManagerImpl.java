package craftedMods.lotr.mpc.persistence.provider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.osgi.framework.ServiceException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.persistence.api.TrackStore;
import craftedMods.lotr.mpc.persistence.api.TrackStoreManager;

@Component
public class TrackStoreManagerImpl implements TrackStoreManager {

	@Reference
	private FileManager fileManager;

	@Reference
	private MusicPackProjectManager musicPackProjectManager;

	private Map<MusicPackProject, TrackStore> trackStores;

	@Activate
	public void onActivate() {
		this.trackStores = new HashMap<>();
	}

	@Deactivate
	public void onDeactivate() {
		this.trackStores.clear();
	}

	@Override
	public TrackStore getTrackStore(MusicPackProject project) {
		Objects.requireNonNull(project);
		if (!musicPackProjectManager.getManagedMusicPackProjects().containsKey(project))
			throw new IllegalArgumentException(
					String.format("The Music Pack Project \"%s\" is not managed", project.getName()));
		if (!trackStores.containsKey(project)) {
			try {
				TrackStore store = new TrackStoreImpl(project,
						this.fileManager.getPathAndCreateDir(
								musicPackProjectManager.getManagedMusicPackProjects().get(project).toString(),
								"tracks"),
						fileManager);
				this.trackStores.put(project, store);
				store.refresh();
			} catch (IOException e) {
				throw new ServiceException(
						String.format("Couldn't create the track store directory for the Music Pack Project \"%s\": ",
								project.getName()),
						e);
			}
		}
		return trackStores.get(project);
	}

	@Override
	public void deleteTrackStore(MusicPackProject project) {
		Objects.requireNonNull(project);
		if (!musicPackProjectManager.getManagedMusicPackProjects().containsKey(project))
			throw new IllegalArgumentException(
					String.format("The Music Pack Project \"%s\" is not managed", project.getName()));
		this.trackStores.remove(project);
	}

	Map<MusicPackProject, TrackStore> getTrackStores() {
		return trackStores;
	}

}

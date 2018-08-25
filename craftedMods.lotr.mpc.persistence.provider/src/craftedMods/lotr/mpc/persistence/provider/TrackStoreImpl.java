package craftedMods.lotr.mpc.persistence.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.persistence.api.TrackStore;

public class TrackStoreImpl implements TrackStore {

	private final MusicPackProject project;
	private final Path storeDir;
	private final FileManager fileManager;

	private final Map<String, Path> storedTracks = new HashMap<>();

	public TrackStoreImpl(MusicPackProject project, Path storeDir, FileManager fileManager) {
		this.project = project;
		this.storeDir = storeDir;
		this.fileManager = fileManager;

	}

	@Override
	public MusicPackProject getMusicPackProject() {
		return project;
	}

	@Override
	public void refresh() throws IOException {
		storedTracks.clear();
		try (Stream<Path> tracks = fileManager.getPathsInDirectory(storeDir)) {
			tracks.forEach(path -> {
				storedTracks.put(path.getFileName().toString(), path);
			});
		}
	}

	public Map<String, Path> getStoredTracksMap() {
		return storedTracks;
	}

	@Override
	public Collection<String> getStoredTracks() {
		return Collections.unmodifiableCollection(storedTracks.keySet());
	}

	@Override
	public InputStream openInputStream(String name) throws IOException {
		this.requireRegisteredTrack(name);
		return fileManager.newInputStream(storedTracks.get(name));
	}

	@Override
	public OutputStream openOutputStream(String name) throws IOException {
		Objects.requireNonNull(name);
		if (!this.storedTracks.containsKey(name)) {
			this.storedTracks.put(name, fileManager.getPathAndCreateFile(storeDir.toString(), name));
		}
		return fileManager.newOutputStream(this.storedTracks.get(name));
	}

	@Override
	public void deleteTrack(String name) throws IOException {
		this.requireRegisteredTrack(name);
		fileManager.deleteFile(storedTracks.remove(name));
	}

	private void requireRegisteredTrack(String name) {
		Objects.requireNonNull(name);
		if (!storedTracks.containsKey(name))
			throw new IllegalArgumentException(String.format("The track \"%s\" isn't registered", name));
	}

}

package craftedMods.lotr.mpc.compatibility.provider;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.ServiceException;

import craftedMods.fileManager.api.FileManager;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.core.api.MusicPackProjectFactory;
import craftedMods.lotr.mpc.core.api.Region;
import craftedMods.lotr.mpc.core.base.DefaultRegion;
import craftedMods.lotr.mpc.core.base.DefaultTrack;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectWriter;
import craftedMods.versionChecker.api.SemanticVersion;

public class SerializedWorkspaceToJSONConverter {

	public static final String OLD_PROJECT_FILE = "project.lmpp";
	public static final String NEW_PROJECT_FILE = "project.json";

	private SemanticVersion mpcVersion;

	private MusicPackProjectFactory factory;

	private MusicPackProjectWriter writer;

	private FileManager fileManager;

	public void onActivate(SemanticVersion mpcVersion, MusicPackProjectFactory factory, MusicPackProjectWriter writer,
			FileManager fileManager) {
		this.mpcVersion = mpcVersion;
		this.factory = factory;
		this.writer = writer;
		this.fileManager = fileManager;
	}

	public void convertWorkspace(Path workspacePath) {
		OldMusicPackProjectReader reader = new OldMusicPackProjectReader(workspacePath);
		craftedMods.lotrTools.musicPackCreator.data.MusicPackProject oldProject = this.readOldProject(workspacePath,
				reader);
		String version = reader.getVersion();
		MusicPackProject newProject = this.factory.createMusicPackProjectInstance(oldProject.getName());
		for (craftedMods.lotrTools.musicPackCreator.data.Track oldTrack : oldProject.getMusicPack().getTracks())
			newProject.getMusicPack().getTracks().add(new DefaultTrack(oldTrack.getTrackPath(), oldTrack.getTitle(),
					this.getNewRegions(oldTrack.getRegions()), oldTrack.getAuthors()));
		newProject.getProperties().setString(MusicPackProject.PROPERTY_MPC_VERSION,
				version == null ? mpcVersion.toString() : version);
		this.saveNewMusicPackProject(workspacePath, newProject);
		this.deleteOldProjectFile(workspacePath);
	}

	private craftedMods.lotrTools.musicPackCreator.data.MusicPackProject readOldProject(Path workspacePath,
			OldMusicPackProjectReader reader) {
		try {
			return reader.read();
		} catch (ClassNotFoundException | IOException e) {
			throw new ServiceException("Couldn't load the serialized Music Pack Project: ", e);
		}
	}

	private void saveNewMusicPackProject(Path workspacePath, MusicPackProject project) {
		Path filePath = workspacePath.resolve(NEW_PROJECT_FILE);
		try {
			fileManager.createFile(filePath);
			this.writer.writeMusicPackProject(project, fileManager.newOutputStream(filePath));
		} catch (IOException e) {
			throw new ServiceException(String.format("Couldn't write the converted Music Pack Project \"%s\" to \"%s\"",
					project.getName(), filePath.toString()), e);
		}
	}

	private void deleteOldProjectFile(Path workspacePath) {
		Path serializedProjectFile = Paths.get(workspacePath.toString(),
				SerializedWorkspaceToJSONConverter.OLD_PROJECT_FILE);
		try {
			fileManager.deleteFile(serializedProjectFile);
		} catch (IOException e) {
			throw new ServiceException(
					String.format("Couldn't delete the old project file at \"%s\"", serializedProjectFile.toString()),
					e);
		}
	}

	private List<Region> getNewRegions(List<craftedMods.lotrTools.musicPackCreator.data.Region> oldRegions) {
		List<Region> regions = new ArrayList<>();
		for (craftedMods.lotrTools.musicPackCreator.data.Region oldRegion : oldRegions)
			regions.add(new DefaultRegion(oldRegion.getName(), oldRegion.getSubregions(), oldRegion.getCategories(),
					oldRegion.getWeight()));
		return regions;
	}

}

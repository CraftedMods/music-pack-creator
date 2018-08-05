package craftedMods.lotr.mpc.core.provider;

import java.util.*;

import org.osgi.service.component.annotations.*;
import org.osgi.service.log.LogService;

import craftedMods.eventManager.api.*;
import craftedMods.eventManager.base.DefaultWriteableEventProperties;
import craftedMods.lotr.mpc.core.api.*;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectPersistenceManager;
import craftedMods.utils.exceptions.InvalidInputException;

@Component
public class MusicPackProjectManagerImpl implements MusicPackProjectManager {

	@Reference
	private MusicPackCreator musicPackCreator;

	@Reference
	private LogService logger;

	@Reference
	private EventManager eventManager;

	@Reference
	private MusicPackProjectPersistenceManager persistenceManager;

	@Reference
	private MusicPackProjectFactory mppFactory;

	private List<MusicPackProjectImpl> musicPackProjects;

	@Activate
	public void onActivate() {
		this.musicPackProjects = new ArrayList<>();
		for (MusicPackProject loadedProject : this.persistenceManager.loadMusicPackProjects())
			try {
				this.registerMusicPackProject(loadedProject);
			} catch (Exception e) {
				this.logger.log(LogService.LOG_ERROR,
						String.format("The Music Pack Project \"%s\" couldn't be registered during loading",
								loadedProject.getName()));
				WriteableEventProperties properties = new DefaultWriteableEventProperties();
				properties.put(MusicPackProjectManager.LOAD_ALL_REGISTER_PROJECT_ERROR_EVENT_EXCEPTION, e);
				this.eventManager.dispatchEvent(MusicPackProjectManager.LOAD_ALL_REGISTER_PROJECT_ERROR_EVENT,
						properties);
			}
	}

	@Deactivate
	public void onDeactivate() {
		this.saveAllMusicPackProjects();
	}

	@Override
	public MusicPackProject registerMusicPackProject(MusicPackProject suggestedInstance) throws InvalidInputException {
		this.validateUnregisteredProject(suggestedInstance);
		this.validateMPPName(suggestedInstance.getName());
		MusicPackProjectImpl impl = (MusicPackProjectImpl) suggestedInstance;
		impl.setName(suggestedInstance.getName().trim());
		this.musicPackProjects.add(impl);
		this.logger.log(LogService.LOG_INFO, String.format("Registered the Music Pack Project \"%s\"", impl.getName()));
		return suggestedInstance;
	}

	/**
	 * Creates an unique Music Pack Project name based on a suggested one. If the
	 * suggested name is empty, a default name will be used. This function is
	 * intended for the automatic creation of Music Pack Project names when the user
	 * cannot provide a name.
	 * 
	 * @param suggestedName The suggested name
	 * @return A currently unique name based on the suggested one
	 */
	@Override
	public String getUnusedMusicPackProjectName(String suggestedName) {
		Objects.requireNonNull(suggestedName);
		String startName = suggestedName.trim().isEmpty() ? "MusicPackProject" : suggestedName.trim();
		String projectName = startName;
		for (int i = 0; this.existsMPPName(projectName = startName + (i == 0 ? "" : "_" + i)); i++) {
		}
		return projectName.trim();
	}

	private boolean existsMPPName(String name) {
		return this.existsMPPName(name, null);
	}

	private boolean existsMPPName(String name, MusicPackProject ignore) {
		for (MusicPackProject project : this.musicPackProjects)
			if ((ignore != null ? project != ignore : true) && project.getName().equals(name.trim()))
				return true;
		return false;
	}

	@Override
	public boolean renameMusicPackProject(MusicPackProject project, String newName) throws InvalidInputException {
		this.validateRegisteredProject(project);
		this.validateMPPName(newName, project);
		String oldName = project.getName();
		if (!newName.trim().equals(oldName)) {
			((MusicPackProjectImpl) project).setName(newName.trim());
			this.logger.log(LogService.LOG_INFO,
					String.format("Renamed the Music Pack Project \"%s\" to \"%s\"", oldName, newName));
			return true;
		}
		return false;
	}

	private void validateMPPName(String name) throws InvalidInputException {
		this.validateMPPName(name, null);
	}

	private void validateMPPName(String name, MusicPackProject ignore) throws InvalidInputException {
		Objects.requireNonNull(name);
		if (name.trim().isEmpty())
			throw new InvalidInputException(MusicPackProjectNameErrors.EMPTY);
		if (this.existsMPPName(name, ignore))
			throw new InvalidInputException(MusicPackProjectNameErrors.DUPLICATED);
	}

	private void validateRegisteredProject(MusicPackProject project) {
		this.validateProject(project, true);
	}

	private void validateUnregisteredProject(MusicPackProject project) {
		this.validateProject(project, false);
	}

	private void validateProject(MusicPackProject project, boolean registered) {
		Objects.requireNonNull(project);
		if (!(project instanceof MusicPackProjectImpl))
			throw new IllegalArgumentException(
					String.format("The Music Pack Project \"%s\" cannot be managed by this Music Pack Project Manager",
							project.getName()));
		if (registered ? !this.musicPackProjects.contains(project) : this.musicPackProjects.contains(project))
			throw new IllegalArgumentException(String.format("The Music Pack Project \"%s\" is %s registered",
					project.getName(), registered ? "not" : "already"));
	}

	@Override
	public void deleteMusicPackProject(MusicPackProject project) {
		this.validateRegisteredProject(project);
		this.persistenceManager.deleteMusicPackProject(project);
		this.musicPackProjects.remove(project);
		this.logger.log(LogService.LOG_INFO, String.format("Deleted the Music Pack Project \"%s\"", project.getName()));
	}

	@Override
	public Collection<MusicPackProject> saveAllMusicPackProjects() {
		Collection<MusicPackProject> erroredProjects = new ArrayList<>();
		for (MusicPackProject project : this.musicPackProjects) {
			try {
				this.saveMusicPackProject(project);
			} catch (Exception e) {
				this.logger.log(LogService.LOG_ERROR,
						String.format("The Music Pack Project \"%s\" couldn't be saved: ", project.getName()), e);
				WriteableEventProperties properties = new DefaultWriteableEventProperties();
				properties.put(MusicPackProjectManager.SAVE_ALL_PROJECT_ERROR_EVENT_MUSIC_PACK_PROJECT, project);
				properties.put(MusicPackProjectManager.SAVE_ALL_PROJECT_ERROR_EVENT_EXCEPTION, e);
				this.eventManager.dispatchEvent(MusicPackProjectManager.SAVE_ALL_PROJECT_ERROR_EVENT, properties);
				erroredProjects.add(project);
			}
		}
		return erroredProjects;
	}

	@Override
	public void saveMusicPackProject(MusicPackProject project) {
		this.validateRegisteredProject(project);
		this.addSaveProperties(project);
		this.persistenceManager.saveMusicPackProject(project);
		this.logger.log(LogService.LOG_DEBUG, String.format("Saved the Music Pack Project \"%s\"", project.getName()));
	}

	private void addSaveProperties(MusicPackProject project) {
		project.getProperties().setString(MusicPackProject.PROPERTY_MPC_VERSION,
				this.musicPackCreator.getVersion().toString());
	}

	@Override
	public Collection<MusicPackProject> getRegisteredMusicPackProjects() {
		return Collections.unmodifiableList(this.musicPackProjects);
	}

}

package craftedMods.lotr.mpc.core.provider;

import java.util.Arrays;
import java.util.Collection;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.Mock;
import org.easymock.MockType;
import org.easymock.TestSubject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.osgi.service.log.FormatterLogger;

import craftedMods.eventManager.api.EventInfo;
import craftedMods.eventManager.api.EventManager;
import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.lotr.mpc.compatibility.api.MusicPackProjectCompatibilityManager;
import craftedMods.lotr.mpc.core.api.MusicPackProject;
import craftedMods.lotr.mpc.core.api.MusicPackProjectFactory;
import craftedMods.lotr.mpc.core.api.MusicPackProjectManager;
import craftedMods.lotr.mpc.persistence.api.MusicPackProjectPersistenceManager;
import craftedMods.utils.exceptions.InvalidInputException;
import craftedMods.versionChecker.api.SemanticVersion;
import craftedMods.versionChecker.base.DefaultSemanticVersion;

@RunWith(EasyMockRunner.class)
public class MusicPackProjectManagerImplTest {

	@TestSubject
	private MusicPackProjectManagerImpl musicPackProjectManager = new MusicPackProjectManagerImpl();

	@Mock(type = MockType.NICE)
	private FormatterLogger mockLogger;

	private SemanticVersion mpcVersion;

	@Mock(type = MockType.NICE)
	private EventManager mockEventManager;

	@Mock
	private MusicPackProjectPersistenceManager mockPersistenceManager;

	@Mock
	private MusicPackProjectFactory mockMusicPackProjectFactory;
	
	@Mock(type=MockType.STRICT)
	private MusicPackProjectCompatibilityManager mockCompatibilityManager;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MusicPackProject loadedProject;
	private MusicPackProject testProject;

	@Before
	public void setup() {
		this.loadedProject = new MusicPackProjectImpl("Loaded Project");
		this.testProject = new MusicPackProjectImpl("Project");

		mpcVersion = DefaultSemanticVersion.of("2.0.0");

		musicPackProjectManager.mpcVersion = mpcVersion;

		EasyMock.expect(this.mockPersistenceManager.loadMusicPackProjects()).andReturn(Arrays.asList()).once();
		EasyMock.expect(this.mockMusicPackProjectFactory.createMusicPackProjectInstance(EasyMock.anyString()))
				.andStubAnswer(() -> {
					return new MusicPackProjectImpl(EasyMock.getCurrentArguments()[0].toString());
				});
		this.mockCompatibilityManager.applyPreRegisterFixes(EasyMock.anyObject(), EasyMock.anyString());
		EasyMock.expectLastCall().anyTimes();

		EasyMock.replay(this.mockPersistenceManager);
		EasyMock.replay(this.mockEventManager);
		EasyMock.replay(this.mockMusicPackProjectFactory);
		EasyMock.replay(this.mockCompatibilityManager);

		this.musicPackProjectManager.onActivate();

		EasyMock.replay(this.mockLogger);
	}

	@Test
	public void testActivationWithNoLoadedProjects() {
		EasyMock.reset(this.mockPersistenceManager);
		EasyMock.expect(this.mockPersistenceManager.loadMusicPackProjects()).andReturn(Arrays.asList()).once();
		EasyMock.replay(this.mockPersistenceManager);

		this.musicPackProjectManager.onActivate();

		EasyMock.verify(this.mockPersistenceManager);

		Assert.assertTrue(this.musicPackProjectManager.getRegisteredMusicPackProjects().isEmpty());
	}

	@Test
	public void testActivationWithLoadedProjectsAndCompatibilityManager() {
		EasyMock.reset(this.mockPersistenceManager);
		EasyMock.expect(this.mockPersistenceManager.loadMusicPackProjects())
				.andReturn(Arrays.asList(this.loadedProject)).once();
		
		EasyMock.reset(this.mockCompatibilityManager);
		this.mockCompatibilityManager.applyPreRegisterFixes(this.loadedProject, null);
		EasyMock.expectLastCall().once();
		
		
		EasyMock.replay(this.mockPersistenceManager);
		EasyMock.replay(this.mockCompatibilityManager);

		this.musicPackProjectManager.onActivate();

		EasyMock.verify(this.mockPersistenceManager);
		EasyMock.verify(this.mockCompatibilityManager);

		Assert.assertEquals(1, this.musicPackProjectManager.getRegisteredMusicPackProjects().size());
		Assert.assertTrue(this.musicPackProjectManager.getRegisteredMusicPackProjects().contains(this.loadedProject));
	}
	
	@Test
	public void testActivationWithLoadedProjectsAndOneError() {
		EasyMock.reset(this.mockPersistenceManager);
		EasyMock.expect(this.mockPersistenceManager.loadMusicPackProjects())
				.andReturn(Arrays.asList(this.loadedProject, this.loadedProject)).once();
		EasyMock.replay(this.mockPersistenceManager);

		Capture<EventInfo> loadingErrorEventInfoCapture = EasyMock.newCapture();
		Capture<WriteableEventProperties> loadingErrorEventPropertiesCapture = EasyMock.newCapture();
		EasyMock.resetToStrict(this.mockEventManager);
		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.capture(loadingErrorEventInfoCapture),
				EasyMock.capture(loadingErrorEventPropertiesCapture))).andReturn(null).once();
		EasyMock.replay(this.mockEventManager);

		this.musicPackProjectManager.onActivate();

		EasyMock.verify(this.mockPersistenceManager);
		EasyMock.verify(this.mockEventManager);

		Assert.assertEquals(MusicPackProjectManager.LOAD_ALL_REGISTER_PROJECT_ERROR_EVENT,
				loadingErrorEventInfoCapture.getValue());
		Assert.assertTrue(loadingErrorEventPropertiesCapture.getValue()
				.containsProperty(MusicPackProjectManager.LOAD_ALL_REGISTER_PROJECT_ERROR_EVENT_EXCEPTION));
		Assert.assertEquals(1, this.musicPackProjectManager.getRegisteredMusicPackProjects().size());
		Assert.assertTrue(this.musicPackProjectManager.getRegisteredMusicPackProjects().contains(this.loadedProject));
	}

	@Test
	public void testRegisterMusicPackProject() throws InvalidInputException {
		Assert.assertFalse(this.musicPackProjectManager.getRegisteredMusicPackProjects().contains(this.testProject));
		MusicPackProject project = this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		Assert.assertTrue(this.musicPackProjectManager.getRegisteredMusicPackProjects().contains(this.testProject));
		Assert.assertEquals(project.getName(), this.testProject.getName().trim());
	}

	@Test
	public void testRegisterMusicPackProjectWithTrailingWhitespaces() throws InvalidInputException {
		MusicPackProject project = new MusicPackProjectImpl("	 	 	Proj.	 	 	");
		this.musicPackProjectManager.registerMusicPackProject(project);
		Assert.assertEquals(project.getName(), "Proj.");
	}

	@Test(expected = NullPointerException.class)
	public void testRegisterMusicPackProjectNull() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(null);
	}

	@Test
	public void testRegisterMusicPackProjectAlreadyRegistered() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		this.expectedException.expect(IllegalArgumentException.class);
		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
	}

	@Test(expected = NullPointerException.class)
	public void testRegisterMusicPackProjectNullName() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl(null));
	}

	@Test(expected = InvalidInputException.class)
	public void testRegisterMusicPackProjectWhitespaceName() throws InvalidInputException {
		try {
			this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("             "));
		} catch (InvalidInputException e) {
			Assert.assertEquals(e.getErrorCode(), MusicPackProjectManager.MusicPackProjectNameErrors.EMPTY);
			throw e;
		}
	}

	@Test(expected = InvalidInputException.class)
	public void testRegisterMusicPackProjecEmptyName() throws InvalidInputException {
		try {
			this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl(""));
		} catch (InvalidInputException e) {
			Assert.assertEquals(e.getErrorCode(), MusicPackProjectManager.MusicPackProjectNameErrors.EMPTY);
			throw e;
		}
	}

	@Test
	public void testRegisterMusicPackProjecDuplicatedName() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		try {
			this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("Project"));
		} catch (InvalidInputException e) {
			Assert.assertEquals(e.getErrorCode(), MusicPackProjectManager.MusicPackProjectNameErrors.DUPLICATED);
			this.expectedException.expect(InvalidInputException.class);
			throw e;
		}
	}

	@Test
	public void testGetUnusedMusicPackProjectName() {
		Assert.assertEquals("MusicPackProject232",
				this.musicPackProjectManager.getUnusedMusicPackProjectName("MusicPackProject232"));
	}

	@Test
	public void testGetUnusedMusicPackProjectNameDuplicated() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("MusicPackProject232"));
		Assert.assertEquals("MusicPackProject232_1",
				this.musicPackProjectManager.getUnusedMusicPackProjectName("MusicPackProject232"));
		this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("MusicPackProject232_1"));
		Assert.assertEquals("MusicPackProject232_2",
				this.musicPackProjectManager.getUnusedMusicPackProjectName("MusicPackProject232"));
	}

	@Test(expected = NullPointerException.class)
	public void testGetUnusedMusicPackProjectNameNull() {
		this.musicPackProjectManager.getUnusedMusicPackProjectName(null);
	}

	@Test
	public void testGetUnusedMusicPackProjectNameWhitespaceName() {
		Assert.assertEquals("MusicPackProject",
				this.musicPackProjectManager.getUnusedMusicPackProjectName("	 	 	"));
	}

	@Test
	public void testGetUnusedMusicPackProjectNameEmpty() {
		Assert.assertEquals("MusicPackProject", this.musicPackProjectManager.getUnusedMusicPackProjectName(""));
	}

	@Test
	public void testRenameMusicPackProject() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		Assert.assertTrue(this.musicPackProjectManager.renameMusicPackProject(this.testProject, "Project2"));
		Assert.assertEquals(this.testProject.getName(), "Project2");
	}

	@Test
	public void testRenameMusicPackProjectSameName() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		Assert.assertFalse(this.musicPackProjectManager.renameMusicPackProject(this.testProject, "Project"));
		Assert.assertEquals(this.testProject.getName(), "Project");
	}

	@Test
	public void testRenameMusicPackProjectSameNameWithTrailingWhitespaces() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		Assert.assertFalse(
				this.musicPackProjectManager.renameMusicPackProject(this.testProject, "	 	Project  	 	"));
		Assert.assertEquals(this.testProject.getName(), "Project");
	}

	@Test(expected = NullPointerException.class)
	public void testRenameMusicPackProjectNullProject() throws InvalidInputException {
		this.musicPackProjectManager.renameMusicPackProject(null, "Test");
	}

	@Test
	public void testRenameMusicPackProjectNullName() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		this.expectedException.expect(NullPointerException.class);
		this.musicPackProjectManager.renameMusicPackProject(this.testProject, null);
	}

	@Test
	public void testRenameMusicPackProjectWhitespaceName() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		this.expectedException.expect(InvalidInputException.class);
		try {
			this.musicPackProjectManager.renameMusicPackProject(this.testProject, "	 	 	 ");
		} catch (InvalidInputException e) {
			Assert.assertEquals(e.getErrorCode(), MusicPackProjectManager.MusicPackProjectNameErrors.EMPTY);
			throw e;
		}
	}

	@Test
	public void testRenameMusicPackProjecEmptyName() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		this.expectedException.expect(InvalidInputException.class);
		try {
			this.musicPackProjectManager.renameMusicPackProject(this.testProject, "");
		} catch (InvalidInputException e) {
			Assert.assertEquals(e.getErrorCode(), MusicPackProjectManager.MusicPackProjectNameErrors.EMPTY);
			throw e;
		}
	}

	@Test
	public void testRenameMusicPackProjecDuplicatedName() throws InvalidInputException {
		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("Test"));
		this.expectedException.expect(InvalidInputException.class);
		try {
			this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("Test"));
		} catch (InvalidInputException e) {
			Assert.assertEquals(e.getErrorCode(), MusicPackProjectManager.MusicPackProjectNameErrors.DUPLICATED);
			throw e;
		}
	}

	@Test
	public void testRenameMusicPackProjectUnregistered() throws InvalidInputException {
		this.expectedException.expect(IllegalArgumentException.class);
		this.musicPackProjectManager.renameMusicPackProject(this.testProject, "Test");
	}

	@Test
	public void testDeleteMusicPackProject() throws InvalidInputException {
		EasyMock.reset(this.mockPersistenceManager);
		this.mockPersistenceManager.deleteMusicPackProject(this.testProject);
		EasyMock.expectLastCall().andReturn(true).once();
		EasyMock.replay(this.mockPersistenceManager);

		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		this.musicPackProjectManager.deleteMusicPackProject(this.testProject);

		EasyMock.verify(this.mockPersistenceManager);

		Assert.assertFalse(this.musicPackProjectManager.getRegisteredMusicPackProjects().contains(this.testProject));
	}

	@Test(expected = NullPointerException.class)
	public void testDeleteMusicPackProjectNullProject() {
		this.musicPackProjectManager.deleteMusicPackProject(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDeleteMusicPackProjectUnregisteredProject() {
		this.musicPackProjectManager.deleteMusicPackProject(this.testProject);
	}

	@Test
	public void testSaveMusicPackProject() throws InvalidInputException {
		EasyMock.reset(this.mockPersistenceManager);
		this.mockPersistenceManager.saveMusicPackProject(this.testProject);
		EasyMock.expectLastCall().andVoid().once();

		EasyMock.replay(this.mockPersistenceManager);

		this.testProject.getProperties().put(MusicPackProject.PROPERTY_MPC_VERSION, "1.0.0");

		this.musicPackProjectManager.registerMusicPackProject(this.testProject);
		this.musicPackProjectManager.saveMusicPackProject(this.testProject);

		EasyMock.verify(this.mockPersistenceManager);

		Assert.assertEquals("2.0.0", this.testProject.getProperties().get(MusicPackProject.PROPERTY_MPC_VERSION));
	}

	@Test(expected = NullPointerException.class)
	public void testSaveMusicPackProjectNullProject() {
		this.musicPackProjectManager.saveMusicPackProject(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSaveMusicPackProjectUnregisteredProject() {
		this.musicPackProjectManager.saveMusicPackProject(this.testProject);
	}

	@Test
	public void testSaveAllMusicPackProjects() throws InvalidInputException {
		EasyMock.reset(this.mockPersistenceManager);
		this.mockPersistenceManager.saveMusicPackProject(EasyMock.anyObject(MusicPackProject.class));
		EasyMock.expectLastCall().times(3);

		EasyMock.replay(this.mockPersistenceManager);

		this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("1"));
		this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("2"));
		this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("3"));

		Assert.assertTrue(this.musicPackProjectManager.saveAllMusicPackProjects().isEmpty());

		EasyMock.verify(this.mockPersistenceManager);
	}

	@Test
	public void testSaveAllMusicPackProjectsWithOneError() throws InvalidInputException {
		EasyMock.reset(this.mockPersistenceManager);
		this.mockPersistenceManager.saveMusicPackProject(EasyMock.anyObject(MusicPackProject.class));
		EasyMock.expectLastCall().times(3).andThrow(new RuntimeException("<--!!!Error!!!-->")).once();

		Capture<EventInfo> saveAllErrorEventInfoCapture = EasyMock.newCapture();
		Capture<WriteableEventProperties> saveAllErrorEventPropertiesCapture = EasyMock.newCapture();

		EasyMock.resetToStrict(this.mockEventManager);
		EasyMock.expect(this.mockEventManager.dispatchEvent(EasyMock.capture(saveAllErrorEventInfoCapture),
				EasyMock.capture(saveAllErrorEventPropertiesCapture))).andReturn(null).once();

		EasyMock.replay(this.mockPersistenceManager);
		EasyMock.replay(this.mockEventManager);

		this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("1"));
		this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("2"));
		this.musicPackProjectManager.registerMusicPackProject(new MusicPackProjectImpl("3"));
		MusicPackProject failedProject = this.musicPackProjectManager
				.registerMusicPackProject(new MusicPackProjectImpl("4"));

		Collection<MusicPackProject> erroredProjects = this.musicPackProjectManager.saveAllMusicPackProjects();

		EasyMock.verify(this.mockPersistenceManager);
		EasyMock.verify(this.mockEventManager);

		WriteableEventProperties saveAllErrorEventProperties = saveAllErrorEventPropertiesCapture.getValue();

		Assert.assertEquals(MusicPackProjectManager.SAVE_ALL_PROJECT_ERROR_EVENT,
				saveAllErrorEventInfoCapture.getValue());
		Assert.assertEquals(failedProject, saveAllErrorEventProperties
				.getProperty(MusicPackProjectManager.SAVE_ALL_PROJECT_ERROR_EVENT_MUSIC_PACK_PROJECT));
		Assert.assertEquals(RuntimeException.class, saveAllErrorEventProperties
				.getProperty(MusicPackProjectManager.SAVE_ALL_PROJECT_ERROR_EVENT_EXCEPTION).getClass());
		Assert.assertEquals("<--!!!Error!!!-->", saveAllErrorEventProperties
				.getProperty(MusicPackProjectManager.SAVE_ALL_PROJECT_ERROR_EVENT_EXCEPTION).getMessage());

		Assert.assertEquals(1, erroredProjects.size());
		Assert.assertEquals(failedProject, erroredProjects.iterator().next());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetRegisteredMusicPackProjectsIsUnmodifiable() {
		this.musicPackProjectManager.getRegisteredMusicPackProjects().clear();
	}
}

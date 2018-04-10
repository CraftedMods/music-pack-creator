package craftedMods.eventManager.provider;

import static org.junit.Assert.*;

import org.junit.*;

import craftedMods.eventManager.api.*;
import craftedMods.eventManager.base.DefaultEventInfo;

public class EventImplTest {

	private EventImpl event;
	private EventInfo info;

	@Before
	public void setup() {
		info = new DefaultEventInfo("TOPIC");
		event = new EventImpl(info, null);
	}

	@Test
	public void testInitialEventResults() {
		assertNotNull(event.getEventResults());
	}

	@Test
	public void testRecreatedEventResults() {
		WriteableEventProperties properties = event.getEventResults();
		event.recreateEventResults();
		WriteableEventProperties newProperties = event.getEventResults();
		assertNotNull(newProperties);
		assertNotSame(properties, newProperties);
	}

	@Test
	public void testMatches() {
		assertTrue(event.matches(info));
	}

}

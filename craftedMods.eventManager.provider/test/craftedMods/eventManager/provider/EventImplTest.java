package craftedMods.eventManager.provider;

import org.junit.*;

import craftedMods.eventManager.api.*;
import craftedMods.eventManager.base.DefaultEventInfo;
import craftedMods.utils.data.LockableTypedProperties;

public class EventImplTest {

	private EventImpl event;
	private EventInfo info;

	@Before
	public void setup() {
		this.info = new DefaultEventInfo("TOPIC");
		this.event = new EventImpl(this.info, null);
	}

	@Test
	public void testInitialEventResults() {
		Assert.assertNotNull(this.event.getEventResults());
	}

	@Test
	public void testRecreatedEventResults() {
		LockableTypedProperties properties = this.event.getEventResults();
		this.event.recreateEventResults();
		LockableTypedProperties newProperties = this.event.getEventResults();
		Assert.assertNotNull(newProperties);
		Assert.assertNotSame(properties, newProperties);
	}

	@Test
	public void testMatches() {
		Assert.assertTrue(this.event.matches(this.info));
	}

}

package craftedMods.eventManager.base;

import org.junit.*;

import craftedMods.eventManager.api.*;

public class EventUtilsTest {

	@Test
	public void testGetSupportedEventsDefaultPolicy() {
		EventInfo info = new DefaultEventInfo("TOPIC");
		Assert.assertEquals(EventHandlerPolicy.NOT_SPECIFIED, EventUtils.getSupportedEvents(info).get(info));
	}

}

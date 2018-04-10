package craftedMods.eventManager.base;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DefaultEventInfoTest {

	@Test
	public void testEventTopicFromClassAndSuffixConstructionFunction() {
		assertEquals("java/lang/String/EVENT_TOPIC", DefaultEventInfo.getEventTopicFromClass(String.class, "EVENT_TOPIC"));
	}

}

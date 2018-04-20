package craftedMods.eventManager.base;

import org.junit.*;

public class DefaultEventInfoTest {

	@Test
	public void testEventTopicFromClassAndSuffixConstructionFunction() {
		Assert.assertEquals("java/lang/String/EVENT_TOPIC", DefaultEventInfo.getEventTopicFromClass(String.class, "EVENT_TOPIC"));
	}

}

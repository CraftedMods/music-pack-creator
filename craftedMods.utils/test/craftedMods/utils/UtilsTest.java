package craftedMods.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(EasyMockRunner.class)
public class UtilsTest extends EasyMockSupport {

	@Test
	public void testIsASCIIValid() {
		Assert.assertTrue(Utils.isASCII("asdklj"));
		Assert.assertTrue(Utils.isASCII("asdk-.-,lj"));
		Assert.assertTrue(Utils.isASCII("asdkl,;,._:; j"));
		Assert.assertTrue(Utils.isASCII("asdklj				"));
		Assert.assertTrue(Utils.isASCII("!345&%$@!?="));
		Assert.assertTrue(Utils.isASCII("~~~-#-+-"));
	}

	@Test
	public void testIsASCIIInvalid() {
		Assert.assertFalse(Utils.isASCII("asdklj		§§§		"));
		Assert.assertFalse(Utils.isASCII("°"));
		Assert.assertFalse(Utils.isASCII("TEXTLJSDLKFJµ"));

	}

	@Test
	public void testClampInBounds() {
		Assert.assertEquals(50, Utils.clamp(100, 0, 50));
	}

	@Test
	public void testClampInBoundsNearMin() {
		Assert.assertEquals(1, Utils.clamp(100, 0, 1));
	}

	@Test
	public void testClampInBoundsMin() {
		Assert.assertEquals(0, Utils.clamp(100, 0, 0));
	}

	@Test
	public void testClampOutOfBoundsNearMin() {
		Assert.assertEquals(0, Utils.clamp(100, 0, -1));
	}

	@Test
	public void testClampOutOfBoundsNearMax() {
		Assert.assertEquals(100, Utils.clamp(100, 0, 101));
	}

	@Test
	public void testClampInBoundsMax() {
		Assert.assertEquals(100, Utils.clamp(100, 0, 100));
	}

	@Test
	public void testClampInBoundsNearMax() {
		Assert.assertEquals(99, Utils.clamp(100, 0, 99));
	}

	@Test
	public void testOutOfBoundsMin() {
		Assert.assertEquals(0, Utils.clamp(100, 0, -123));
	}

	@Test
	public void testOutOfBoundsMax() {
		Assert.assertEquals(100, Utils.clamp(100, 0, 2123));
	}

	@Test
	public void testGetFormattedTime() {
		Assert.assertEquals("00:00:01", Utils.getFormattedTime(1000l));
		Assert.assertEquals("00:00:10", Utils.getFormattedTime(10000l));
		Assert.assertEquals("00:01:00", Utils.getFormattedTime(60000l));
		Assert.assertEquals("00:10:00", Utils.getFormattedTime(600000l));
		Assert.assertEquals("01:00:00", Utils.getFormattedTime((long) 3.6e6));
		Assert.assertEquals("10:00:00", Utils.getFormattedTime((long) 3.6e7));
		Assert.assertEquals("10:34:12", Utils.getFormattedTime(38_052_000l));
	}

	@Test
	public void testGetFormattedTimeUnderflow() {
		Assert.assertEquals("00:00:00", Utils.getFormattedTime(1));
	}

	@Test
	public void testGetFormattedTimeOverflow() {
		Assert.assertEquals("25:00:00", Utils.getFormattedTime((long) 9e7));
		Assert.assertEquals("100:00:00", Utils.getFormattedTime((long) 3.6e8));
	}

	@Test
	public void testGetFormattedTimeNegative() {
		Assert.assertEquals("-10:10:00", Utils.getFormattedTime((long) -3.66e7));
	}

	@Test
	public void testGetFormattedTimeMillis() {
		Assert.assertEquals("00:00:00:001", Utils.getFormattedTime(1l, true));
		Assert.assertEquals("00:00:01:000", Utils.getFormattedTime(1000l, true));
	}

	@Test
	public void testGetFormattedTimeCustomSeparator() {
		Assert.assertEquals("00||01||00", Utils.getFormattedTime(60000l, "||"));
	}

	@Test(expected = NullPointerException.class)
	public void testWriteFromInputStreamToOutputStreamInNull() throws IOException {
		Utils.writeFromInputStreamToOutputStream(null, this.createMock(OutputStream.class));
	}
	
	@Test(expected = NullPointerException.class)
	public void testWriteFromInputStreamToOutputStreamOutNull() throws IOException {
		Utils.writeFromInputStreamToOutputStream(this.createMock(InputStream.class),null);
	}

}

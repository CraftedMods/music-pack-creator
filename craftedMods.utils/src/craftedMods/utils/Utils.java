package craftedMods.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Utils {

	public static Locale[] getSortedLocales() {
		Locale[] locales = Locale.getAvailableLocales();
		String[] names = new String[locales.length];
		Map<String, Locale> localesToName = new HashMap<>();
		for (int i = 0; i < locales.length; i++) {
			names[i] = locales[i].getDisplayName();
			localesToName.put(names[i], locales[i]);
		}
		Arrays.sort(names);
		for (int i = 0; i < names.length; i++) {
			locales[i] = localesToName.get(names[i]);
		}
		return locales;
	}

	public static boolean isASCII(String string) {
		return Charset.forName("US-ASCII").newEncoder().canEncode(string);
	}

	public static int clamp(int max, int min, int value) {
		return value > max ? max : value < min ? min : value;
	}

	public static String getFormattedTime(long timeMillis) {
		return getFormattedTime(timeMillis, false, ":");
	}

	public static String getFormattedTime(long timeMillis, boolean millis) {
		return getFormattedTime(timeMillis, millis, ":");
	}

	public static String getFormattedTime(long timeMillis, String separator) {
		return getFormattedTime(timeMillis, false, separator);
	}

	public static String getFormattedTime(long timeMillis, boolean millis, String separator) {
		long hrs = Math.abs(TimeUnit.MILLISECONDS.toHours(timeMillis));
		long min = Math.abs(TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60);
		long sec = Math.abs(TimeUnit.MILLISECONDS.toSeconds(timeMillis) % 60);
		long mls = Math.abs(timeMillis % 1000);
		String sign = timeMillis < 0 ? "-" : "";
		if (millis)
			return String.format("%6$s%1$02d%4$s%2$02d%4$s%3$02d%4$s%5$03d", hrs, min, sec, separator, mls, sign);
		return String.format("%5$s%1$02d%4$s%2$02d%4$s%3$02d", hrs, min, sec, separator, sign);
	}

	/**
	 * Writes all data from the specified input stream to the specified output
	 * stream. The streams won't be closed.
	 * 
	 * @param in  The input stream
	 * @param out The output stream
	 * @throws IOException If the writing/reading operations go wrong
	 */
	public static void writeFromInputStreamToOutputStream(InputStream in, OutputStream out) throws IOException {
		Objects.requireNonNull(in);
		Objects.requireNonNull(out);
		int read = -1;
		while ((read = in.read()) != -1) {
			out.write(read);
		}
	}

}

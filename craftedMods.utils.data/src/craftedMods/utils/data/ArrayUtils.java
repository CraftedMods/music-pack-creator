package craftedMods.utils.data;

import java.util.Objects;

public class ArrayUtils {

	public static <T> boolean contains(T[] array, T value) {
		Objects.requireNonNull(array);
		boolean contains = false;
		for (T val : array) {
			if (value == null ? val == null : value.equals(val)) {
				contains = true;
				break;
			}
		}
		return contains;
	}

}

package craftedMods.utils.data;

public class ArrayUtils {

	public static <T> boolean contains(T[] array, T value) {
		boolean contains = false;
		for (T val : array) {
			if (value.equals(val)) {
				contains = true;
				break;
			}
		}
		return contains;
	}

}

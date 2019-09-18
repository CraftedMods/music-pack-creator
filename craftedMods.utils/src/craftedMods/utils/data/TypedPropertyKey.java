package craftedMods.utils.data;

public class TypedPropertyKey<T> {

	private final long propertyKey;
	private final Class<T> propertyKeyType;

	private static long counter = 0;

	private TypedPropertyKey(Class<T> propertyKeyType) {
		this.propertyKey = TypedPropertyKey.counter++;
		this.propertyKeyType = propertyKeyType;
	}

	public Long getUniquePropertyIdentifier() {
		return this.propertyKey;
	}

	public Class<T> getPropertyType() {
		return this.propertyKeyType;
	}

	public static <T> TypedPropertyKey<T> createPropertyKey(Class<T> propertyKeyType) {
		return new TypedPropertyKey<>(propertyKeyType);
	}

	public static TypedPropertyKey<String> createStringPropertyKey() {
		return new TypedPropertyKey<>(String.class);
	}

	public static TypedPropertyKey<Byte> createBytePropertyKey() {
		return new TypedPropertyKey<>(Byte.class);
	}

	public static TypedPropertyKey<Short> createShortPropertyKey() {
		return new TypedPropertyKey<>(Short.class);
	}

	public static TypedPropertyKey<Integer> createIntegerPropertyKey() {
		return new TypedPropertyKey<>(Integer.class);
	}

	public static TypedPropertyKey<Character> createCharacterPropertyKey() {
		return new TypedPropertyKey<>(Character.class);
	}

	public static TypedPropertyKey<Long> createLongPropertyKey() {
		return new TypedPropertyKey<>(Long.class);
	}

	public static TypedPropertyKey<Float> createFloatPropertyKey() {
		return new TypedPropertyKey<>(Float.class);
	}

	public static TypedPropertyKey<Double> createDoublePropertyKey() {
		return new TypedPropertyKey<>(Double.class);
	}

	public static TypedPropertyKey<Boolean> createBooleanPropertyKey() {
		return new TypedPropertyKey<>(Boolean.class);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (this.propertyKey ^ this.propertyKey >>> 32);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		TypedPropertyKey<?> other = (TypedPropertyKey<?>) obj;
		if (this.propertyKey != other.propertyKey)
			return false;
		return true;
	}
}

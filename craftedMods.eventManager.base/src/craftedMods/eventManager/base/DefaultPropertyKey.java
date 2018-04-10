package craftedMods.eventManager.base;

import craftedMods.eventManager.api.PropertyKey;

public class DefaultPropertyKey<T> implements PropertyKey<T> {

	private final long propertyKey;
	private final Class<T> propertyKeyType;

	private static long counter = 0;

	public DefaultPropertyKey(Class<T> propertyKeyType) {
		this.propertyKey = counter++;
		this.propertyKeyType = propertyKeyType;
	}

	@Override
	public Long getUniquePropertyIdentifier() {
		return this.propertyKey;
	}

	@Override
	public Class<T> getPropertyType() {
		return this.propertyKeyType;
	}

	public static PropertyKey<String> getStringPropertyKey() {
		return new DefaultPropertyKey<>(String.class);
	}

	public static PropertyKey<Byte> getBytePropertyKey() {
		return new DefaultPropertyKey<>(Byte.class);
	}

	public static PropertyKey<Short> getShortPropertyKey() {
		return new DefaultPropertyKey<>(Short.class);
	}

	public static PropertyKey<Integer> getIntegerPropertyKey() {
		return new DefaultPropertyKey<>(Integer.class);
	}

	public static PropertyKey<Character> getCharacterPropertyKey() {
		return new DefaultPropertyKey<>(Character.class);
	}

	public static PropertyKey<Long> getLongPropertyKey() {
		return new DefaultPropertyKey<>(Long.class);
	}

	public static PropertyKey<Float> getFloatPropertyKey() {
		return new DefaultPropertyKey<>(Float.class);
	}

	public static PropertyKey<Double> getDoublePropertyKey() {
		return new DefaultPropertyKey<>(Double.class);
	}

	public static PropertyKey<Boolean> getBooleanPropertyKey() {
		return new DefaultPropertyKey<>(Boolean.class);
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
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;
		DefaultPropertyKey<?> other = (DefaultPropertyKey<?>) obj;
		if (this.propertyKey != other.propertyKey) return false;
		return true;
	}

}

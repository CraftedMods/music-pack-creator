package craftedMods.eventManager.base;

import craftedMods.eventManager.api.EventPropertyKey;

public class DefaultEventPropertyKey<T> implements EventPropertyKey<T> {

	private final long propertyKey;
	private final Class<T> propertyKeyType;

	private static long counter = 0;

	public DefaultEventPropertyKey(Class<T> propertyKeyType) {
		this.propertyKey = counter++;
		this.propertyKeyType = propertyKeyType;
	}

	@Override
	public Long getPropertyKey() {
		return this.propertyKey;
	}

	@Override
	public Class<T> getPropertyType() {
		return this.propertyKeyType;
	}

	public static EventPropertyKey<String> getStringPropertyKey() {
		return new DefaultEventPropertyKey<>(String.class);
	}

	public static EventPropertyKey<Byte> getBytePropertyKey() {
		return new DefaultEventPropertyKey<>(Byte.class);
	}

	public static EventPropertyKey<Short> getShortPropertyKey() {
		return new DefaultEventPropertyKey<>(Short.class);
	}

	public static EventPropertyKey<Integer> getIntegerPropertyKey() {
		return new DefaultEventPropertyKey<>(Integer.class);
	}

	public static EventPropertyKey<Character> getCharacterPropertyKey() {
		return new DefaultEventPropertyKey<>(Character.class);
	}

	public static EventPropertyKey<Long> getLongPropertyKey() {
		return new DefaultEventPropertyKey<>(Long.class);
	}

	public static EventPropertyKey<Float> getFloatPropertyKey() {
		return new DefaultEventPropertyKey<>(Float.class);
	}

	public static EventPropertyKey<Double> getDoublePropertyKey() {
		return new DefaultEventPropertyKey<>(Double.class);
	}

	public static EventPropertyKey<Boolean> getBooleanPropertyKey() {
		return new DefaultEventPropertyKey<>(Boolean.class);
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
		DefaultEventPropertyKey<?> other = (DefaultEventPropertyKey<?>) obj;
		if (this.propertyKey != other.propertyKey) return false;
		return true;
	}

}

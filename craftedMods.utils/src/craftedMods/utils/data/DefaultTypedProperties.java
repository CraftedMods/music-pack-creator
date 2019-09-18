package craftedMods.utils.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultTypedProperties implements TypedProperties {

	protected final Map<TypedPropertyKey<?>, Object> properties = Collections.synchronizedMap(new HashMap<>());

	@Override
	@SuppressWarnings("unchecked")
	public <T> T put(TypedPropertyKey<T> key, T value) {
		Objects.requireNonNull(key);
		return (T) this.properties.put(key, value);
	}

	@Override
	public boolean isEmpty() {
		return this.properties.isEmpty();
	}

	@Override
	public <T> boolean containsProperty(TypedPropertyKey<T> property) {
		return this.properties.containsKey(property);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getProperty(TypedPropertyKey<T> property) {
		return (T) this.properties.get(property);
	}

	@Override
	public void clear() {
		this.properties.clear();
	}

}

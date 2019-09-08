package craftedMods.eventManager.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import craftedMods.eventManager.api.PropertyKey;
import craftedMods.eventManager.api.WriteableEventProperties;

public class DefaultWriteableEventProperties implements WriteableEventProperties {

	private Map<PropertyKey<?>, Object> properties = Collections.synchronizedMap(new HashMap<>());

	private boolean isLocked = false;

	@Override
	@SuppressWarnings("unchecked")
	public <T> T put(PropertyKey<T> key, T value) {
		this.checkState();
		Objects.requireNonNull(key);
		return (T) this.properties.put(key, value);
	}

	@Override
	public boolean isEmpty() {
		return this.properties.isEmpty();
	}

	@Override
	public <T> boolean containsProperty(PropertyKey<T> property) {
		return this.properties.containsKey(property);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getProperty(PropertyKey<T> property) {
		return (T) this.properties.get(property);
	}

	@Override
	public void clear() {
		this.checkState();
		this.properties.clear();
	}

	private void checkState() throws IllegalStateException {
		if (this.isLocked) throw new IllegalStateException("The writeable event properties were locked");
	}

	@Override
	public boolean isLocked() {
		return this.isLocked;
	}

	@Override
	public boolean lock() {
		return this.isLocked ? false : (this.isLocked = true);
	}

}

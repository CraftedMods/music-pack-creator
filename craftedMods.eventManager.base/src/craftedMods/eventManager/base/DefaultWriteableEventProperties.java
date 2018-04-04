package craftedMods.eventManager.base;

import java.util.Hashtable;

import craftedMods.eventManager.api.*;

public class DefaultWriteableEventProperties implements WriteableEventProperties {

	private Hashtable<EventPropertyKey<?>, Object> properties = new Hashtable<>();

	private boolean isLocked = false;

	@Override
	@SuppressWarnings("unchecked")
	public <T> T put(EventPropertyKey<T> key, T value) {
		this.checkState();
		return (T) this.properties.put(key, value);
	}

	@Override
	public <T> boolean containsProperty(EventPropertyKey<T> property) {
		return this.properties.containsKey(property);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getProperty(EventPropertyKey<T> property) {
		return (T) this.properties.get(property);
	}

	@Override
	public void clear() {
		this.checkState();
		this.properties.clear();
	}

	private void checkState() throws IllegalStateException {
		if (this.isLocked) throw new IllegalStateException("The WriteableEventProperties were locked");
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

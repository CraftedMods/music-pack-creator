package craftedMods.eventManager.base;

import craftedMods.eventManager.api.WriteableEventProperties;
import craftedMods.utils.data.DefaultTypedProperties;
import craftedMods.utils.data.TypedPropertyKey;

public class DefaultWriteableEventProperties extends DefaultTypedProperties implements WriteableEventProperties {

	private boolean isLocked = false;

	@Override
	public <T> T put(TypedPropertyKey<T> key, T value) {
		this.checkState();
		return super.put(key, value);
	}

	@Override
	public void clear() {
		this.checkState();
		super.clear();
	}

	private void checkState() throws IllegalStateException {
		if (this.isLocked)
			throw new IllegalStateException("The writeable event properties were locked");
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

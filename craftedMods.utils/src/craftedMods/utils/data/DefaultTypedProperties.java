package craftedMods.utils.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultTypedProperties implements LockableTypedProperties {

	protected final Map<TypedPropertyKey<?>, Object> properties = Collections.synchronizedMap(new HashMap<>());
	
	protected boolean isLocked = false;

	@Override
	@SuppressWarnings("unchecked")
	public <T> T put(TypedPropertyKey<T> key, T value) {
		Objects.requireNonNull(key);
		this.checkState ();
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
	    this.checkState ();
		this.properties.clear();
	}
	
	@Override
    public boolean isLocked() {
        return this.isLocked;
    }

    @Override
    public boolean lock() {
        return this.isLocked ? false : (this.isLocked = true);
    }
	
	private void checkState() throws IllegalStateException {
        if (this.isLocked)
            throw new IllegalStateException("The writeable event properties were locked");
    }

}

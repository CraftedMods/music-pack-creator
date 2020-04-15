package craftedMods.utils.data;

import org.osgi.annotation.versioning.ProviderType;

/**
 * By default those behave like writable typed properties, however upon calling
 * the lock function they can be made read-only - invoking any function which
 * would modify those properties will then raise an exception. This is useful
 * when returning instances of some writable typed properties as the readable
 * typed properties type, as it additionally assures that even through
 * casting them back to the writable type doesn't make them modifiable.
 * 
 * @author CraftedMods
 *
 */
@ProviderType
public interface LockableTypedProperties extends TypedProperties
{

    public boolean isLocked ();

    public boolean lock ();

}

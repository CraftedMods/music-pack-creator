package craftedMods.utils.function;

@FunctionalInterface
public interface FailableSupplier<T, E extends Throwable> {

	public T get() throws E;

}

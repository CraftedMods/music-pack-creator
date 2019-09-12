package craftedMods.utils.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class CollectionUtils {

	public static <T> NonNullSet<T> createNonNullHashSet(Collection<? extends T> initialValues) {
		return createNonNullSetWithInternal(new HashSet<>(initialValues));
	}

	public static <T> NonNullSet<T> createNonNullHashSet() {
		return createNonNullSetWithInternal(new HashSet<>());
	}

	public static <T> NonNullSet<T> createNonNullLinkedHashSet(Collection<? extends T> initialValues) {
		return createNonNullSetWithInternal(new LinkedHashSet<>(initialValues));
	}

	public static <T> NonNullSet<T> createNonNullLinkedHashSet() {
		return createNonNullSetWithInternal(new LinkedHashSet<>());
	}

	public static <T> NonNullSet<T> createNonNullSetWithInternal(Set<T> internal) {
		Objects.requireNonNull(internal);

		return new NonNullSetWrapper<>(internal);
	}

	private static class NonNullSetWrapper<T> implements NonNullSet<T> {

		private final Set<T> internal;

		public NonNullSetWrapper(Set<T> internal) {
			internal.forEach(Objects::requireNonNull);

			this.internal = internal;
		}

		@Override
		public int size() {
			return internal.size();
		}

		@Override
		public boolean isEmpty() {
			return internal.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return internal.contains(o);
		}

		@Override
		public Iterator<T> iterator() {
			return internal.iterator();
		}

		@Override
		public Object[] toArray() {
			return internal.toArray();
		}

		@Override
		public <U> U[] toArray(U[] a) {
			return internal.toArray(a);
		}

		@Override
		public boolean add(T e) {
			Objects.requireNonNull(e);

			return internal.add(e);
		}

		@Override
		public boolean remove(Object o) {
			return internal.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return internal.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends T> c) {
			c.forEach(Objects::requireNonNull);
			return internal.addAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return internal.retainAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return internal.removeAll(c);
		}

		@Override
		public void clear() {
			internal.clear();
		}

	}

}

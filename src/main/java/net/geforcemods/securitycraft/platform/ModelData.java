package net.geforcemods.securitycraft.platform;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/** Immutable values captured by Fabric's block entity render-data API. */
public final class ModelData {
	public static final ModelData EMPTY = new ModelData(Map.of());
	private final Map<ModelProperty<?>, Object> values;

	private ModelData(Map<ModelProperty<?>, Object> values) {
		this.values = Collections.unmodifiableMap(new IdentityHashMap<>(values));
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ModelProperty<T> property) {
		return (T) values.get(property);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final Map<ModelProperty<?>, Object> values = new IdentityHashMap<>();

		public <T> Builder with(ModelProperty<T> property, T value) {
			if (value == null)
				values.remove(property);
			else
				values.put(property, value);
			return this;
		}

		public ModelData build() {
			return values.isEmpty() ? EMPTY : new ModelData(values);
		}
	}
}

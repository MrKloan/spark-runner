package spark.runner;

import java.util.HashMap;
import java.util.Map;

final class SparkComponentStore {
	
	private static final Map<Class<?>, Object> sparkComponents = new HashMap<>();
	
	static <T> T put(final T component) {
		sparkComponents.put(component.getClass(), component);
		return component;
	}
	
	@SuppressWarnings("unchecked")
	static <T> T get(final Class<T> componentClass) {
		return (T) sparkComponents.get(componentClass);
	}
}

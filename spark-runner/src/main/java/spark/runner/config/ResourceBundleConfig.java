package spark.runner.config;

import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

public class ResourceBundleConfig implements ApplicationConfig {
	
	private final ResourceBundle resourceBundle;
	
	private ResourceBundleConfig(final String resourceBundle) {
		this.resourceBundle = ResourceBundle.getBundle(resourceBundle);
	}
	
	public static ResourceBundleConfig of(final String resourceBundle) {
		return new ResourceBundleConfig(resourceBundle);
	}
	
	@Override
	public Optional<String> getString(final String key) {
		try {
			return Optional.of(resourceBundle.getString(key));
		}
		catch(final MissingResourceException e) {
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<Integer> getInt(final String key) {
		return getString(key).map(Integer::parseInt);
	}
	
	@Override
	public Optional<Long> getLong(final String key) {
		return getString(key).map(Long::parseLong);
	}
	
	@Override
	public Optional<Double> getDouble(final String key) {
		return getString(key).map(Double::parseDouble);
	}
}

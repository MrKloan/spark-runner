package spark.runner.config;

import java.util.Optional;

public interface ApplicationConfig {
	Optional<String> getString(final String key);
	Optional<Integer> getInt(final String key);
	Optional<Long> getLong(final String key);
	Optional<Double> getDouble(final String key);
}

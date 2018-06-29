package spark.runner.config.consumers;

import spark.Spark;
import spark.runner.config.ApplicationConfig;

import java.util.function.Consumer;

public final class StaticFilesConfigConsumer implements Consumer<ApplicationConfig> {
	
	private static final String STATIC_CLASSPATH = "spark.static.classpath";
	private static final String STATIC_EXTERNAL = "spark.static.external";
	private static final String STATIC_CACHE = "spark.static.cache";
	
	@Override
	public void accept(final ApplicationConfig applicationConfig) {
		applicationConfig.getString(STATIC_CLASSPATH).ifPresent(Spark.staticFiles::location);
		applicationConfig.getString(STATIC_EXTERNAL).ifPresent(Spark.staticFiles::externalLocation);
		applicationConfig.getInt(STATIC_CACHE).ifPresent(Spark.staticFiles::expireTime);
	}
}

package spark.runner.config;

import spark.runner.config.consumers.*;

import java.util.function.Consumer;

public final class SparkConfig {
	
	private final ApplicationConfig applicationConfig;
	private final Consumer<ApplicationConfig> configurationSteps;
	
	private SparkConfig(final ApplicationConfig applicationConfig) {
		this.applicationConfig = applicationConfig;
		
		this.configurationSteps = new PortConfigConsumer()
			.andThen(new ThreadsConfigConsumer())
			.andThen(new SslConfigConsumer())
			.andThen(new RouteConfigConsumer())
			.andThen(new StaticFilesConfigConsumer());
	}
	
	public static SparkConfig of(final ApplicationConfig applicationConfig) {
		return new SparkConfig(applicationConfig);
	}
	
	public void load() {
		configurationSteps.accept(applicationConfig);
	}
}

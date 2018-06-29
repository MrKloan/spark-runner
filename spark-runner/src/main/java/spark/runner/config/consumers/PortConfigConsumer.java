package spark.runner.config.consumers;

import spark.Spark;
import spark.runner.config.ApplicationConfig;

import java.util.function.Consumer;

public final class PortConfigConsumer implements Consumer<ApplicationConfig> {
	
	private static final String PORT = "spark.port";
	
	@Override
	public void accept(final ApplicationConfig applicationConfig) {
		applicationConfig.getInt(PORT).ifPresent(Spark::port);
	}
}

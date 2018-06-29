package spark.runner.config.consumers;

import spark.Spark;
import spark.runner.config.ApplicationConfig;

import java.util.Optional;
import java.util.function.Consumer;

public final class ThreadsConfigConsumer implements Consumer<ApplicationConfig> {
	
	private static final String THREADS_MAX = "spark.threads.max";
	private static final String THREADS_MIN = "spark.threads.min";
	private static final String THREADS_TIMEOUT = "spark.threads.timeout";
	
	@Override
	public void accept(final ApplicationConfig applicationConfig) {
		final Optional<Integer> max = applicationConfig.getInt(THREADS_MAX);
		
		if(!max.isPresent())
			return;
		
		final Optional<Integer> min = applicationConfig.getInt(THREADS_MIN);
		final Optional<Integer> timeout = applicationConfig.getInt(THREADS_TIMEOUT);
		
		if(min.isPresent() && timeout.isPresent())
			Spark.threadPool(max.get(), min.get(), timeout.get());
		else
			Spark.threadPool(max.get());
	}
}

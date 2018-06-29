package spark.runner.config.consumers;

import spark.route.RouteOverview;
import spark.runner.config.ApplicationConfig;

import java.util.function.Consumer;

public final class RouteConfigConsumer implements Consumer<ApplicationConfig> {
	
	private static final String ROUTE_OVERVIEW = "spark.route.overview";
	
	@Override
	public void accept(final ApplicationConfig applicationConfig) {
		applicationConfig.getString(ROUTE_OVERVIEW).ifPresent(RouteOverview::enableRouteOverview);
	}
}

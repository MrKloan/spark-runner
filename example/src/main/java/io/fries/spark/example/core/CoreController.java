package io.fries.spark.example.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.runner.annotations.Controller;
import spark.runner.annotations.ExceptionHandler;
import spark.runner.annotations.Filter;
import spark.runner.annotations.Inject;

import static spark.runner.annotations.Filter.Type.AFTER;

@Controller
public class CoreController {
	
	private static final Logger logger = LoggerFactory.getLogger(CoreController.class);
	
	@Inject
	private ApiParser parser;
	
	@Filter(type = AFTER)
	private void gzipFilter(final Request req, final Response res) {
		res.header("Content-Encoding", "gzip");
	}
	
	@ExceptionHandler(Exception.class)
	private void genericExceptionHandler(final Exception ex, final Request req, final Response res) {
		final ApiResponse response = new ApiResponse.Builder(req, res).status(500).data("An unexpected error occured... :(").build();
		res.body(parser.json(response));
		
		logger.error(ex.getMessage(), ex);
	}
}

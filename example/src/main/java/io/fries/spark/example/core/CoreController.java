package io.fries.spark.example.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.runner.annotations.SparkController;
import spark.runner.annotations.SparkException;
import spark.runner.annotations.SparkFilter;
import spark.runner.annotations.SparkInject;

import static spark.runner.annotations.SparkFilter.Filter.AFTER;

@SparkController
public class CoreController {
	
	private static final Logger logger = LoggerFactory.getLogger(CoreController.class);
	
	@SparkInject
	private ApiParser parser;
	
	@SparkFilter(filter = AFTER)
	private void gzipFilter(final Request req, final Response res) {
		res.header("Content-Encoding", "gzip");
	}
	
	@SparkException(Exception.class)
	private void genericExceptionHandler(final Exception ex, final Request req, final Response res) {
		final ApiResponse response = new ApiResponse.Builder(req, res).status(500).data("An unexpected error occured... :(").build();
		res.body(parser.json(response));
		
		logger.error(ex.getMessage(), ex);
	}
}

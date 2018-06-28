package io.fries.spark.example.other;

import io.fries.spark.example.core.ApiResponse;
import spark.Request;
import spark.Response;
import spark.runner.annotations.SparkController;
import spark.runner.annotations.SparkInject;
import spark.runner.annotations.SparkRoute;

@SparkController(path = "/other")
public class OtherController {
	
	@SparkInject
	private OtherService otherService;
	
	@SparkRoute(path = "")
	private ApiResponse getOtherResponse(final Request req, final Response res) {
		final String msg = otherService.other();
		return new ApiResponse.Builder(req, res).data(msg).build();
	}
}

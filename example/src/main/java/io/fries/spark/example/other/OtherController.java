package io.fries.spark.example.other;

import io.fries.spark.example.core.ApiResponse;
import spark.Request;
import spark.Response;
import spark.runner.annotations.Controller;
import spark.runner.annotations.Inject;
import spark.runner.annotations.Route;

@Controller(path = "/other")
public class OtherController {
	
	@Inject
	private OtherService otherService;
	
	@Route(path = "")
	private ApiResponse getOtherResponse(final Request req, final Response res) {
		final String msg = otherService.other();
		return new ApiResponse.Builder(req, res).data(msg).build();
	}
}

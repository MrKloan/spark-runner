package io.fries.spark.example.hello;

import io.fries.spark.example.core.ApiParser;
import io.fries.spark.example.core.ApiResponse;
import spark.Request;
import spark.Response;
import spark.runner.annotations.SparkController;
import spark.runner.annotations.SparkFilter;
import spark.runner.annotations.SparkInject;
import spark.runner.annotations.SparkRoute;

import static spark.runner.annotations.SparkFilter.Filter.AFTER;

@SparkController(path = "/hello")
public class HelloController {
	
	@SparkInject
	private ApiParser parser;
	
	@SparkInject
	private HelloService helloService;
	
	@SparkRoute(path = "")
	private ApiResponse getHelloMessage(final Request req, final Response res) {
		return new ApiResponse.Builder(req, res).data(helloService.hello()).build();
	}
	
	@SparkRoute(path = "/:name")
	private ApiResponse getCustomMessage(final Request req, final Response res) {
		return new ApiResponse.Builder(req, res).data(helloService.hello(req.params("name"))).build();
	}
	
	@SparkFilter(filter = AFTER, path = "/*")
	private void afterCustomHello(final Request req, final Response res) {
		final ApiResponse initialResponse = parser.object(res.body(), ApiResponse.class);
		final String data = initialResponse.getData() + " It was nice meeting you. :)";
		final ApiResponse newResponse = new ApiResponse.Builder(req, res).data(data).build();
		
		res.body(parser.json(newResponse));
	}
}

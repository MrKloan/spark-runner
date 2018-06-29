package io.fries.spark.example.hello;

import io.fries.spark.example.core.ApiParser;
import io.fries.spark.example.core.ApiResponse;
import spark.Request;
import spark.Response;
import spark.runner.annotations.Controller;
import spark.runner.annotations.Filter;
import spark.runner.annotations.Inject;
import spark.runner.annotations.Route;

import static spark.runner.annotations.Filter.Type.AFTER;

@Controller(path = "/hello")
public class HelloController {
	
	@Inject
	private ApiParser parser;
	
	@Inject
	private HelloService helloService;
	
	@Route(path = "")
	private ApiResponse getHelloMessage(final Request req, final Response res) {
		return new ApiResponse.Builder(req, res).data(helloService.hello()).build();
	}
	
	@Route(path = "/:name")
	private ApiResponse getCustomMessage(final Request req, final Response res) {
		return new ApiResponse.Builder(req, res).data(helloService.hello(req.params("name"))).build();
	}
	
	@Filter(type = AFTER, path = "/*")
	private void afterCustomHello(final Request req, final Response res) {
		final ApiResponse initialResponse = parser.object(res.body(), ApiResponse.class);
		final String data = initialResponse.getData() + " It was nice meeting you. :)";
		final ApiResponse newResponse = new ApiResponse.Builder(req, res).data(data).build();
		
		res.body(parser.json(newResponse));
	}
}

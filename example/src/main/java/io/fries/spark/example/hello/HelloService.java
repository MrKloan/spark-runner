package io.fries.spark.example.hello;

import spark.runner.annotations.Component;

@Component
public class HelloService {

	String hello() {
		return hello("World");
	}

	String hello(final String name) {
		return "Hello " + name + "!";
	}
}

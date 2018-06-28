package io.fries.spark.example.hello;

import spark.runner.annotations.SparkComponent;

@SparkComponent
public class HelloService {

	String hello() {
		return hello("World");
	}

	String hello(final String name) {
		return "Hello " + name + "!";
	}
}

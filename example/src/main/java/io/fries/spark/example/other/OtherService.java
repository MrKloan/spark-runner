package io.fries.spark.example.other;

import spark.runner.annotations.Component;

@Component
public class OtherService {
	
	String other() {
		return "Because you know?";
	}
}

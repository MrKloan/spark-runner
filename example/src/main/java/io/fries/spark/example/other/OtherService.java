package io.fries.spark.example.other;

import spark.runner.annotations.SparkComponent;

@SparkComponent
public class OtherService {
	
	String other() {
		return "Because you know?";
	}
}

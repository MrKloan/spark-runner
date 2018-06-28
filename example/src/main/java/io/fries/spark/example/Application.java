package io.fries.spark.example;

import spark.runner.SparkRunner;
import spark.runner.annotations.SparkApplication;

@SparkApplication
public class Application {
	
	public static void main(String[] args) {
		SparkRunner.startApplication(Application.class);
	}
}

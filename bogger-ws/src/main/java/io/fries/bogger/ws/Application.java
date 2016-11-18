package io.fries.bogger.ws;

import com.google.gson.Gson;
import spark.annotations.SparkApplication;
import spark.annotations.SparkRunner;

@SparkApplication
public class Application {

	private Gson gson;

	public Application() {
		this.gson = new Gson();
	}

	public String json(Object o) {
		return gson.toJson(o);
	}

	public static void main(String[] args) {
		SparkRunner.startApplication(Application.class);
	}
}

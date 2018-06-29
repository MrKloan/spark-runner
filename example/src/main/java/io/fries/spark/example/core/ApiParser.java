package io.fries.spark.example.core;

import com.google.gson.Gson;
import spark.runner.annotations.Component;

@Component
public class ApiParser {
	
	private final Gson gson;
	
	public ApiParser() {
		this.gson = new Gson();
	}
	
	public String json(final Object obj) {
		return gson.toJson(obj);
	}
	
	public <T> T object(final String json, final Class<T> c) {
		return gson.fromJson(json, c);
	}
}

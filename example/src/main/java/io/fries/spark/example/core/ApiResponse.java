package io.fries.spark.example.core;

import spark.Request;
import spark.Response;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ApiResponse implements Serializable {
	
	private static final String APPLICATION_JSON = "application/json";
	
	private final Object data;
	private final Map<String, String> links;
	private final long timestamp;
	
	private ApiResponse(final Builder builder) {
		this.data = builder.data;
		this.links = builder.links;
		this.timestamp = new Date().getTime();
	}
	
	public Object getData() {
		return data;
	}
	
	public Map<String, String> getLinks() {
		return links;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public static final class Builder {
		
		private final Request req;
		private final Response res;
		private final Map<String, String> links;
		private Object data;
		
		public Builder(final Request req, final Response res) {
			this.req = req;
			this.res = res;
			
			this.links = new HashMap<>();
			this.type(APPLICATION_JSON);
		}
		
		public Builder status(final int status) {
			this.res.status(status);
			return this;
		}
		
		public Builder header(final String header, final String value) {
			if(header == null || header.isEmpty() || value == null || value.isEmpty())
				throw new IllegalArgumentException("Header name or value cannot be null nor empty.");
			
			this.res.header(header, value);
			return this;
		}
		
		public Builder type(final String contentType) {
			if(contentType == null || contentType.isEmpty())
				throw new IllegalArgumentException("ContentType cannot be null nor empty.");
			
			this.res.type(contentType);
			return this;
		}
		
		public Builder data(final Object data) {
			this.data = data;
			return this;
		}
		
		public Builder link(final String name, final String href) {
			this.links.put(name, href);
			return this;
		}
		
		public ApiResponse build() {
			return new ApiResponse(this);
		}
	}
}

package spark.runner.annotations;

import spark.ResponseTransformer;
import spark.runner.transformers.JsonTransformer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {
	
	String path();
	String accept() default "application/json";
	String contentType() default "application/json";
	HttpMethod method() default HttpMethod.GET;
	Class<? extends ResponseTransformer> transformer() default JsonTransformer.class;
	
	enum HttpMethod {GET, POST, PUT, PATCH, DELETE, HEAD, TRACE, CONNECT, OPTIONS}
}

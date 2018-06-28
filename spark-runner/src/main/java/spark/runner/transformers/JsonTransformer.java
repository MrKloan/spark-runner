package spark.runner.transformers;

import com.google.gson.Gson;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

	private Gson gson;
	
	public JsonTransformer() {
		this.gson = new Gson();
	}
	
	@Override
	public String render(Object model) {
		return gson.toJson(model);
	}
}

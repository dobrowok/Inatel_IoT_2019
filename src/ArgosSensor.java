import java.util.Arrays;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/* 
 * https://dzone.com/articles/introduction-to-json-with-java
 * Json Para c++: https://github.com/nlohmann/json#examples  
 * 	
 */



public class ArgosSensor {
	
	ArgosSensor() {
		String json = "{\"foo\":1, \"bar\":\"baz\"}";
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		Object o = null;
		try {
			o = engine.eval(String.format("JSON.parse('%s')", json));
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) o;
		System.out.println(Arrays.toString(map.entrySet().toArray()));
		// [foo=1, bar=baz]
	}
	
	/*
	public JSONObject  getJson() {
		JSONObject jo = new JSONObject();
		jo.put("name", "jon doe");
		jo.put("age", "22");
		jo.put("city", "chicago");
	} */
	
	/*
	 private static void buildJsonUsingObjectModelApi() {
		  System.out.println("Json Building using Object Model API");
		  JsonArray jsonArray =
		          //Create an Array Builder to build an JSON Array
		          Json.createArrayBuilder()
		            .add(Json.createObjectBuilder()//Create an Object builder to build JSON Object
		              .add("parentid", 23424900)
		              .add("name","Jackson")
		              .add("url", "http://where.yahooapis.com/v1/place/2428184")
		              .add("placeType", Json.createObjectBuilder()//Another nested JSON Object
		                    .add("name", "Town")
		                    .add("code",7)
		                  )
		              .add("woeid", 116545)
		              .build()//The JSON Object completely constructed.
		            )
		            .add(Json.createObjectBuilder()//Another object builder to build JSON Object.
		              .add("name","Mexico City")
		              .add("url", "http://where.yahooapis.com/v1/place/116545")
		              .add("placeType", Json.createObjectBuilder()
		                    .add("name", "Town")
		                    .add("code",7)
		                  )
		              .add("parentid", 23424977)
		              .add("woeid", 2428184)
		              .build()
		             )
		            .build();
		  StringWriter writer = new StringWriter();

		  //Extracting the JSON data from the JSON object tree into the string.
		  Json.createWriter(writer).writeArray(jsonArray);

		  System.out.println(writer.toString());

		}
	 */
}

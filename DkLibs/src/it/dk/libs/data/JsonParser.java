package it.dk.libs.data;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {
	
	private Gson gson;
	
	static JsonParser mInstance;

	static {
		mInstance = new JsonParser();
	}

	public static JsonParser getInstance() {
		return mInstance;
	}

	private JsonParser() {
		gson = new Gson();
	}
	
	/**
	 * Simple serialize obj to json
	 * @param obj
	 * @return
	 */
	public String serializeObject(Object obj){
		return gson.toJson(obj);
	}
	
	/**
	 * Create an object of class T from JSON
	 * @param json
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T deserialize(String json, Class<?> obj){
		return (T) gson.fromJson(json, obj);
	}

	/**
	 * Simply get a JSONObject from string
	 * @param json
	 * @return
	 */
	public JSONObject getJson(String json) {
		try {
			return new JSONObject(json);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public JSONArray getJsonArray(String json){
		try {
			return new JSONArray(json);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
	

}

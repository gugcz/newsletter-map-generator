package cz.gug.newsletter;

import org.json.JSONObject;

public class JsonUtils {

	private JsonUtils() {}

	public static boolean isNullOrEmpty(JSONObject jsonObject, String key){
		return jsonObject.isNull(key) || jsonObject.getString(key).length() ==0;
	}
}

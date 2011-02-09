package se.jimlar.intranet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class APIResponseParser {
    public List<Employee> parseEmployees(String data) {
        List<Employee> result = new ArrayList<Employee>();

        try {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                result.add(new Employee(object.getLong("id"),
                                        object.getString("first_name"),
                                        object.getString("last_name"),
                                        object.getString("mobile"),
                                        object.getString("thumbnail_url"),
                                        object.getString("email")));
            }

        } catch (JSONException e) {
            throw new RuntimeException("Could not parse data", e);
        }
        return result;
    }
}

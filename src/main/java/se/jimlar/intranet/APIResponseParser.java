package se.jimlar.intranet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.jimlar.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class APIResponseParser {
    private static final Logger LOG = new Logger(APIResponseParser.class);
    private SimpleDateFormat format;

    public APIResponseParser() {
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public List<Employee> parseEmployees(String data) {
        List<Employee> result = new ArrayList<Employee>();

        try {
            JSONArray array = new JSONArray(data);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                long employeeId = object.getLong("id");

                LOG.debug("Parsing employee " + employeeId);
                JSONObject status = getJSONObject(object, "status");

                String statusMessage = null;
                long statusTimeStamp = 0;
                if (status != null) {
                    statusMessage = status.getString("description");
                    statusTimeStamp = parseStatusTimeStamp(status);
                }

                result.add(new Employee(employeeId,
                                        object.getString("first_name"),
                                        object.getString("last_name"),
                                        object.getString("mobile"),
                                        object.getString("thumbnail_url"),
                                        object.getString("email"),
                                        object.getString("title"),
                                        statusMessage,
                                        statusTimeStamp));
            }

        } catch (JSONException e) {
            throw new RuntimeException("Could not parse data", e);
        }
        return result;
    }

    private JSONObject getJSONObject(JSONObject object, String key) throws JSONException {
        if (!object.has(key)) {
            return null;
        }
        Object o = object.get(key);
        if (o == null || JSONObject.NULL.equals(o) || "".equals(o)) {
            return null;
        }

        return object.getJSONObject(key);
    }

    private long parseStatusTimeStamp(JSONObject status) throws JSONException {
        try {
            return format.parse(status.getString("created_on")).getTime();
        } catch (ParseException e) {
            LOG.warn("Cound not parse status timestamp: " + status.getString("created_on"), e);
            return 0;
        }
    }
}

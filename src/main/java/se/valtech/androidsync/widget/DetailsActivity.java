package se.valtech.androidsync.widget;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import se.valtech.androidsync.Logger;
import se.valtech.androidsync.R;
import se.valtech.androidsync.storage.StatusReader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailsActivity extends Activity {
    private static final Logger LOGGER = new Logger(DetailsActivity.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.info("create");

        setContentView(R.layout.status_appwidget_details);

        ListView lv = (ListView) findViewById(R.id.details_listview);

        // Column to view mapping
        String[] from = new String[]{"time", "status"};
        int[] to = new int[]{R.id.details_item_time, R.id.details_item_status};

        LOGGER.debug("Reading statuses");
        StatusReader reader = new StatusReader(this.getContentResolver());
        List<StatusReader.Status> statuses = reader.getLatestStatuses(100);

        List<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
        for (StatusReader.Status status : statuses) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("time", "" + DATE_FORMAT.format(status.when));
            map.put("status", status.employee.getName() + " " + status.text);
            listItems.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.details_item, from, to);
        lv.setAdapter(adapter);
    }
}

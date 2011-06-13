package se.valtech.androidsync.widget;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
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
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMM");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LOGGER.info("create");

        setTitle("Valtech Intranet");
        setContentView(R.layout.status_appwidget_details);

        LoadStatusTask t = new LoadStatusTask(this);
        t.execute();
    }

    

    public class AlternatingRowColorAdapter extends SimpleAdapter {
	    private int[] colors = new int[] { 0x30FF0000, 0x300000FF };

	    public AlternatingRowColorAdapter(Context context, List<Map<String, String>> items, int resource, String[] from, int[] to) {
	        super(context, items, resource, from, to);
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	      View view = super.getView(position, convertView, parent);
	      int colorPos = position % colors.length;
	      view.setBackgroundColor(colors[colorPos]);
	      return view;
	    }
	}

    private class LoadStatusTask extends AsyncTask<String, Void, Boolean> {
        private final Logger LOGGER = new Logger(LoadStatusTask.class);

        ProgressDialog progressDialog;
        private Handler progressMessageHandler;
        private ContentResolver contentResolver;
        private List<StatusReader.Status> statuses;
        private Context context;

        public LoadStatusTask(Context context) {
            LOGGER.info("create");
            this.progressDialog = ProgressDialog.show(context, "", "Loading statuses...", true, false);
            this.progressMessageHandler = new Handler();
            this.context = context;
            this.contentResolver = context.getContentResolver();
        }

        @Override
        public Boolean doInBackground(String... params) {
            LOGGER.debug("Reading statuses");
            StatusReader reader = new StatusReader(contentResolver);
            statuses = reader.getLatestStatuses(50);
            return true;
        }

        @Override
		public void onPostExecute(Boolean result) {
            progressMessageHandler.post(new Runnable() {
                public void run() {
                    ListView lv = (ListView) findViewById(R.id.details_listview);

                    // Column to view mapping
                    String[] from = new String[]{"time", "status", "date"};
                    int[] to = new int[]{R.id.details_item_time, R.id.details_item_status, R.id.details_item_date};

                    List<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
                    for (StatusReader.Status status : statuses) {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("time", "" + TIME_FORMAT.format(status.when));
                        map.put("date", "" + DATE_FORMAT.format(status.when));
                        map.put("status", status.employeeName + " " + status.text);
                        listItems.add(map);
                    }

                    SimpleAdapter adapter = new AlternatingRowColorAdapter(context, listItems, R.layout.details_item, from, to);
                    lv.setAdapter(adapter);
                }
            });
            progressDialog.dismiss();
		}
    }
}

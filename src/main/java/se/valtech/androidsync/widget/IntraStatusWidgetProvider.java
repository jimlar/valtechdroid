package se.valtech.androidsync.widget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import se.valtech.androidsync.Logger;
import se.valtech.androidsync.R;
import se.valtech.androidsync.storage.StatusReader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class IntraStatusWidgetProvider extends AppWidgetProvider {
    private static final Logger LOGGER = new Logger(IntraStatusWidgetProvider.class);

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.status_appwidget);
            setupWidget(context, views);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private void setupWidget(Context context, RemoteViews views) {

        // Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, Activity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        views.setOnClickPendingIntent(R.id.widgetpanel, pendingIntent);

        LOGGER.debug("Reading statuses");
        StatusReader reader = new StatusReader(context.getContentResolver());
        List<StatusReader.Status> statuses = reader.getLatestStatuses(4);

        LOGGER.debug("Updating " + statuses.size() + " widget statuses");
        updateStatusLine(views, statuses, R.id.statusname0, R.id.statustext0, 0);
        updateStatusLine(views, statuses, R.id.statusname1, R.id.statustext1, 1);
        updateStatusLine(views, statuses, R.id.statusname2, R.id.statustext2, 2);
    }

    private void updateStatusLine(RemoteViews views, List<StatusReader.Status> statuses, int nameViewId, int textViewId, int statusIndex) {
        if (statuses.size() > statusIndex) {
            StatusReader.Status status = statuses.get(statusIndex);
            views.setTextViewText(nameViewId, formatDate(status.when));
            views.setTextViewText(textViewId, status.employee.getFirstName() + " " + status.employee.getLastName() + " " + status.text);
        }
    }

    private String formatDate(Date date) {
        Date now = new Date();
        if (now.getDay() == date.getDay()) {
            return new SimpleDateFormat("HH:mm").format(date);
        }
        return new SimpleDateFormat("dd MMM").format(date);
    }
}

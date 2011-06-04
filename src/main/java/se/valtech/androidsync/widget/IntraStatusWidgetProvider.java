package se.valtech.androidsync.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;
import se.valtech.androidsync.Logger;
import se.valtech.androidsync.R;
import se.valtech.androidsync.storage.StatusReader;

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
/*
        // Create an Intent to launch ExampleActivity
        Intent intent = new Intent(context, Activity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Get the layout for the App Widget and attach an on-click listener
        // to the button
        views.setOnClickPendingIntent(R.id.widgettext, pendingIntent);
*/

        LOGGER.debug("Reading statuses");
        StatusReader reader = new StatusReader(context.getContentResolver());
        List<StatusReader.Status> statuses = reader.getLatestStatuses(4);

        LOGGER.debug("Updating " + statuses.size() + " widget statuses");
        updateStatusLine(views, statuses, R.id.statuswidgettext1, 0);
        views.setTextViewText(R.id.statuswidgettext2, statuses.get(1).status);
        views.setTextViewText(R.id.statuswidgettext3, statuses.get(2).status);
        views.setTextViewText(R.id.statuswidgettext4, statuses.get(3).status);


    }

    private void updateStatusLine(RemoteViews views, List<StatusReader.Status> statuses, int line, int index) {
        if (statuses.size() > index) {
            views.setTextViewText(line, statuses.get(index).status);
        }
    }
}

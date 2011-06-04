package se.valtech.androidsync.widget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import se.valtech.androidsync.R;

public class IntraStatusWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
           final int N = appWidgetIds.length;

           // Perform this loop procedure for each App Widget that belongs to this provider
           for (int i=0; i<N; i++) {
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
        views.setTextViewText(R.id.statuswidgettext, "This is status!");

    }
}

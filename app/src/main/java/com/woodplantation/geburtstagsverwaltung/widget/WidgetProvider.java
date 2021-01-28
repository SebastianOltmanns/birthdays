package com.woodplantation.geburtstagsverwaltung.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.woodplantation.geburtstagsverwaltung.R;

/**
 * Created by Sebu on 19.10.2019.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
public class WidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
			Intent intent = new Intent(context, WidgetService.class);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
			views.setRemoteAdapter(R.id.data_list_view, intent);
			views.setEmptyView(R.id.data_list_view, R.id.widget_textview_nothing_to_show);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

	@Override
	public void onEnabled(Context context) {
		WidgetAlarmReceiver.createNextAlarm(context);
	}

	@Override
	public void onDisabled(Context context) {
		Intent intent = new Intent(context, WidgetAlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.cancel(pendingIntent);
	}

}

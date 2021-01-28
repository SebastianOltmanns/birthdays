package com.woodplantation.geburtstagsverwaltung.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

/**
 * Created by Sebu on 22.10.2019.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
public class WidgetAlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		createNextAlarm(context);
		WidgetService.notifyDataChanged(context);
	}

	public static void createNextAlarm(Context context) {
		Intent intent = new Intent(context, WidgetAlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		Calendar when = Calendar.getInstance();
		when.add(Calendar.DAY_OF_YEAR, 1);
		when.set(Calendar.HOUR_OF_DAY, 0);
		when.set(Calendar.MINUTE, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			am.setExactAndAllowWhileIdle(AlarmManager.RTC, when.getTimeInMillis(), pendingIntent);
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			am.setExact(AlarmManager.RTC, when.getTimeInMillis(), pendingIntent);
		} else {
			am.set(AlarmManager.RTC, when.getTimeInMillis(), pendingIntent);
		}
	}
}

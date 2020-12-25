package com.woodplantation.geburtstagsverwaltung.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.util.IntentCodes;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by Sebu on 29.10.2019.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
public class AlarmCreator {

	public enum ChangeType {
		UPDATE, CANCEL, CREATE, NOTHING
	}

	private static boolean allNothing(ChangeType[] changeTypes) {
		boolean allNothing = true;
		for (ChangeType ct : changeTypes) {
			if (ct != ChangeType.NOTHING) {
				allNothing = false;
				break;
			}
		}
		return allNothing;
	}

	public static void changeAlarms(Context context, ChangeType[] changeTypes, MyPreferences preferences) {
		Log.d("alarmcreator","change alarms!" + Arrays.toString(changeTypes));
		if (allNothing(changeTypes)) {
			return;
		}
		int[] clocks = preferences.getClocks();
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		for (int i = 0; i < changeTypes.length; i++) {
			ChangeType changeType = changeTypes[i];
			if (changeType == ChangeType.NOTHING) {
				continue;
			} else {
				Intent intent = new Intent(context, AlarmReceiver.class);
				intent.putExtra(IntentCodes.WHICH, i);
				Log.d("alamcreator","putting intent extra" + i + " which code:" + IntentCodes.WHICH);
				if (changeType == ChangeType.CANCEL) {
					PendingIntent pi = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
					am.cancel(pi);
				} else {
					int flag = changeType == ChangeType.UPDATE ? PendingIntent.FLAG_CANCEL_CURRENT : 0;
					PendingIntent pi = PendingIntent.getBroadcast(context, i, intent, flag);
					Calendar when = Calendar.getInstance();
					int h = clocks[i] / 60;
					int m = clocks[i] % 60;
					if ((when.get(Calendar.HOUR_OF_DAY) > h) || (when.get(Calendar.HOUR_OF_DAY) == h && when.get(Calendar.MINUTE) >= m)) {
						// the time point today already happened. add one day
						when.add(Calendar.DAY_OF_YEAR, 1);
					}
					when.set(Calendar.MINUTE, m);
					when.set(Calendar.HOUR_OF_DAY, h);
					when.set(Calendar.SECOND, 0);
					// set the alarm
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pi);
					} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						am.setExact(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pi);
					} else {
						am.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), pi);
					}
				}
			}
		}
	}

	public static void createFromScratch(Context context, MyPreferences preferences) {
		Log.d("alarmcreator","create from scratch");
		// whatever we will create in the following, at first we delete all existing alarms
		changeAlarms(context, new ChangeType[]{ChangeType.CANCEL, ChangeType.CANCEL, ChangeType.CANCEL}, preferences);
		if (!preferences.getActive()) {
			return;
		}
		boolean[] which = preferences.getWhich();
		ChangeType[] changeTypes = {
				ChangeType.NOTHING,
				ChangeType.NOTHING,
				ChangeType.NOTHING
		};
		for (int i = 0; i < which.length; i++) {
			if (which[i]) {
				changeTypes[i] = ChangeType.CREATE;
			}
		}
		changeAlarms(context, changeTypes, preferences);
	}

	public static void preferencesChanged(Context context, Map<String, ?> oldPref, MyPreferences preferences) {
		Log.d("alarmcreator","preferences changed");

		// old settings
		boolean[] oldWhich = new boolean[3];
		oldWhich[0] = (Boolean) oldPref.get(context.getString(R.string.preferences_on_birthday_active));
		oldWhich[1] = (Boolean) oldPref.get(context.getString(R.string.preferences_one_day_before_active));
		oldWhich[2] = (Boolean) oldPref.get(context.getString(R.string.preferences_x_days_before_active));
		int[] oldClocks = new int[3];
		oldClocks[0] = (Integer) oldPref.get(context.getString(R.string.preferences_on_birthday_clock));
		oldClocks[1] = (Integer) oldPref.get(context.getString(R.string.preferences_one_day_before_clock));
		oldClocks[2] = (Integer) oldPref.get(context.getString(R.string.preferences_x_days_before_clock));
		int oldXDaysBeforeDays = (Integer) oldPref.get(context.getString(R.string.preferences_x_days_before_days));
		boolean oldActive = (Boolean) oldPref.get(context.getString(R.string.preferences_active));

		// new settings
		boolean[] newWhich = preferences.getWhich();
		int[] newClocks = preferences.getClocks();
		int newXDaysBeforeDays = preferences.getXDaysBeforeDays();
		boolean newActive = preferences.getActive();

		ChangeType[] changeTypes = {
				ChangeType.NOTHING, ChangeType.NOTHING, ChangeType.NOTHING
		};

		// compare settings
		if (oldActive != newActive) {
			// notifications got completely activated or deactivated
			if (newActive) {
				for (int i = 0; i < changeTypes.length; i++) {
					changeTypes[i] = ChangeType.CREATE;
				}
			} else {
				for (int i = 0; i < changeTypes.length; i++) {
					changeTypes[i] = ChangeType.CANCEL;
				}
			}
		} else {
			// compare single ones
			for (int i = 0; i < 3; i++) {

				boolean clockChanged = (oldClocks[i] != newClocks[i])
						|| (i == 2 && oldXDaysBeforeDays != newXDaysBeforeDays);

				if (!oldWhich[i] && newWhich[i]) {
					changeTypes[i] = ChangeType.CREATE;
				} else if (oldWhich[i] && !newWhich[i]) {
					changeTypes[i] = ChangeType.CANCEL;
				} else if (oldWhich[i] && newWhich[i] && clockChanged) {
					changeTypes[i] = ChangeType.UPDATE;
				}
			}
		}
		changeAlarms(context, changeTypes, preferences);
	}

}

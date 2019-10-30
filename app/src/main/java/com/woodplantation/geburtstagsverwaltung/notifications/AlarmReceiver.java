package com.woodplantation.geburtstagsverwaltung.notifications;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.activities.MainActivity;
import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.storage.StorageHandler;
import com.woodplantation.geburtstagsverwaltung.util.IntentCodes;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Created by Sebu on 29.10.2019.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("alarmreceiver","onreceive!");
		// get which alarm this is
		int which = intent.getIntExtra(IntentCodes.WHICH, -1);
		// get preferences
		MyPreferences notificationPreferences = new MyPreferences(context, MyPreferences.FILEPATH_NOTIFICATION);
		int xDaysBeforeDays = notificationPreferences.getXDaysBeforeDays();
		// iterate all data
		ArrayList<DataSet> dataList = new StorageHandler(context).loadData();
		Calendar now = Calendar.getInstance();
		for (DataSet data : dataList) {
			Calendar birthdayAlarm = data.getNextBirthday();
			if (which == 1) {
				birthdayAlarm.add(Calendar.DAY_OF_YEAR, -1);
			} else if (which == 2) {
				birthdayAlarm.add(Calendar.DAY_OF_YEAR, -xDaysBeforeDays);
			}
			Log.d("alarmreceiver","in loop. which: " + which);
			Log.d("alarmreceiver","in loop." + now);
			Log.d("alarmreceiver","in loop." + birthdayAlarm);

			if ((now.get(Calendar.YEAR) == birthdayAlarm.get(Calendar.YEAR))
					&& (now.get(Calendar.DAY_OF_YEAR) == birthdayAlarm.get(Calendar.DAY_OF_YEAR))) {
				// show notification
				createNotification(context, data, which, xDaysBeforeDays, notificationPreferences);
			}
		}

		if (which < 0 || which > 2) {
			AlarmCreator.createFromScratch(context);
		} else {
			// recreate the alarm in 24 hours
			AlarmCreator.ChangeType[] changeTypes = {
					AlarmCreator.ChangeType.NOTHING,
					AlarmCreator.ChangeType.NOTHING,
					AlarmCreator.ChangeType.NOTHING
			};
			changeTypes[which] = AlarmCreator.ChangeType.UPDATE;
			AlarmCreator.changeAlarms(context, changeTypes);
		}

	}

	private void createNotification(Context context, DataSet dataSet, int which, int xDaysBeforeDays, MyPreferences notificationPreferences) {
		Log.d("alarmreceiver","creating notification for" + dataSet.firstName + dataSet.lastName);
		String dayText;
		if ((which < 0) || (which > 2)) {
			// if which is something weird, we find out how many days are left on our own
			Calendar now = Calendar.getInstance();
			Calendar nextBirthday = dataSet.getNextBirthday();
			int diffDays = nextBirthday.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
			if (nextBirthday.get(Calendar.YEAR) > now.get(Calendar.YEAR)) {
				diffDays += now.getActualMaximum(Calendar.YEAR);
			}
			switch (diffDays) {
				case 0: {
					which = 0;
					break;
				}
				case 1: {
					which = 1;
					break;
				}
				default: {
					which = 2;
					break;
				}
			}
		}
		switch (which) {
			case 0: {
				dayText = context.getString(R.string.today);
				break;
			}
			case 1: {
				dayText = context.getString(R.string.tomorrow);
				break;
			}
			default: {
				dayText = context.getString(R.string.in_x_days, xDaysBeforeDays);
				break;
			}
		}

		String name = dataSet.firstName + " " + dataSet.lastName;
		int textResource;
		if (name.endsWith("s")) {
			textResource = R.string.content_text_name_ending_with_s;
		} else {
			textResource = R.string.content_text;
		}
		String text = context.getString(textResource, dayText, name);


		Intent notificationIntent = new Intent(context, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id));
		builder.setSmallIcon(R.drawable.ic_event_note);
		builder.setContentTitle(context.getString(R.string.content_title));
		builder.setContentText(text);
		builder.setContentIntent(pi);
		builder.setAutoCancel(true);
		builder.setCategory(NotificationCompat.CATEGORY_REMINDER);
		builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
		/*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			builder.setAllowSystemGeneratedContextualActions(false);
		}*/
		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
		notificationManager.notify(notificationPreferences.getNextNotificationId(), builder.build());

	}

}

package com.woodplantation.geburtstagsverwaltung.notifications;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.activities.MainActivity;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;
import com.woodplantation.geburtstagsverwaltung.util.DateUtil;
import com.woodplantation.geburtstagsverwaltung.util.IntentCodes;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Created by Sebu on 29.10.2019.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
@AndroidEntryPoint
public class AlarmReceiver extends BroadcastReceiver {

	@Inject
	MyPreferences preferences;
	@Inject
	Repository repository;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("alarmreceiver","onreceive!");
		// get which alarm this is
		int which = intent.getIntExtra(IntentCodes.WHICH, -1);
		// get preferences
		int xDaysBeforeDays = preferences.getXDaysBeforeDays();
		// iterate all data
		//TODO this receiver must be registered on a background thread
		List<Entry> dataList = repository.getDataSynchronously();
		LocalDate now = LocalDate.now();
		for (Entry data : dataList) {
			LocalDate birthdayAlarm = DateUtil.getNextBirthday(data.birthday);
			if (which == 1) {
				birthdayAlarm = birthdayAlarm.minus(1, ChronoUnit.DAYS);
			} else if (which == 2) {
				birthdayAlarm = birthdayAlarm.minus(xDaysBeforeDays, ChronoUnit.DAYS);
			}
			Log.d("alarmreceiver","in loop. which: " + which);
			Log.d("alarmreceiver","in loop." + now);
			Log.d("alarmreceiver","in loop." + birthdayAlarm);

			if (now.equals(birthdayAlarm)) {
				// show notification
				createNotification(context, data, which, xDaysBeforeDays, preferences);
			}
		}

		if (which < 0 || which > 2) {
			AlarmCreator.createFromScratch(context, preferences);
		} else {
			// recreate the alarm in 24 hours
			AlarmCreator.ChangeType[] changeTypes = {
					AlarmCreator.ChangeType.NOTHING,
					AlarmCreator.ChangeType.NOTHING,
					AlarmCreator.ChangeType.NOTHING
			};
			changeTypes[which] = AlarmCreator.ChangeType.UPDATE;
			AlarmCreator.changeAlarms(context, changeTypes, preferences);
		}

	}

	private void createNotification(Context context, Entry dataSet, int which, int xDaysBeforeDays, MyPreferences notificationPreferences) {
		Log.d("alarmreceiver","creating notification for" + dataSet.firstName + dataSet.lastName);
		String dayText;
		if ((which < 0) || (which > 2)) {
			// if which is something weird, we find out how many days are left on our own
			LocalDate now = LocalDate.now();
			LocalDate nextBirthday = DateUtil.getNextBirthday(dataSet.birthday);
			long diffDays = nextBirthday.toEpochDay() - now.toEpochDay();
			if (diffDays == 0) {
				which = 0;
			} else if (diffDays == 1) {
				which = 1;
			} else {
				which = 2;
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

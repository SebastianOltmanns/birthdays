package com.woodplantation.geburtstagsverwaltung.notifications;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import com.woodplantation.geburtstagsverwaltung.R;

/**
 * Created by Sebu on 19.10.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */

public class MyPreferences {

	private static final String FILEPATH = "notification_settings";

	private Context context;
	public SharedPreferences preferences;

	public MyPreferences(Context context) {
		this.context = context;
		this.preferences = context.getSharedPreferences(FILEPATH, Context.MODE_PRIVATE);
	}

	public boolean getActive() {
		return preferences
				.getBoolean(context.getString(R.string.preferences_active),
						context.getResources().getBoolean(R.bool.preferences_active));
	}

	public boolean getOnBirthdayActive() {
		return preferences
				.getBoolean(context.getString(R.string.preferences_on_birthday_active),
						context.getResources().getBoolean(R.bool.preferences_on_birthday_active));
	}

	public int getOnBirthdayClock() {
		return preferences
				.getInt(context.getString(R.string.preferences_on_birthday_clock),
						context.getResources().getInteger(R.integer.preferences_on_birthday_clock));
	}

	public boolean getOneDayBeforeActive() {
		return preferences
				.getBoolean(context.getString(R.string.preferences_one_day_before_active),
						context.getResources().getBoolean(R.bool.preferences_one_day_before_active));
	}

	public int getOneDayBeforeClock() {
		return preferences
				.getInt(context.getString(R.string.preferences_one_day_before_clock),
						context.getResources().getInteger(R.integer.preferences_one_day_before_clock));
	}

	public boolean getXDaysBeforeActive() {
		return preferences
				.getBoolean(context.getString(R.string.preferences_x_days_before_active),
						context.getResources().getBoolean(R.bool.preferences_x_days_before_active));
	}

	public int getXDaysBeforeClock() {
		return preferences
				.getInt(context.getString(R.string.preferences_x_days_before_clock),
						context.getResources().getInteger(R.integer.preferences_x_days_before_clock));
	}

	public int getXDaysBeforeDays() {
		return preferences
				.getInt(context.getString(R.string.preferences_x_days_before_days),
						context.getResources().getInteger(R.integer.preferences_x_days_before_days));
	}

}

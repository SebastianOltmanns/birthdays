package com.woodplantation.geburtstagsverwaltung.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.woodplantation.geburtstagsverwaltung.R;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Created by Sebu on 19.10.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */

public class MyPreferences {

	//TODO
	//TODO migrate two sharedpreferences into one
	//TODO
	public static final String FILEPATH_NOTIFICATION = "notification_settings";
	public static final String FILEPATH_SETTINGS = "settings";

	private Context context;
	public SharedPreferences preferences;

	@Inject
	public MyPreferences(@ApplicationContext Context context) {
		this.context = context;
		this.preferences = context.getSharedPreferences(FILEPATH_SETTINGS, Context.MODE_PRIVATE);
	}

	public MyPreferences(Context context, String filepath) {
		this.context = context;
		this.preferences = context.getSharedPreferences(filepath, Context.MODE_PRIVATE);
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

	public boolean getDisplayFAB() {
		return preferences
				.getBoolean(context.getString(R.string.preferences_display_fab),
						context.getResources().getBoolean(R.bool.preferences_display_fab));
	}

	public int getNextNotificationId() {
		int id = preferences
				.getInt(context.getString(R.string.preferences_notification_id),
						context.getResources().getInteger(R.integer.preferences_notification_id));
		preferences.edit().putInt(context.getString(R.string.preferences_notification_id),
				(id + 1) % Integer.MAX_VALUE).apply();
		return id;
	}

	public boolean getFirstTimeCall() {
		boolean firstTime = preferences
				.getBoolean(context.getString(R.string.preferences_first_time),
						context.getResources().getBoolean(R.bool.preferences_first_time));
		if (firstTime) {
			preferences.edit().putBoolean(context.getString(R.string.preferences_first_time), false).apply();
		}
		return firstTime;
	}

	public boolean[] getWhich() {
		return new boolean[]{
				getOnBirthdayActive(),
				getOneDayBeforeActive(),
				getXDaysBeforeActive()
		};
	}

	public int[] getClocks() {
		return new int[]{
				getOnBirthdayClock(),
				getOneDayBeforeClock(),
				getXDaysBeforeClock()
		};
	}

	public boolean isStorageMigrated() {
		return preferences
				.getBoolean(context.getString(R.string.preferences_storage_migrated),
						context.getResources().getBoolean(R.bool.preferences_storage_migrated));
	}

	public boolean arePreferencesMigrated() {
		return preferences
				.getBoolean(context.getString(R.string.preferences_preferences_migrated),
						context.getResources().getBoolean(R.bool.preferences_preferences_migrated));
	}

}

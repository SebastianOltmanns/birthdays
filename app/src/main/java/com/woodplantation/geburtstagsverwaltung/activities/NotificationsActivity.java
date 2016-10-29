package com.woodplantation.geburtstagsverwaltung.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.woodplantation.geburtstagsverwaltung.notifications.MyPreferences;
import com.woodplantation.geburtstagsverwaltung.R;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Sebu on 19.10.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */

public class NotificationsActivity extends AppCompatActivity {

	private MyPreferences preferences;

	private boolean is24h;

	private Switch switchActive;
	private LinearLayout layoutActive;
	private CheckBox checkBoxOnBirthday;
	private TextView textViewOnBirthday;
	private CheckBox checkBoxOneDayBefore;
	private TextView textViewOneDayBefore;
	private CheckBox checkBoxXDaysBefore;
	private TextView textViewXDaysBefore;

	private boolean active;
	private boolean onBirthdayActive;
	private boolean oneDayBeforeActive;
	private boolean xDaysBeforeActive;
	int[] clocks = new int[3];
	private int xDaysBeforeDays;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notifications);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		is24h = DateFormat.is24HourFormat(this);

		//initalize the layout variables

		switchActive = (Switch) findViewById(R.id.activity_notifications_switch_active);
		layoutActive = (LinearLayout) findViewById(R.id.activity_notifications_layout_active);
		checkBoxOnBirthday = (CheckBox) findViewById(R.id.activity_notifications_checkbox_on_birthday);
		textViewOnBirthday = (TextView) findViewById(R.id.activity_notifications_textview_on_birthday);
		checkBoxOneDayBefore = (CheckBox) findViewById(R.id.activity_notifications_checkbox_one_day_before);
		textViewOneDayBefore = (TextView) findViewById(R.id.activity_notifications_textview_one_day_before);
		checkBoxXDaysBefore = (CheckBox) findViewById(R.id.activity_notifications_checkbox_x_days_before);
		textViewXDaysBefore = (TextView) findViewById(R.id.activity_notifications_textview_x_days_before);

		//get preferences

		preferences = new MyPreferences(this);

		//load all preferences and save it once so the preferences are initialized

		active = preferences.getActive();
		onBirthdayActive = preferences.getOnBirthdayActive();
		oneDayBeforeActive = preferences.getOneDayBeforeActive();
		xDaysBeforeActive = preferences.getXDaysBeforeActive();
		clocks[0] = preferences.getOnBirthdayClock();
		clocks[1] = preferences.getOneDayBeforeClock();
		clocks[2] = preferences.getXDaysBeforeClock();
		xDaysBeforeDays = preferences.getXDaysBeforeDays();
		saveThePreferences();

		//do graphics stuff

		refreshVision();

		//set listeners

		switchActive.setOnClickListener(new OnSwitchClickListener());
		OnCheckBoxClickListener onCheckBoxClickListener = new OnCheckBoxClickListener();
		checkBoxOnBirthday.setOnClickListener(onCheckBoxClickListener);
		checkBoxOneDayBefore.setOnClickListener(onCheckBoxClickListener);
		checkBoxXDaysBefore.setOnClickListener(onCheckBoxClickListener);
		OnTextViewClickListener onTextViewClickListener = new OnTextViewClickListener();
		textViewOnBirthday.setOnClickListener(onTextViewClickListener);
		textViewOneDayBefore.setOnClickListener(onTextViewClickListener);
		textViewXDaysBefore.setOnClickListener(onTextViewClickListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_cancel_and_save, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case R.id.menu_cancel:
			case android.R.id.home:
				setResult(RESULT_CANCELED);
				finish();
				return true;
			case R.id.menu_ok:
				Map<String, ?> map = preferences.preferences.getAll();
				Intent resultIntent = new Intent();
				resultIntent.putExtra(MainActivity.INTENT_CODE_OLD_PREFERENCES, (Serializable) map);

				saveThePreferences();

				setResult(RESULT_OK, resultIntent);
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void saveThePreferences() {
		SharedPreferences.Editor editor = preferences.preferences.edit();
		Log.d("NotificationsActivity","Pref set active: " + active);
		editor.putBoolean(getString(R.string.preferences_active), active);
		Log.d("NotificationsActivity","Pref set active: " + onBirthdayActive);
		editor.putBoolean(getString(R.string.preferences_on_birthday_active), onBirthdayActive);
		Log.d("NotificationsActivity","Pref set active: " + oneDayBeforeActive);
		editor.putBoolean(getString(R.string.preferences_one_day_before_active), oneDayBeforeActive);
		Log.d("NotificationsActivity","Pref set active: " + xDaysBeforeActive);
		editor.putBoolean(getString(R.string.preferences_x_days_before_active), xDaysBeforeActive);
		Log.d("NotificationsActivity","Pref set active: " + clocks[0]);
		editor.putInt(getString(R.string.preferences_on_birthday_clock), clocks[0]);
		Log.d("NotificationsActivity","Pref set active: " + clocks[1]);
		editor.putInt(getString(R.string.preferences_one_day_before_clock), clocks[1]);
		Log.d("NotificationsActivity","Pref set active: " + clocks[2]);
		editor.putInt(getString(R.string.preferences_x_days_before_clock), clocks[2]);
		Log.d("NotificationsActivity","Pref set active: " + xDaysBeforeDays);
		editor.putInt(getString(R.string.preferences_x_days_before_days), xDaysBeforeDays);
		editor.apply();
	}

	private void refreshVision() {
		refreshActive();

		refreshCheckBoxActives();

		refreshTexts();
	}

	private void refreshActive() {
		switchActive.setChecked(active);
		layoutActive.setVisibility(active ? View.VISIBLE : View.GONE);
	}

	private void refreshCheckBoxActives() {
		checkBoxOnBirthday.setChecked(onBirthdayActive);

		checkBoxOneDayBefore.setChecked(oneDayBeforeActive);

		checkBoxXDaysBefore.setChecked(xDaysBeforeActive);
	}

	private void refreshTexts() {
		String[] stringClocks = new String[3];
		for (int i = 0; i < clocks.length; i++) {
			int h = clocks[i] / 60;
			int m = clocks[i] % 60;
			if (!is24h) {
				boolean am = h < 12;
				h = h % 12;
				if (h == 0) {
					h = 12;
				}
				String hS = (h < 10) ? ("0" + h) : String.valueOf(h);
				String mS = (m < 10) ? ("0" + m) : String.valueOf(m);
				stringClocks[i] = hS + ":" + mS + (am ? " AM" : " PM");
			} else {
				String hS = (h < 10) ? ("0" + h) : String.valueOf(h);
				String mS = (m < 10) ? ("0" + m) : String.valueOf(m);
				stringClocks[i] = hS + ":" + mS;
			}
		}

		textViewOnBirthday.setText(getString(R.string.activity_notifications_on_birthday, stringClocks[0]));
		textViewOneDayBefore.setText(getString(R.string.activity_notifications_one_day_before, stringClocks[1]));
		textViewXDaysBefore.setText(getString(R.string.activity_notifications_x_days_before, xDaysBeforeDays, stringClocks[2]));
	}

	private class OnSwitchClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			active = !active;
			refreshActive();
		}
	}

	private class OnCheckBoxClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
				case R.id.activity_notifications_checkbox_on_birthday:
					onBirthdayActive = !onBirthdayActive;
					break;
				case R.id.activity_notifications_checkbox_one_day_before:
					oneDayBeforeActive = !oneDayBeforeActive;
					break;
				case R.id.activity_notifications_checkbox_x_days_before:
					xDaysBeforeActive = !xDaysBeforeActive;
					break;
			}
			refreshTexts();
		}
	}

	private class OnTextViewClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			int which = -1;
			switch (view.getId()) {
				case R.id.activity_notifications_textview_on_birthday:
					if (!onBirthdayActive) {
						return;
					}
					which=0;
					break;
				case R.id.activity_notifications_textview_one_day_before:
					if (!oneDayBeforeActive) {
						return;
					}
					which=1;
					break;
				case R.id.activity_notifications_textview_x_days_before:
					if (!xDaysBeforeActive) {
						return;
					}
					which=2;
					break;
			}
			if (which == -1) {
				return;
			}
			int h = clocks[which] / 60;
			int m = clocks[which] % 60;
			MyOnTimeSetListener myOnTimeSetListener = new MyOnTimeSetListener(which);
			TimePickerDialog dialog = new TimePickerDialog(NotificationsActivity.this, myOnTimeSetListener, h, m, is24h);
			dialog.show();
		}

		private class MyOnTimeSetListener implements TimePickerDialog.OnTimeSetListener {

			private int which;
			private NumberPicker numberPicker;
			private Dialog dialog;
			private int hour, minute;

			private MyOnTimeSetListener(int which) {
				this.which = which;
			}

			@Override
			public void onTimeSet(TimePicker timePicker, int hour, int minute) {
				if (which == 2) {
					this.hour = hour;
					this.minute = minute;

					dialog = new Dialog(NotificationsActivity.this);
					dialog.setContentView(R.layout.dialog_number_picker);
					dialog.setTitle(getString(R.string.activity_notifications_x_days_before_without_time, xDaysBeforeDays));

					numberPicker = (NumberPicker) dialog.findViewById(R.id.number_picker);
					numberPicker.setMaxValue(28);
					numberPicker.setMinValue(2);
					numberPicker.setValue(xDaysBeforeDays);
					MyOnValueChangeListener myOnValueChangeListener = new MyOnValueChangeListener();
					numberPicker.setOnValueChangedListener(myOnValueChangeListener);

					Button buttonCancel = (Button) dialog.findViewById(R.id.number_picker_button_cancel);
					Button buttonOk = (Button) dialog.findViewById(R.id.number_picker_button_ok);
					OnButtonClickListener onButtonClickListener = new OnButtonClickListener();
					buttonCancel.setOnClickListener(onButtonClickListener);
					buttonOk.setOnClickListener(onButtonClickListener);

					dialog.show();
				} else {
					clocks[which] = hour * 60 + minute;
					refreshTexts();
				}
			}

			private class MyOnValueChangeListener implements NumberPicker.OnValueChangeListener {

				@Override
				public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
					dialog.setTitle(getString(R.string.activity_notifications_x_days_before_without_time, newVal));
				}
			}

			private class OnButtonClickListener implements View.OnClickListener {

				@Override
				public void onClick(View view) {
					if (view.getId() == R.id.number_picker_button_cancel) {
						dialog.dismiss();
					} else if (view.getId() == R.id.number_picker_button_ok) {
						xDaysBeforeDays = numberPicker.getValue();
						clocks[which] = hour * 60 + minute;
						refreshTexts();
						dialog.dismiss();
					}
				}
			}
		}
	}
}

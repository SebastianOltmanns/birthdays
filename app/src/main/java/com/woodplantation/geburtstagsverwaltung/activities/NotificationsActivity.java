package com.woodplantation.geburtstagsverwaltung.activities;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.woodplantation.geburtstagsverwaltung.notifications.MyPreferences;
import com.woodplantation.geburtstagsverwaltung.R;

import java.io.Serializable;
import java.util.Calendar;
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
	private CheckBox[] checkBoxes = new CheckBox[3];
	private TextView[] textViews = new TextView[3];

	private boolean active;
	private boolean[] actives = new boolean[3];
	int[] clocks = new int[3];
	private int xDaysBeforeDays;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notifications);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		is24h = android.text.format.DateFormat.is24HourFormat(this);

		//initalize the layout variables

		switchActive = (Switch) findViewById(R.id.activity_notifications_switch_active);
		layoutActive = (LinearLayout) findViewById(R.id.activity_notifications_layout_active);
		checkBoxes[0] = (CheckBox) findViewById(R.id.activity_notifications_checkbox_on_birthday);
		textViews[0] = (TextView) findViewById(R.id.activity_notifications_textview_on_birthday);
		checkBoxes[1] = (CheckBox) findViewById(R.id.activity_notifications_checkbox_one_day_before);
		textViews[1] = (TextView) findViewById(R.id.activity_notifications_textview_one_day_before);
		checkBoxes[2] = (CheckBox) findViewById(R.id.activity_notifications_checkbox_x_days_before);
		textViews[2] = (TextView) findViewById(R.id.activity_notifications_textview_x_days_before);

		//get preferences

		preferences = new MyPreferences(this, MyPreferences.FILEPATH_NOTIFICATION);

		//load all preferences and save it once so the preferences are initialized

		active = preferences.getActive();
		actives[0] = preferences.getOnBirthdayActive();
		actives[1] = preferences.getOneDayBeforeActive();
		actives[2] = preferences.getXDaysBeforeActive();
		clocks[0] = preferences.getOnBirthdayClock();
		clocks[1] = preferences.getOneDayBeforeClock();
		clocks[2] = preferences.getXDaysBeforeClock();
		xDaysBeforeDays = preferences.getXDaysBeforeDays();
		saveThePreferences();

		//do graphics stuff

		refreshVision();

		//set listeners

		switchActive.setOnClickListener(new OnSwitchClickListener());
		for (int i = 0; i < 3; i++) {
			checkBoxes[i].setOnClickListener(new OnCheckBoxClickListener(i));
		}
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
		Log.d("NotificationsActivity","Pref set active: " + actives[0]);
		editor.putBoolean(getString(R.string.preferences_on_birthday_active), actives[0]);
		Log.d("NotificationsActivity","Pref set active: " + actives[1]);
		editor.putBoolean(getString(R.string.preferences_one_day_before_active), actives[1]);
		Log.d("NotificationsActivity","Pref set active: " + actives[2]);
		editor.putBoolean(getString(R.string.preferences_x_days_before_active), actives[2]);
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
		for (int i = 0; i < 3; i++) {
			checkBoxes[i].setChecked(actives[i]);
			textViews[i].setEnabled(actives[i]);
		}
	}

	private Calendar c = Calendar.getInstance(); {
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
	}

	private abstract class DisableableClickableSpan extends ClickableSpan {
		protected int which;
		DisableableClickableSpan(int which) {
			this.which = which;
		}
		@Override
		public void updateDrawState(TextPaint ds) {
			if (textViews[which].isEnabled()) {
				ds.setColor(ContextCompat.getColor(NotificationsActivity.this, R.color.colorAccent));
				ds.setUnderlineText(true);
			} else {
				ds.setColor(ContextCompat.getColor(NotificationsActivity.this, R.color.text_color_disabled));
				ds.setUnderlineText(false);
			}
		}
	}

	private class ClickableTimeSpan extends DisableableClickableSpan {
		ClickableTimeSpan(int which) {
			super(which);
		}
		@Override
		public void onClick(View view) {
			int h = clocks[which] / 60;
			int m = clocks[which] % 60;
			MyOnTimeSetListener myOnTimeSetListener = new MyOnTimeSetListener(which);
			TimePickerDialog dialog = new TimePickerDialog(NotificationsActivity.this, myOnTimeSetListener, h, m, is24h);
			dialog.setTitle(R.string.activity_notifications_time_picker_title);
			dialog.show();
		}
		class MyOnTimeSetListener implements TimePickerDialog.OnTimeSetListener {
			private int which;
			private MyOnTimeSetListener(int which) {
				this.which = which;
			}
			@Override
			public void onTimeSet(TimePicker timePicker, int hour, int minute) {
				clocks[which] = hour * 60 + minute;
				refreshTexts();
			}
		}

	};

	private ClickableSpan clickableSetDaysBeforeSpan = new DisableableClickableSpan(2) {
		private AlertDialog dialog;
		private TextView dialogTextView;
		private NumberPicker numberPicker;
		@Override
		public void onClick(View view) {
			AlertDialog.Builder builder = new AlertDialog.Builder(NotificationsActivity.this)
					.setTitle(R.string.activity_notifications_date_picker_title)
					.setPositiveButton(R.string.ok, new OnButtonClickListener())
					.setNegativeButton(R.string.cancel, null);
			dialog = builder.create();
			View dialogNumberPicker = View.inflate(NotificationsActivity.this, R.layout.dialog_number_picker, null);
			dialog.setView(dialogNumberPicker);

			dialogTextView = (TextView) dialogNumberPicker.findViewById(R.id.dialog_number_picker_text);
			dialogTextView.setText(getResources().getString(R.string.activity_notifications_x_days_before_without_time, xDaysBeforeDays));

			numberPicker = (NumberPicker) dialogNumberPicker.findViewById(R.id.number_picker);

			numberPicker.setMaxValue(28);
			numberPicker.setMinValue(2);
			numberPicker.setValue(xDaysBeforeDays);
			MyOnValueChangeListener myOnValueChangeListener = new MyOnValueChangeListener();
			numberPicker.setOnValueChangedListener(myOnValueChangeListener);

			dialog.show();
		}
		class MyOnValueChangeListener implements NumberPicker.OnValueChangeListener {
			@Override
			public void onValueChange(NumberPicker numberPicker, int oldVal, int newVal) {
				dialogTextView.setText(getString(R.string.activity_notifications_x_days_before_without_time, newVal));
			}
		}
		class OnButtonClickListener implements DialogInterface.OnClickListener {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				if (i == DialogInterface.BUTTON_POSITIVE) {
					xDaysBeforeDays = numberPicker.getValue();
					refreshTexts();
					dialog.dismiss();
				}
			}
		}
	};

	private void refreshTexts() {
		SpannableStringBuilder[] ssb = new SpannableStringBuilder[3];
		String[] stringClocks = getResources().getStringArray(R.array.activity_notifications_settings);

		for (int i = 0; i < clocks.length; i++) {
			c.set(Calendar.HOUR_OF_DAY, clocks[i]/60);
			c.set(Calendar.MINUTE, clocks[i]%60);
			String clock = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(c.getTime());

			String string;
			if (i == 2) {
				string = String.format(stringClocks[i], xDaysBeforeDays, clock);
			} else {
				string = String.format(stringClocks[i], clock);
			}

			ssb[i] = new SpannableStringBuilder(string);
			ssb[i].setSpan(new ClickableTimeSpan(i), string.length()-clock.length(), string.length(), 0);

			if (i == 2) {
				ssb[i].setSpan(clickableSetDaysBeforeSpan, 0, String.valueOf(xDaysBeforeDays).length(), 0);
			}
		}

		for (int i = 0; i < 3; i++) {
			textViews[i].setMovementMethod(LinkMovementMethod.getInstance());
			textViews[i].setText(ssb[i], TextView.BufferType.SPANNABLE);
		}
	}

	private class OnSwitchClickListener implements View.OnClickListener {
		@Override
		public void onClick(View view) {
			active = !active;
			refreshActive();
		}
	}


	private class OnCheckBoxClickListener implements View.OnClickListener {

		int which;

		public OnCheckBoxClickListener(int which) {
			this.which = which;
		}

		@Override
		public void onClick(View view) {
			actives[which] = !actives[which];
            refreshCheckBoxActives();
		}
	}

}

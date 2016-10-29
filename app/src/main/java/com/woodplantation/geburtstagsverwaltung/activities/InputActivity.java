package com.woodplantation.geburtstagsverwaltung.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.woodplantation.geburtstagsverwaltung.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public abstract class InputActivity extends AppCompatActivity {


	protected EditText firstNameEdit;
	protected EditText lastNameEdit;
	protected TextView birthdayText;
	protected EditText othersEdit;

	protected SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
	protected Calendar birthday;
	protected boolean flagAddedBirthday = false;

	protected void onCreate(Bundle savedInstanceState, int contentView) {
		super.onCreate(savedInstanceState);
		setContentView(contentView);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		firstNameEdit = (EditText) findViewById(R.id.text_input_firstname);
		lastNameEdit = (EditText) findViewById(R.id.text_input_lastname);
		birthdayText = (TextView) findViewById(R.id.text_input_birthday);
		othersEdit = (EditText) findViewById(R.id.text_input_others);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void changeBirthday(View view) {
		final TextView birthdayTextView = (TextView) findViewById(R.id.text_input_birthday);

		DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				birthday.set(year, monthOfYear, dayOfMonth);
				birthdayTextView.setText(sdf.format(birthday.getTime()));
				flagAddedBirthday = true;
			}
		};

		DatePickerDialog datePickerDialog = new DatePickerDialog(this, callback,
				birthday.get(Calendar.YEAR),
				birthday.get(Calendar.MONTH),
				birthday.get(Calendar.DAY_OF_MONTH));

		datePickerDialog.show();
	}
}

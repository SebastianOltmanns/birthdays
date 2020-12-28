package com.woodplantation.geburtstagsverwaltung.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.viewmodel.InputViewModel;
import com.woodplantation.geburtstagsverwaltung.viewmodel.MainViewModel;

import java.util.Calendar;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
@AndroidEntryPoint
public abstract class InputActivity extends AppCompatActivity {

	private EditText firstName;
	private EditText lastName;
	private EditText birthdayDay, birthdayMonth, birthdayYear;
	private SwitchCompat ignoreYear;
	private EditText notes;

	private InputViewModel inputViewModel;

	/**
	 * text watcher that automatically requests focus for the next
	 * textview once the given text length is reached
	 */
	private static class FocusNextViewTextWatcher implements TextWatcher {
		int count;
		View view;
		FocusNextViewTextWatcher(int count, View view) {
			this.count = count;
			this.view = view;
		}
		@Override
		public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
		@Override
		public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
		@Override
		public void afterTextChanged(Editable editable) {
			if (editable.length() == count) view.requestFocus();
		}
	}

	protected void onCreate(Bundle savedInstanceState, int contentView) {
		super.onCreate(savedInstanceState);
		setContentView(contentView);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		firstName = findViewById(R.id.text_input_firstname);
		lastName = findViewById(R.id.text_input_lastname);
		birthdayDay = findViewById(R.id.text_input_birthday_day);
		birthdayMonth = findViewById(R.id.text_input_birthday_month);
		birthdayYear = findViewById(R.id.text_input_birthday_year);
		ignoreYear = findViewById(R.id.ignore_year);
		notes = findViewById(R.id.text_input_others);

        birthdayDay.addTextChangedListener(new FocusNextViewTextWatcher(2, birthdayMonth));
		birthdayMonth.addTextChangedListener(new FocusNextViewTextWatcher(2, birthdayYear));

		inputViewModel = new ViewModelProvider(this).get(InputViewModel.class);

		inputViewModel.getFirstName().observe(this, firstName::setText);
		inputViewModel.getLastName().observe(this, lastName::setText);
		inputViewModel.getBirthday().observe(this, birthday -> {
			birthdayDay.setText(String.valueOf(birthday.getDayOfMonth()));
			birthdayMonth.setText(String.valueOf(birthday.getMonth()));
			birthdayYear.setText(String.valueOf(birthday.getYear()));
		});
		inputViewModel.getIgnoreYear()

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
		DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
				birthday.set(year, monthOfYear, dayOfMonth);
				birthdayDay.setText(String.valueOf(dayOfMonth));
				birthdayMonth.setText(String.valueOf(monthOfYear+1));
				birthdayYear.setText(String.valueOf(year));
			}
		};

		DatePickerDialog datePickerDialog = new DatePickerDialog(this, callback,
				birthday.get(Calendar.YEAR),
				birthday.get(Calendar.MONTH),
				birthday.get(Calendar.DAY_OF_MONTH));

		datePickerDialog.show();
	}

	protected boolean checkInput() {
        String birthdayDay = this.birthdayDay.getText().toString();
        String birthdayMonth = this.birthdayMonth.getText().toString();
        String birthdayYear = this.birthdayYear.getText().toString();
        String firstName = this.firstName.getText().toString();
        String lastName = this.lastName.getText().toString();

        boolean flag = true;
        int messageResource = 0;

        //check for valid birthday: first, check if all fields are set
		if (TextUtils.isEmpty(birthdayDay)
				|| TextUtils.isEmpty(birthdayMonth)
				|| TextUtils.isEmpty(birthdayYear)) {
            messageResource = R.string.wrong_input_birthday;
            flag = false;
        } else {
            //all fields are set. check if the actual values are fine, e.g. 41.15.2088 is not valid
            int birthdayD = Integer.parseInt(birthdayDay);
            int birthdayM = Integer.parseInt(birthdayMonth);
            int birthdayY = Integer.parseInt(birthdayYear);
            Calendar inputBirthday = Calendar.getInstance();
            inputBirthday.set(birthdayY, birthdayM-1, birthdayD);
            if (inputBirthday.after(Calendar.getInstance())
                    || inputBirthday.get(Calendar.YEAR) != birthdayY
                    || inputBirthday.get(Calendar.MONTH) != birthdayM-1
                    || inputBirthday.get(Calendar.DAY_OF_MONTH) != birthdayD) {
                messageResource = R.string.wrong_input_birthday;
                flag = false;
            } else if (TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) {
                messageResource = R.string.wrong_input_no_name;
                flag = false;
            }
        }

        if (!flag) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(messageResource);
            builder.setTitle(R.string.wrong_input_title);
            builder.setPositiveButton(R.string.ok, null);
            builder.show();
            return false;
        }

        return true;
    }

	/**
	 * only call this function when checkInput() was called before AND returned true !
	 */
	protected void setBirthdayFromEditTexts() {
		String birthdayDay = this.birthdayDay.getText().toString();
		String birthdayMonth = this.birthdayMonth.getText().toString();
		String birthdayYear = this.birthdayYear.getText().toString();
		int birthdayD = Integer.parseInt(birthdayDay);
		int birthdayM = Integer.parseInt(birthdayMonth);
		int birthdayY = Integer.parseInt(birthdayYear);

		birthday.set(birthdayY, birthdayM-1, birthdayD);

	}
}

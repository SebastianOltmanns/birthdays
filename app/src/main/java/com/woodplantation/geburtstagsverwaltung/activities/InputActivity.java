package com.woodplantation.geburtstagsverwaltung.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;
import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.exceptions.InvalidDateException;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoFirstNameSpecifiedException;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoIdToDeleteException;
import com.woodplantation.geburtstagsverwaltung.util.IntentCodes;
import com.woodplantation.geburtstagsverwaltung.view.AfterTextChangedWatcher;
import com.woodplantation.geburtstagsverwaltung.view.FocusNextViewTextWatcher;
import com.woodplantation.geburtstagsverwaltung.view.ObserverThatSetsTextIfContentIsNotEqual;
import com.woodplantation.geburtstagsverwaltung.viewmodel.InputViewModel;

import java.time.DateTimeException;
import java.time.LocalDate;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
@AndroidEntryPoint
public class InputActivity extends AppCompatActivity {

	private TextInputEditText firstName;
	private TextInputEditText lastName;
	private TextInputEditText birthdayDay, birthdayMonth, birthdayYear;
	private ImageButton birthdayButton;
	private SwitchCompat ignoreYear;
	private TextInputEditText notes;

	private InputViewModel inputViewModel;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		firstName = findViewById(R.id.first_name);
		lastName = findViewById(R.id.last_name);
		birthdayDay = findViewById(R.id.birthday_day);
		birthdayMonth = findViewById(R.id.birthday_month);
		birthdayYear = findViewById(R.id.birthday_year);
		birthdayButton = findViewById(R.id.birthday_button);
		ignoreYear = findViewById(R.id.ignore_year);
		notes = findViewById(R.id.notes);

        birthdayDay.addTextChangedListener(new FocusNextViewTextWatcher(2, birthdayMonth));
		birthdayMonth.addTextChangedListener(new FocusNextViewTextWatcher(2, birthdayYear));

		inputViewModel = new ViewModelProvider(this).get(InputViewModel.class);
		Long id = getIntent().hasExtra(IntentCodes.ID) ? getIntent().getLongExtra(IntentCodes.ID, -1) : null;
		setTitle((id != null && id != -1) ? R.string.edit : R.string.add);
		inputViewModel.init(id, error -> {
			new AlertDialog.Builder(this)
					.setTitle(R.string.loading_failed_title)
					.setMessage(R.string.loading_failed_text)
					.setNeutralButton(R.string.ok, (a,b) -> finish())
					.setCancelable(false)
					.show();
		});

		inputViewModel.getFirstName().observe(this, ObserverThatSetsTextIfContentIsNotEqual.forString(firstName));
		inputViewModel.getLastName().observe(this, ObserverThatSetsTextIfContentIsNotEqual.forString(lastName));
		inputViewModel.getBirthdayDay().observe(this, ObserverThatSetsTextIfContentIsNotEqual.forInteger(birthdayDay));
		inputViewModel.getBirthdayMonth().observe(this, ObserverThatSetsTextIfContentIsNotEqual.forInteger(birthdayMonth));
		inputViewModel.getBirthdayYear().observe(this, ObserverThatSetsTextIfContentIsNotEqual.forInteger(birthdayYear));
		inputViewModel.getIgnoreYear().observe(this, _ignoreYear -> {
			if (!_ignoreYear.equals(ignoreYear.isChecked())) {
				ignoreYear.setChecked(_ignoreYear);
			}
		});
		inputViewModel.getNotes().observe(this, ObserverThatSetsTextIfContentIsNotEqual.forString(notes));

		firstName.addTextChangedListener(new AfterTextChangedWatcher(e -> inputViewModel.setFirstName(e.toString())));
		lastName.addTextChangedListener(new AfterTextChangedWatcher(e -> inputViewModel.setLastName(e.toString())));
		birthdayDay.addTextChangedListener(new AfterTextChangedWatcher(e -> inputViewModel.setBirthdayDay(Integer.parseInt(e.toString()))));
		birthdayMonth.addTextChangedListener(new AfterTextChangedWatcher(e -> inputViewModel.setBirthdayMonth(Integer.parseInt(e.toString()))));
		birthdayYear.addTextChangedListener(new AfterTextChangedWatcher(e -> inputViewModel.setBirthdayYear(Integer.parseInt(e.toString()))));
		ignoreYear.setOnCheckedChangeListener((buttonView, isChecked) -> inputViewModel.setIgnoreYear(isChecked));
		notes.addTextChangedListener(new AfterTextChangedWatcher(e -> inputViewModel.setNotes(e.toString())));

		birthdayButton.setOnClickListener(view -> {
			LocalDate birthday = LocalDate.now();
			try {
				//noinspection ConstantConditions
				birthday = LocalDate.of(inputViewModel.getBirthdayYear().getValue(), inputViewModel.getBirthdayMonth().getValue(), inputViewModel.getBirthdayDay().getValue());
			} catch (DateTimeException ignored) {
			}
			new DatePickerDialog(
					this,
					(v, year, monthOfYear, dayOfMonth) -> {
						inputViewModel.setBirthdayYear(year);
						inputViewModel.setBirthdayMonth(monthOfYear + 1);
						inputViewModel.setBirthdayDay(dayOfMonth);
					},
					birthday.getYear(),
					birthday.getMonthValue() - 1,
					birthday.getDayOfMonth()
			).show();
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_input, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem deleteItem = menu.findItem(R.id.menu_delete);
		deleteItem.setVisible(inputViewModel.hasId());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home || item.getItemId() == R.id.menu_cancel) {
			finish();
			return true;
		} else if (item.getItemId() == R.id.menu_ok) {
			inputViewModel.save(
					this::finish,
					error -> {
						String errorMessage;
						if (error instanceof NoFirstNameSpecifiedException) {
							errorMessage = getString(R.string.no_first_name_specified);
						} else if (error instanceof InvalidDateException) {
							errorMessage = getString(R.string.invalid_date);
						} else {
							errorMessage = error.getLocalizedMessage();
						}
						new AlertDialog.Builder(this)
								.setTitle(R.string.save_failed_title)
								.setMessage(getString(R.string.save_failed_text, errorMessage))
								.setNeutralButton(R.string.ok, null)
								.show();
					}
			);
		} else if (item.getItemId() == R.id.menu_delete) {
			inputViewModel.delete(
					this::finish,
					error -> new AlertDialog.Builder(this)
							.setTitle(R.string.delete_failed_title)
							.setMessage(getString(R.string.delete_failed_text, error instanceof NoIdToDeleteException ? getString(R.string.no_id_to_delete) : error.getLocalizedMessage()))
							.setNeutralButton(R.string.ok, null)
							.show()
			);
		}

		return super.onOptionsItemSelected(item);
	}

}

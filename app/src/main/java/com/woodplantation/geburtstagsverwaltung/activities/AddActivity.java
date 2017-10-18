package com.woodplantation.geburtstagsverwaltung.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.R;

import java.util.Calendar;

/**
 * Created by Sebu on 09.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class AddActivity extends InputActivity {

	private int newId;

	@Override
	@SuppressWarnings("MissingSuperCall")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_add);

		birthday = Calendar.getInstance();
		birthday.setTimeInMillis(0);
		birthday.set(Calendar.HOUR_OF_DAY, 8);

		Intent intent = getIntent();
		newId = intent.getIntExtra(MainActivity.INTENT_CODE_NEW_ID, -1);
		if (newId == -1) {
			setResult(RESULT_CANCELED);
			finish();
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

		if (id == R.id.menu_cancel) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		} else if (id == R.id.menu_ok) {
			if (!checkInput()) {
				return true;
			}
			setBirthdayFromEditTexts();
			finishWithSaving();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void finishWithSaving() {
		Intent resultIntent = new Intent();
		DataSet dataSet = new DataSet(
				newId,
				birthday,
				firstNameEdit.getText().toString(),
				lastNameEdit.getText().toString(),
				othersEdit.getText().toString());
		resultIntent.putExtra(MainActivity.INTENT_CODE_DATA_SET, dataSet);
		setResult(RESULT_OK, resultIntent);
		finish();
	}
}

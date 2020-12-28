package com.woodplantation.geburtstagsverwaltung.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.util.IntentCodes;

import java.util.Calendar;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class EditActivity extends InputActivity {

	private DataSet oldDataSet;
	private int index;

	@Override
	@SuppressWarnings("MissingSuperCall")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_edit);

		Intent intent = getIntent();
		index = intent.getIntExtra(IntentCodes.INDEX, -1);
		oldDataSet = (DataSet) intent.getSerializableExtra(IntentCodes.DATASET);
		if (index == -1) {
			setResult(RESULT_CANCELED);
			finish();
		}
		birthday = oldDataSet.birthday;

		firstName.setText(oldDataSet.firstName);
		lastName.setText(oldDataSet.lastName);
		birthdayDay.setText(String.valueOf(birthday.get(Calendar.DAY_OF_MONTH)));
		birthdayMonth.setText(String.valueOf(birthday.get(Calendar.MONTH)+1));
		birthdayYear.setText(String.valueOf(birthday.get(Calendar.YEAR)));
		notes.setText(oldDataSet.others);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_edit, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.edit_cancel) {
			setResult(RESULT_CANCELED);
			finish();
			return true;
		} else if (id == R.id.edit_ok) {
			if (!checkInput()) {
				return true;
			}
			setBirthdayFromEditTexts();
			Intent resultIntent = new Intent();
			resultIntent.putExtra(IntentCodes.INDEX, index);
			DataSet newDataSet = new DataSet(
					oldDataSet.id,
					birthday,
					firstName.getText().toString(),
					lastName.getText().toString(),
					notes.getText().toString());
			resultIntent.putExtra(IntentCodes.DATASET, newDataSet);
			setResult(RESULT_OK, resultIntent);
			finish();
			return true;
		} else if (id == R.id.edit_delete) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.sure_delete_title);
			builder.setMessage(R.string.sure_delete_text);
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent resultIntent = new Intent();
					resultIntent.putExtra(IntentCodes.INDEX, index);
					setResult(RESULT_OK, resultIntent);
					dialog.dismiss();
					finish();
				}
			});
			builder.setNegativeButton(R.string.no, null);
			builder.show();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}

package com.woodplantation.geburtstagsverwaltung.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.R;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class EditActivity extends InputActivity {

	private DataSet oldDataSet;
	private int index;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, R.layout.activity_edit);

		Intent intent = getIntent();
		index = intent.getIntExtra(MainActivity.INTENT_CODE_EDIT_INDEX, -1);
		oldDataSet = (DataSet) intent.getSerializableExtra(MainActivity.INTENT_CODE_DATA_SET);
		if (index == -1) {
			setResult(RESULT_CANCELED);
			finish();
		}
		birthday = oldDataSet.getBirthday();

		firstNameEdit.setText(oldDataSet.getFirstName());
		lastNameEdit.setText(oldDataSet.getLastName());
		birthdayText.setText(sdf.format(birthday.getTime()));
		othersEdit.setText(oldDataSet.getOthers());
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
			Intent resultIntent = new Intent();
			resultIntent.putExtra(MainActivity.INTENT_CODE_EDIT_INDEX, index);
			DataSet newDataSet = new DataSet(
					oldDataSet.getId(),
					birthday,
					firstNameEdit.getText().toString(),
					lastNameEdit.getText().toString(),
					othersEdit.getText().toString());
			resultIntent.putExtra(MainActivity.INTENT_CODE_DATA_SET, newDataSet);
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
					resultIntent.putExtra(MainActivity.INTENT_CODE_EDIT_INDEX, index);
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

	private boolean checkInput() {
		String firstName = firstNameEdit.getText().toString();
		String lastName = lastNameEdit.getText().toString();

		boolean flag = true;
		int messageResource = 0;
		if (firstName == "") {
			messageResource = R.string.wrong_input_no_firstname;
			flag = false;
		}
		if (lastName == "") {
			messageResource = R.string.wrong_input_no_lastname;
			flag = false;
		}

		if (flag) {
			return true;
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(messageResource);
			builder.setTitle(R.string.wrong_input_title);
			builder.setPositiveButton(R.string.ok, null);
			builder.show();
			return false;
		}
	}
}

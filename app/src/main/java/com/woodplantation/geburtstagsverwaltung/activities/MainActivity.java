package com.woodplantation.geburtstagsverwaltung.activities;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.woodplantation.geburtstagsverwaltung.adapter.DataListViewAdapter;
import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.notifications.IdGenerator;
import com.woodplantation.geburtstagsverwaltung.notifications.NotificationHandler;
import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.storage.StorageHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Sebu on 09.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class MainActivity extends AppCompatActivity {

	public static final int REQUEST_INTENT_CREATE_DATA_SET = 1;
	public static final int REQUEST_INTENT_EDIT_DATA_SET = 2;
	public static final int REQUEST_INTENT_NOTIFICATIONS = 3;
	public static final int REQUEST_INTENT_FILEPICKER_IMPORT = 4;

	public static final int MAXIMUM_DATA_SIZE = 100;

	public static final String INTENT_CODE_DATA_SET = "DATASET";
	public static final String INTENT_CODE_EDIT_INDEX = "DATASET_INDEX";
	public static final String INTENT_CODE_NEW_ID = "DATASET_NEW_ID";
	public static final String INTENT_CODE_OLD_PREFERENCES = "OLD_PREFERENCES";

	private static final String FILE_EXPORT_DIRECTORY = "birthdays";
	private static final String FILE_EXPORT_NAME = "export_";
	private static final String FILE_EXPORT_EXTENSION = ".birthdays";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.US);

	private ArrayList<DataSet> data;
	private StorageHandler storageHandler;
	private ListView dataListView;
	private DataListViewAdapter dataListViewAdapter;

	private AlertDialog.Builder importExportFailDialog;
	private AlertDialog.Builder importExportStorageFailDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.activity_main_label);
		setSupportActionBar(toolbar);
		//getSupportActionBar().setTitle(R.string.activity_main_label);

		dataListViewAdapter = new DataListViewAdapter(this, R.layout.data_list_view_item);
		dataListView = (ListView) findViewById(R.id.data_list_view);
		dataListView.setAdapter(dataListViewAdapter);
		dataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d("mainactivity", "onitemclicklistener");
				DataSet dataSet = dataListViewAdapter.getItem(position);
				Intent intent = new Intent(MainActivity.this, EditActivity.class);
				intent.putExtra(INTENT_CODE_EDIT_INDEX, data.indexOf(dataSet));
				intent.putExtra(INTENT_CODE_DATA_SET, dataSet);
				startActivityForResult(intent, REQUEST_INTENT_EDIT_DATA_SET);
			}
		});

		storageHandler = new StorageHandler(this);
		if ((data = storageHandler.loadData()) == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.failure_loading_data_title);
			builder.setMessage(R.string.failure_loading_data_text);
			builder.setPositiveButton(R.string.ok, null);
			builder.show();
		}
		Log.d("mainactivity","oncreate. data: "  + data);

		importExportFailDialog =
				new AlertDialog.Builder(this).
						setTitle(R.string.import_export_failed_title).
						setMessage(R.string.import_export_failed_text).
						setNeutralButton(R.string.ok, null);

		importExportStorageFailDialog =
				new AlertDialog.Builder(MainActivity.this).
						setTitle(R.string.import_export_storage_error_title).
						setMessage(R.string.import_export_storage_error_text).
						setNeutralButton(R.string.ok, null);
	}

	public void onAddButtonClick(View v) {
		if (data != null && data.size() >= MAXIMUM_DATA_SIZE) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.maximum_data_achieved_text);
			builder.setTitle(R.string.maximum_data_achieved_title);
			builder.setNeutralButton(R.string.ok, null);
			builder.show();
			return;
		}

		int id = IdGenerator.getNewId(this);
		if (id == -1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.failure_loading_data_text);
			builder.setTitle(R.string.failure_loading_data_title);
			builder.setNeutralButton(R.string.ok, null);
			builder.show();
			return;
		}

		Intent intent = new Intent(MainActivity.this, AddActivity.class);
		intent.putExtra(INTENT_CODE_NEW_ID, id);
		startActivityForResult(intent, REQUEST_INTENT_CREATE_DATA_SET);
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.main_info) {
			Intent intent = new Intent(this, InfoActivity.class);
			startActivity(intent);
		} else if (id == R.id.main_rate) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.rating_title);
			builder.setMessage(R.string.rating_text);
			builder.setPositiveButton(R.string.rate_app, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String packageName = getPackageName();
					try {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
					} catch (ActivityNotFoundException e) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
					}
				}
			});
			builder.setNegativeButton(R.string.cancel, null);
			builder.show();
		} else if (id == R.id.main_notifications) {
			Intent intent = new Intent(this, NotificationsActivity.class);
			startActivityForResult(intent, REQUEST_INTENT_NOTIFICATIONS);
		} else if (id == R.id.main_import_export) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.import_export_dialog_title);
			builder.setMessage(getString(R.string.import_export_dialog_text, MAXIMUM_DATA_SIZE));
			builder.setPositiveButton(R.string.import_export_export, exportClickListener);
			builder.setNeutralButton(R.string.import_export_import, importClickListener);
			builder.show();
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("mainactivity", "onactivity result! requestcode: " + requestCode + " resultCode : " + resultCode);
		switch (requestCode) {

			case REQUEST_INTENT_CREATE_DATA_SET:
				if (resultCode == RESULT_OK) {
					DataSet dataSet = (DataSet) data.getSerializableExtra(INTENT_CODE_DATA_SET);
					this.data.add(dataSet);
					NotificationHandler.addBirthday(this, dataSet);

					refresh();
				}
				break;
			case REQUEST_INTENT_EDIT_DATA_SET:
				if (resultCode == RESULT_OK) {
					DataSet newDataSet = (DataSet) data.getSerializableExtra(INTENT_CODE_DATA_SET);
					int index = data.getIntExtra(INTENT_CODE_EDIT_INDEX, -1);
					if (index == -1) {
						return;
					}
					if (newDataSet != null) {
						this.data.add(newDataSet);
						NotificationHandler.updateBirthday(this, newDataSet);
					} else {
						NotificationHandler.deleteBirthday(this, this.data.get(index));
					}
					this.data.remove(index);

					refresh();
				}
				break;
			case REQUEST_INTENT_NOTIFICATIONS:
				if (resultCode == RESULT_OK) {
					Map<String, ?> map = (Map<String, ?>) data.getSerializableExtra(INTENT_CODE_OLD_PREFERENCES);
					NotificationHandler.handlePreferences(this, map, this.data);
				}
				break;
			case REQUEST_INTENT_FILEPICKER_IMPORT:
				if (resultCode != RESULT_OK) {
					importExportFailDialog.show();
					break;
				}
				String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
				Log.d("MainActivity","import: " + filePath);
				File file = new File(filePath);
				if (!file.exists()) {
					importExportFailDialog.show();
				}
				try {
					FileInputStream fis = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(fis);
					ObjectInputStream ois = new ObjectInputStream(bis);

					ArrayList<DataSet> importedData = (ArrayList<DataSet>) ois.readObject();

					ois.close();
					bis.close();
					fis.close();

					if (importedData.size() + this.data.size() >= MAXIMUM_DATA_SIZE) {
						new AlertDialog.Builder(this).
								setMessage(getString(R.string.import_export_maximum_data_size_text, MAXIMUM_DATA_SIZE)).
								setTitle(R.string.import_export_maximum_data_size_title).
								setNeutralButton(R.string.ok, null).
								show();
						return;
					}

					for (DataSet dataSet : importedData) {
						dataSet.id = IdGenerator.getNewId(this);
						this.data.add(dataSet);
						NotificationHandler.addBirthday(this, dataSet);
					}
					refresh();

					new AlertDialog.Builder(this).
							setMessage(R.string.import_export_import_successfull_text).
							setTitle(R.string.import_export_import_successfull_title).
							setNeutralButton(R.string.ok, null).
							show();

				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
					importExportFailDialog.show();
				}
				break;
		}
	}

	private void refresh() {
		if (data == null) {
			data = new ArrayList<>();
		}
		Collections.sort(data);
		storageHandler.saveData(data);
		dataListViewAdapter.clear();
		dataListViewAdapter.addAll(data);
		dataListViewAdapter.notifyDataSetChanged();

		TextView tv = (TextView) findViewById(R.id.activity_main_textview_nothing_to_show);
		if (data.size() == 0) {
			tv.setVisibility(View.VISIBLE);
		} else {
			tv.setVisibility(View.GONE);
		}
	}

	private DialogInterface.OnClickListener exportClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			String state = Environment.getExternalStorageState();
			if (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
				File dir = new File(Environment.getExternalStorageDirectory(), FILE_EXPORT_DIRECTORY);
				if (!dir.exists() && !dir.mkdir()) {
					Log.d("MainActivity","cancelling export. directory " + dir.getAbsolutePath() + " not creatable");
					importExportStorageFailDialog.show();
				}
				String filename = FILE_EXPORT_NAME + sdf.format(new Date()) + FILE_EXPORT_EXTENSION;
				File output = new File(dir + File.separator + filename);
				if (data == null) {
					importExportFailDialog.show();
				}
				try {
					FileOutputStream fos = new FileOutputStream(output);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					ObjectOutputStream oos = new ObjectOutputStream(bos);

					oos.writeObject(data);

					oos.close();
					bos.close();
					fos.close();

					new AlertDialog.Builder(MainActivity.this).
							setTitle(R.string.import_export_export_successfull_title).
							setMessage(getString(R.string.import_export_export_successfull_text, output.getAbsolutePath())).
							setNeutralButton(R.string.ok, null).
							show();
				} catch (IOException e) {
					e.printStackTrace();
					importExportStorageFailDialog.show();
				}
			} else {
				Log.d("MainActivity","cancelling export. state: " + state);
				importExportStorageFailDialog.show();
			}
		}
	};

	private DialogInterface.OnClickListener importClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			new MaterialFilePicker()
					.withActivity(MainActivity.this)
					.withRequestCode(REQUEST_INTENT_FILEPICKER_IMPORT)
					.withFilter(Pattern.compile(".*\\.birthdays")) // Filtering files and directories by file name using regexp
					.start();
		}
	};

}

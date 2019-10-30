package com.woodplantation.geburtstagsverwaltung.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.woodplantation.geburtstagsverwaltung.notifications.AlarmCreator;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;
import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.notifications.IdGenerator;
import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.storage.StorageHandler;
import com.woodplantation.geburtstagsverwaltung.util.IntentCodes;
import com.woodplantation.geburtstagsverwaltung.widget.WidgetService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 5;
	public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 6;
	public static final int REQUEST_PERMISSION_SET_ALARM = 7;

	private static final String FILE_EXPORT_DIRECTORY = "birthdays";
	private static final String FILE_EXPORT_NAME = "export_";
	private static final String FILE_EXPORT_EXTENSION = ".birthdays";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.US);

	private ArrayList<DataSet> data;
	private StorageHandler storageHandler;
	private DataListViewAdapter dataListViewAdapter;

	private Comparator<DataSet> comparator = new DataSet.NextBirthdayComparator();

	private AlertDialog importExportFailDialog;
	private AlertDialog importExportStorageFailDialog;
	private AlertDialog.Builder importExportPermissionFailDialogBuilder;
	private AlertDialog failureLoadingDataDialog;

	private FloatingActionButton fab;

	private void initDialogs() {
		importExportFailDialog =
				new AlertDialog.Builder(this).
						setTitle(R.string.import_export_failed_title).
						setMessage(R.string.import_export_failed_text).
						setNeutralButton(R.string.ok, null).
						create();
		importExportStorageFailDialog =
				new AlertDialog.Builder(MainActivity.this).
						setTitle(R.string.import_export_storage_error_title).
						setMessage(R.string.import_export_storage_error_text).
						setNeutralButton(R.string.ok, null).
						create();
		importExportPermissionFailDialogBuilder =
				new AlertDialog.Builder(MainActivity.this).
						setTitle(R.string.import_export_permissions_fail_title).
						setMessage(R.string.import_export_permissions_fail_text).
						setNegativeButton(R.string.cancel, null);
		failureLoadingDataDialog =
				new AlertDialog.Builder(this).
						setTitle(R.string.failure_loading_data_title).
						setMessage(R.string.failure_loading_data_text).
						setPositiveButton(R.string.ok, null).
						create();
	}

	//Code adapted from https://developer.android.com/training/notify-user/channels.html
	private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            String channelId = getString(R.string.notification_channel_id);
            CharSequence name = getString(R.string.notification_channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(channelId, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(
                    NotificationManager.class);
            notificationManager.createNotificationChannel(mChannel);
        }
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//initialize layout and toolbar
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.activity_main_label);
		setSupportActionBar(toolbar);

		fab = findViewById(R.id.fab);

		//execute code one time to create alarms
		if (new MyPreferences(this, MyPreferences.FILEPATH_SETTINGS).getFirstTimeCall()) {
			AlarmCreator.createFromScratch(this);
		}

		//check about alarm permissions
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.SET_ALARM},
				REQUEST_PERMISSION_SET_ALARM);

		//bind the list layout to data adapter
		dataListViewAdapter = new DataListViewAdapter(this, R.layout.data_list_view_item);
		ListView dataListView = findViewById(R.id.data_list_view);
		dataListView.setAdapter(dataListViewAdapter);
		dataListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DataSet dataSet = dataListViewAdapter.getItem(position);
				Intent editIntent = new Intent(MainActivity.this, EditActivity.class);
				editIntent.putExtra(IntentCodes.INDEX, data.indexOf(dataSet));
				editIntent.putExtra(IntentCodes.DATASET, dataSet);
				startActivityForResult(editIntent, REQUEST_INTENT_EDIT_DATA_SET);
			}
		});

		//initialize storage handler
		storageHandler = new StorageHandler(this);
		if ((data = storageHandler.loadData()) == null) {
			failureLoadingDataDialog.show();
		}

		//init dialogs
		initDialogs();

		//init notification channel
        initNotificationChannel();
	}

	@Override
	public void onResume() {
		super.onResume();
		refresh();
		MyPreferences preferences = new MyPreferences(this, MyPreferences.FILEPATH_SETTINGS);
		if (preferences.getDisplayFAB()) {
			fab.show();
		} else {
			fab.hide();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	public void onAddClick(@Nullable View v) {
		//check for entry limit
		if (data.size() >= getResources().getInteger(R.integer.MAXIMUM_DATA_SIZE)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.maximum_data_achieved_text, getResources().getInteger(R.integer.MAXIMUM_DATA_SIZE)));
			builder.setTitle(R.string.maximum_data_achieved_title);
			builder.setNeutralButton(R.string.ok, null);
			builder.show();
			return;
		}

		//get new id
		int id = IdGenerator.getNewId(this);
		if (id == -1) {
			failureLoadingDataDialog.show();
			return;
		}

		//open the create activity and pass the new id
		Intent intent = new Intent(MainActivity.this, AddActivity.class);
		intent.putExtra(IntentCodes.NEW_ID, id);
		startActivityForResult(intent, REQUEST_INTENT_CREATE_DATA_SET);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

			case R.id.main_info: {
				Intent intent = new Intent(this, InfoActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.main_rate: {
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
				return true;
			}
			case R.id.main_notifications: {
				Intent intent = new Intent(this, NotificationsActivity.class);
				startActivityForResult(intent, REQUEST_INTENT_NOTIFICATIONS);
				return true;
			}
			case R.id.main_import_export: {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.import_export_dialog_title);
				builder.setMessage(getString(R.string.import_export_dialog_text, getResources().getInteger(R.integer.MAXIMUM_DATA_SIZE)));
				builder.setPositiveButton(R.string.import_export_export, exportClickListener);
				builder.setNeutralButton(R.string.import_export_import, importClickListener);
				builder.show();
				return true;
			}
			case R.id.main_settings: {
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.main_add: {
				onAddClick(null);
				return true;
			}
			case R.id.main_sort_next: {
				comparator = new DataSet.NextBirthdayComparator();
				item.setChecked(true);
				refresh();
				return true;
			}
			case R.id.main_sort_calendric: {
				comparator = new DataSet.CalendarComparator();
				item.setChecked(true);
				refresh();
				return true;
			}
			case R.id.main_sort_lexicographic: {
				comparator = new DataSet.NameComparator();
				item.setChecked(true);
				refresh();
				return true;
			}
			case R.id.main_sort_age: {
				comparator = new DataSet.AgeComparator();
				item.setChecked(true);
				refresh();
				return true;
			}
			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("mainactivity", "onactivity result! requestcode: " + requestCode + " resultCode : " + resultCode);
		switch (requestCode) {

			case REQUEST_INTENT_CREATE_DATA_SET: {
				if (resultCode == RESULT_OK) {
					DataSet dataSet = (DataSet) data.getSerializableExtra(IntentCodes.DATASET);
					this.data.add(dataSet);
					WidgetService.notifyDataChanged(this);
				}
				break;
			}
			case REQUEST_INTENT_EDIT_DATA_SET: {
				if (resultCode == RESULT_OK) {
					DataSet newDataSet = (DataSet) data.getSerializableExtra(IntentCodes.DATASET);
					int index = data.getIntExtra(IntentCodes.INDEX, -1);
					if (index == -1) {
						return;
					}
					if (newDataSet != null) {
						this.data.add(newDataSet);
					}
					this.data.remove(index);
					WidgetService.notifyDataChanged(this);
				}
				break;
			}
			case REQUEST_INTENT_NOTIFICATIONS: {
				if (resultCode == RESULT_OK) {
					Map<String, ?> map = (Map<String, ?>) data.getSerializableExtra(IntentCodes.OLD_PREFERENCES);
					AlarmCreator.preferencesChanged(this, map);
				}
				break;
			}
			case REQUEST_INTENT_FILEPICKER_IMPORT: {
				if (resultCode != RESULT_OK) {
					importExportFailDialog.show();
					break;
				}
				String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
				Log.d("MainActivity", "import: " + filePath);
				File file = new File(filePath);
				if (!file.exists()) {
					importExportFailDialog.show();
				}
				try {
					ArrayList<DataSet> importedData = new ArrayList<>();
					try {
						//first try block: for json reading
						FileReader fr = new FileReader(file);
						BufferedReader br = new BufferedReader(fr);

						JSONArray jsonArray = new JSONArray(br.readLine());
						for (int i = 0; i < jsonArray.length(); i++) {
							importedData.add(new DataSet((JSONObject) jsonArray.get(i)));
						}

						br.close();
						fr.close();
					} catch (JSONException e) {
						e.printStackTrace();
						//if json fails: try old reading method (using serializable interface)
						FileInputStream fis = new FileInputStream(file);
						BufferedInputStream bis = new BufferedInputStream(fis);
						ObjectInputStream ois = new ObjectInputStream(bis);

						importedData = (ArrayList<DataSet>) ois.readObject();

						ois.close();
						bis.close();
						fis.close();
					}

					if (importedData.size() + this.data.size() >= getResources().getInteger(R.integer.MAXIMUM_DATA_SIZE)) {
						new AlertDialog.Builder(this).
								setMessage(getString(R.string.import_export_maximum_data_size_text, getResources().getInteger(R.integer.MAXIMUM_DATA_SIZE))).
								setTitle(R.string.import_export_maximum_data_size_title).
								setNeutralButton(R.string.ok, null).
								show();
						return;
					}

					for (DataSet dataSet : importedData) {
						dataSet.id = IdGenerator.getNewId(this);
						this.data.add(dataSet);
					}
					WidgetService.notifyDataChanged(this);

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
			case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE: {
				exportWithPermissionCheck();
				break;
			}
			case REQUEST_PERMISSION_READ_EXTERNAL_STORAGE: {
				importWithPermissionCheck();
				break;
			}
		}
	}

	public static ArrayList<DataSet> sortAndStoreData(StorageHandler storageHandler,
												 ArrayList<DataSet> dataList,
												 Comparator<DataSet> comparator) {
		if (dataList == null) {
			dataList = new ArrayList<>();
		}
		try {
			Collections.sort(dataList, comparator);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		storageHandler.saveData(dataList);
		return dataList;
	}

	private void refresh() {
		data = sortAndStoreData(storageHandler, data, comparator);
		dataListViewAdapter.clear();
		dataListViewAdapter.addAll(data);
		dataListViewAdapter.notifyDataSetChanged();

		TextView tv = findViewById(R.id.activity_main_textview_nothing_to_show);
		if (data.size() == 0) {
			tv.setVisibility(View.VISIBLE);
		} else {
			tv.setVisibility(View.GONE);
		}
	}

	private void exportWithPermissionCheck() {
		//check permissions to write external storage
		if (PackageManager.PERMISSION_GRANTED ==
				ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			exportWithoutPermissionCheck();
		} else {
			//no permission to write external storage. ask for it.
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
		}
	}

	private void exportWithoutPermissionCheck() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			File dir = new File(Environment.getExternalStorageDirectory(), FILE_EXPORT_DIRECTORY);
			if (!dir.exists() && !dir.mkdir()) {
				importExportStorageFailDialog.show();
			}
			String filename = FILE_EXPORT_NAME + sdf.format(new Date()) + FILE_EXPORT_EXTENSION;
			File output = new File(dir + File.separator + filename);
			if (data == null) {
				importExportFailDialog.show();
			}
			JSONArray jsonArray = new JSONArray();
			for (DataSet dataSet : data) {
				jsonArray.put(dataSet.toJSON());
			}
			try {
				FileWriter fw = new FileWriter(output);
				BufferedWriter bw = new BufferedWriter(fw);

				bw.write(jsonArray.toString());

				bw.close();
				fw.close();

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
			importExportStorageFailDialog.show();
		}
	}

	private void importWithPermissionCheck() {
		//check permissions to read external storage
		if (PackageManager.PERMISSION_GRANTED ==
				ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
			importWithoutPermissionCheck();
		} else {
			//no permission to read external storage. ask for it.
			ActivityCompat.requestPermissions(MainActivity.this,
					new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
					REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);
		}
	}

	private void importWithoutPermissionCheck() {
		new MaterialFilePicker()
				.withActivity(MainActivity.this)
				.withRequestCode(REQUEST_INTENT_FILEPICKER_IMPORT)
				.withFilter(Pattern.compile(".*\\.birthdays")) // Filtering files and directories by file name using regexp
				.start();
	}

	private DialogInterface.OnClickListener exportClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			exportWithPermissionCheck();
		}
	};

	private DialogInterface.OnClickListener importClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			importWithPermissionCheck();
		}
	};

	private class PermissionSettingsClickListener implements DialogInterface.OnClickListener {
		private int requestCode;
		PermissionSettingsClickListener(int requestCode) {
			this.requestCode = requestCode;
		}

		@Override
		public void onClick(DialogInterface dialogInterface, int i) {
			Intent intent = new Intent();
			intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			Uri uri = Uri.fromParts("package", getPackageName(), null);
			intent.setData(uri);
			startActivityForResult(intent, requestCode);
		}
	}

	private DialogInterface.OnClickListener exportPermissionSettingsClickListener = new PermissionSettingsClickListener(REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
	private DialogInterface.OnClickListener importPermissionSettingsClickListener = new PermissionSettingsClickListener(REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					exportWithoutPermissionCheck();
				} else {
					if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
						//ask again
						importExportPermissionFailDialogBuilder.setPositiveButton(R.string.allow, exportClickListener);
					} else {
						//go to settings
						importExportPermissionFailDialogBuilder.setPositiveButton(R.string.allow, exportPermissionSettingsClickListener);
					}
					importExportPermissionFailDialogBuilder.show();
				}
				break;
			}
			case REQUEST_PERMISSION_READ_EXTERNAL_STORAGE: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					importWithoutPermissionCheck();
				} else {
					if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))  {
						//ask again
						importExportPermissionFailDialogBuilder.setPositiveButton(R.string.allow, importClickListener);
					} else {
						//go to settings
						importExportPermissionFailDialogBuilder.setPositiveButton(R.string.allow, importPermissionSettingsClickListener);
					}
					importExportPermissionFailDialogBuilder.show();
				}
				break;
			}
			case REQUEST_PERMISSION_SET_ALARM: {
				if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
					if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SET_ALARM)) {
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setTitle(R.string.set_alarm_permissions_explanation_title);
						builder.setMessage(R.string.set_alarm_permissions_explanation_text);
						builder.setCancelable(false);
						builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								ActivityCompat.requestPermissions(MainActivity.this,
										new String[] {Manifest.permission.SET_ALARM},
										REQUEST_PERMISSION_SET_ALARM);
							}
						});
						builder.show();
					}
				}
			}
		}
	}

}

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
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoDataToExportException;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoStorageAvailableException;
import com.woodplantation.geburtstagsverwaltung.exceptions.UnableToCreateDirectoryException;
import com.woodplantation.geburtstagsverwaltung.notifications.AlarmCreator;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;
import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.storage.StorageHandler;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;
import com.woodplantation.geburtstagsverwaltung.util.SortingCategory;
import com.woodplantation.geburtstagsverwaltung.view.DataAdapter;
import com.woodplantation.geburtstagsverwaltung.viewmodel.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

	private MainViewModel mainViewModel;
	@Inject
	MyPreferences myPreferences;
	@Inject
	Repository repository;

	public static final int REQUEST_INTENT_FILEPICKER_IMPORT = 4;
	public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 5;
	public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 6;
	public static final int REQUEST_PERMISSION_SET_ALARM = 7;

	public static final String FILE_EXPORT_DIRECTORY = "birthdays";
	public static final String FILE_EXPORT_NAME = "export_";
	public static final String FILE_EXPORT_EXTENSION = ".birthdays";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.US);

	private AlertDialog importExportFailDialog;
	private AlertDialog.Builder importExportPermissionFailDialogBuilder;

	private FloatingActionButton fab;

	private void initDialogs() {
		importExportFailDialog =
				new AlertDialog.Builder(this).
						setTitle(R.string.import_export_failed_title).
						setMessage(R.string.import_export_failed_text).
						setNeutralButton(R.string.ok, null).
						create();
		importExportPermissionFailDialogBuilder =
				new AlertDialog.Builder(MainActivity.this).
						setTitle(R.string.import_export_permissions_fail_title).
						setMessage(R.string.import_export_permissions_fail_text).
						setNegativeButton(R.string.cancel, null);
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

		mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

		// init recyclerview, observe the livedata and connect it to recyclerview
		RecyclerView dataView = findViewById(R.id.data_view);
		dataView.setLayoutManager(new LinearLayoutManager(this));
		dataView.setHasFixedSize(true);
		DataAdapter adapter = new DataAdapter(this);
		dataView.setAdapter(adapter);
		mainViewModel.getData().observe(this, adapter::submitList);

		// check if this is first app start
		if (myPreferences.getFirstTimeCall()) {
			// if it is first app start, create alarms and set migrations to true because they
			// are not necessary
			AlarmCreator.createFromScratch(this, myPreferences);
			myPreferences.preferences
					.edit()
					.putBoolean(getString(R.string.preferences_preferences_migrated), true)
					.putBoolean(getString(R.string.preferences_storage_migrated), true)
					.apply();
		}

		//check about alarm permissions
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.SET_ALARM},
				REQUEST_PERMISSION_SET_ALARM);

		//init dialogs
		initDialogs();

		//init notification channel
        initNotificationChannel();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (myPreferences.getDisplayFAB()) {
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
		Intent intent = new Intent(MainActivity.this, AddActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.main_info) {
			Intent intent = new Intent(this, InfoActivity.class);
			startActivity(intent);
			return true;
		} else if (itemId == R.id.main_rate) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.rating_title);
			builder.setMessage(R.string.rating_text);
			builder.setPositiveButton(R.string.rate_app, (dialog, which) -> {
				String packageName = getPackageName();
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
				} catch (ActivityNotFoundException e) {
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
				}
			});
			builder.setNegativeButton(R.string.cancel, null);
			builder.show();
			return true;
		} else if (itemId == R.id.main_notifications) {
			Intent intent = new Intent(this, NotificationsActivity.class);
			startActivity(intent);
			return true;
		} else if (itemId == R.id.main_import_export) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.import_export_dialog_title);
			builder.setMessage(getString(R.string.import_export_dialog_text, getResources().getInteger(R.integer.MAXIMUM_DATA_SIZE)));
			builder.setPositiveButton(R.string.import_export_export, exportClickListener);
			builder.setNeutralButton(R.string.import_export_import, importClickListener);
			builder.show();
			return true;
		} else if (itemId == R.id.main_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		} else if (itemId == R.id.main_add) {
			onAddClick(null);
			return true;
		} else if (itemId == R.id.main_sort_next) {
			mainViewModel.sortingCategoryClicked(SortingCategory.NEXT_BIRTHDAY);
			item.setChecked(true);
			return true;
		} else if (itemId == R.id.main_sort_calendric) {
			mainViewModel.sortingCategoryClicked(SortingCategory.CALENDRIC);
			item.setChecked(true);
			return true;
		} else if (itemId == R.id.main_sort_lexicographic) {
			mainViewModel.sortingCategoryClicked(SortingCategory.LEXICOGRAPHIC);
			item.setChecked(true);
			return true;
		} else if (itemId == R.id.main_sort_age) {
			mainViewModel.sortingCategoryClicked(SortingCategory.AGE);
			item.setChecked(true);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_INTENT_FILEPICKER_IMPORT: {
				if (resultCode != RESULT_OK) {
					importExportFailDialog.show();
					break;
				}
				String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
				repository.importData(filePath);
				/*Log.d("MainActivity", "import: " + filePath);
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
				}*/
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
		repository.exportData(path -> {
			new AlertDialog.Builder(MainActivity.this).
					setTitle(R.string.import_export_export_successful_title).
					setMessage(getString(R.string.import_export_export_successful_text, path)).
					setNeutralButton(R.string.ok, null).
					show();
		}, error -> {
			String reason;
			if (error instanceof NoDataToExportException) {
				reason = getString(R.string.no_data_to_export);
			} else if (error instanceof NoStorageAvailableException) {
				reason = getString(R.string.no_storage_available);
			} else if (error instanceof UnableToCreateDirectoryException) {
				reason = getString(R.string.unable_to_create_directory);
			} else {
				reason = error.getLocalizedMessage();
			}
			new AlertDialog.Builder(MainActivity.this).
					setTitle(R.string.export_failed_title).
					setMessage(getString(R.string.export_failed_text, reason)).
					setNeutralButton(R.string.ok, null).
					show();
		});
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

	private final DialogInterface.OnClickListener exportClickListener = (dialogInterface, i) -> exportWithPermissionCheck();

	private final DialogInterface.OnClickListener importClickListener = (dialogInterface, i) -> importWithPermissionCheck();

	private class PermissionSettingsClickListener implements DialogInterface.OnClickListener {
		private final int requestCode;
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

	private final DialogInterface.OnClickListener exportPermissionSettingsClickListener = new PermissionSettingsClickListener(REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
	private final DialogInterface.OnClickListener importPermissionSettingsClickListener = new PermissionSettingsClickListener(REQUEST_PERMISSION_READ_EXTERNAL_STORAGE);

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
						builder.setNeutralButton(R.string.ok, (dialogInterface, i) ->
								ActivityCompat.requestPermissions(MainActivity.this,
										new String[] {Manifest.permission.SET_ALARM},
										REQUEST_PERMISSION_SET_ALARM
								)
						);
						builder.show();
					}
				}
			}
		}
	}

}

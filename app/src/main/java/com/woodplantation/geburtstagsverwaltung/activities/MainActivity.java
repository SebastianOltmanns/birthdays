package com.woodplantation.geburtstagsverwaltung.activities;

import android.Manifest;
import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoDataToExportException;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoIdToDeleteException;
import com.woodplantation.geburtstagsverwaltung.exceptions.UnableToCreateFileException;
import com.woodplantation.geburtstagsverwaltung.exceptions.UnableToOpenFileException;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.notifications.AlarmCreator;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;
import com.woodplantation.geburtstagsverwaltung.util.IntentCodes;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;
import com.woodplantation.geburtstagsverwaltung.util.SortingCategory;
import com.woodplantation.geburtstagsverwaltung.view.AppTheme;
import com.woodplantation.geburtstagsverwaltung.view.DataAdapter;
import com.woodplantation.geburtstagsverwaltung.view.RecyclerItemClickListener;
import com.woodplantation.geburtstagsverwaltung.viewmodel.MainViewModel;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

	private MainViewModel mainViewModel;
	@Inject
	MyPreferences myPreferences;
	@Inject
	Repository repository;
	@Inject
	AppTheme appTheme;

	public static final int REQUEST_PERMISSION_SET_ALARM = 7;
	public static final int REQUEST_EXPORT_FILE_CREATE = 8;
	public static final int REQUEST_IMPORT_FILE_OPEN = 9;
	public static final int REQUEST_SETTINGS_ACTIVITY = 10;

	public static final String FILE_EXPORT_NAME = "export_";
	public static final String FILE_EXPORT_EXTENSION = ".birthdays";

	private FloatingActionButton fab;

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
		appTheme.applyAppTheme(this);
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
		mainViewModel.getData().observe(this, data -> adapter.submitList(data == null ? null : new ArrayList<>(data)));
		// connect recyclerview with click listener
		dataView.addOnItemTouchListener(
				new RecyclerItemClickListener(this, dataView, new RecyclerItemClickListener.OnItemClickListener() {
					@Override public void onItemClick(View view, int position) {
						// start edit
						Intent intent = new Intent(MainActivity.this, InputActivity.class);
						intent.putExtra(IntentCodes.ID, mainViewModel.getData().getValue().get(position).id);
						startActivity(intent);
					}

					@Override public void onLongItemClick(View view, int position) {
						Entry toDelete = mainViewModel.getData().getValue().get(position);
						new AlertDialog.Builder(MainActivity.this)
								.setTitle(R.string.sure_delete_title)
								.setMessage(getString(R.string.sure_delete_text_with_name, toDelete.getFullName()))
								.setNegativeButton(R.string.cancel, null)
								.setPositiveButton(R.string.yes, (a, b) ->
										repository.deleteData(
												toDelete.id,
												() -> {},
												error -> new AlertDialog.Builder(MainActivity.this)
														.setTitle(R.string.delete_failed_title)
														.setMessage(getString(R.string.delete_failed_text, error instanceof NoIdToDeleteException ? getString(R.string.no_id_to_delete) : error.getLocalizedMessage()))
														.setNeutralButton(R.string.ok, null)
														.show()
										)
								)
								.show();
					}
				})
		);

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
		SortingCategory sortingCategory = mainViewModel.getSortingCategory().getValue();
		if (sortingCategory == null) {
			sortingCategory = SortingCategory.NEXT_BIRTHDAY;
			mainViewModel.sortingCategoryClicked(sortingCategory);
		}
		switch (sortingCategory) {
			case AGE: {
				mainViewModel.sortingCategoryClicked(SortingCategory.AGE);
				menu.findItem(R.id.main_sort_age).setChecked(true);
			}
			case CALENDRIC: {
				menu.findItem(R.id.main_sort_calendric).setChecked(true);
			}
			case LEXICOGRAPHIC_FULL_NAME: {
				menu.findItem(R.id.main_sort_lexicographic_full_name).setChecked(true);
			}
			case LEXICOGRAPHIC_LAST_NAME: {
				menu.findItem(R.id.main_sort_lexicographic_last_name).setChecked(true);
			}
			case NEXT_BIRTHDAY: {
				menu.findItem(R.id.main_sort_next).setChecked(true);
			}
		}
		return true;
	}

	public void onAddClick(@Nullable View v) {
		Intent intent = new Intent(MainActivity.this, InputActivity.class);
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
			builder.setMessage(R.string.import_export_dialog_text);
			builder.setPositiveButton(R.string.import_export_export, exportClickListener);
			builder.setNeutralButton(R.string.import_export_import, importClickListener);
			builder.show();
			return true;
		} else if (itemId == R.id.main_settings) {
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivityForResult(intent, REQUEST_SETTINGS_ACTIVITY);
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
		} else if (itemId == R.id.main_sort_lexicographic_full_name) {
			mainViewModel.sortingCategoryClicked(SortingCategory.LEXICOGRAPHIC_FULL_NAME);
			item.setChecked(true);
			return true;
		} else if (itemId == R.id.main_sort_lexicographic_last_name) {
			mainViewModel.sortingCategoryClicked(SortingCategory.LEXICOGRAPHIC_LAST_NAME);
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
			case REQUEST_EXPORT_FILE_CREATE: {
				if (resultCode == Activity.RESULT_OK) {
					try {
						repository.exportData(
								getContentResolver().openFileDescriptor(data.getData(), "w"),
								() -> new AlertDialog.Builder(MainActivity.this).
										setTitle(R.string.export_successful_title).
										setMessage(getString(R.string.export_successful_text)).
										setNeutralButton(R.string.ok, null).
										show(),
								this::exportFailedHandler
						);
					} catch (FileNotFoundException e) {
						exportFailedHandler(e);
					}
				} else {
					exportFailedHandler(new UnableToCreateFileException());
				}
				break;
			}
			case REQUEST_IMPORT_FILE_OPEN: {
				if (resultCode == Activity.RESULT_OK) {
					repository.importData(
							data.getData(),
							getContentResolver(),
							() -> new AlertDialog.Builder(MainActivity.this).
									setTitle(R.string.import_successful_title).
									setMessage(getString(R.string.import_successful_text)).
									setNeutralButton(R.string.ok, null).
									show(),
							this::importFailedHandler
					);
				} else {
					importFailedHandler(new UnableToOpenFileException());
				}
				break;
			}
			case REQUEST_SETTINGS_ACTIVITY: {
				if (resultCode == Activity.RESULT_OK) {
					if (data.getBooleanExtra(IntentCodes.THEME_CHANGED, false)) {
						recreate();
					}
				}
				break;
			}
		}
	}

	private void exportFailedHandler(Throwable error) {
		String reason;
		if (error instanceof NoDataToExportException) {
			reason = getString(R.string.no_data_to_export);
		} else if (error instanceof UnableToCreateFileException) {
			reason = getString(R.string.unable_to_create_file);
		} else {
			reason = error.getLocalizedMessage();
		}
		new AlertDialog.Builder(MainActivity.this).
				setTitle(R.string.export_failed_title).
				setMessage(getString(R.string.export_failed_text, reason)).
				setNeutralButton(R.string.ok, null).
				show();
	}

	private void importFailedHandler(Throwable error) {
		error.printStackTrace();
		String reason;
		if (error instanceof UnableToOpenFileException) {
			reason = getString(R.string.unable_to_open_file);
		} else {
			reason = error.getLocalizedMessage();
		}
		new AlertDialog.Builder(MainActivity.this).
				setTitle(R.string.import_failed_title).
				setMessage(getString(R.string.import_failed_text, reason)).
				setNeutralButton(R.string.ok, null).
				show();
	}
	private final DialogInterface.OnClickListener exportClickListener = (dialogInterface, i) -> {
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.setType("application/json");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_TITLE,
				MainActivity.FILE_EXPORT_NAME
						+ DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.US).format(LocalDateTime.now())
						+ MainActivity.FILE_EXPORT_EXTENSION);
		startActivityForResult(intent, REQUEST_EXPORT_FILE_CREATE);
	};

	private final DialogInterface.OnClickListener importClickListener = (dialogInterface, i) -> {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, REQUEST_IMPORT_FILE_OPEN);
	};

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_PERMISSION_SET_ALARM) {
			if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
				if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SET_ALARM)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(R.string.set_alarm_permissions_explanation_title);
					builder.setMessage(R.string.set_alarm_permissions_explanation_text);
					builder.setCancelable(false);
					builder.setNeutralButton(R.string.ok, (dialogInterface, i) ->
							ActivityCompat.requestPermissions(MainActivity.this,
									new String[]{Manifest.permission.SET_ALARM},
									REQUEST_PERMISSION_SET_ALARM
							)
					);
					builder.show();
				}
			}
		}
	}

}

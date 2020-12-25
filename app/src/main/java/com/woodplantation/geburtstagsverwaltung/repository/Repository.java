package com.woodplantation.geburtstagsverwaltung.repository;

import android.content.Context;
import android.os.Build;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.database.EntryDao;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.storage.StorageHandler;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

public class Repository {

    private final EntryDao entryDao;
    private final StorageHandler storageHandler;
    private final MyPreferences myPreferences;
    private final Context context;

    @Inject
    public Repository(EntryDao entryDao, StorageHandler storageHandler, MyPreferences myPreferences, @ApplicationContext Context context) {
        this.entryDao = entryDao;
        this.storageHandler = storageHandler;
        this.myPreferences = myPreferences;
        this.context = context;

        migrateTwoPreferencesToOne();
        migrateStorageToDb();
    }

    private void migrateStorageToDb() {
        if (!myPreferences.isStorageMigrated()) {
            // load current data
            ArrayList<DataSet> data = storageHandler.loadData();

            // iterate list and insert into db
            entryDao.insertMany(data
                    .stream()
                    .map(dataSet -> {
                        Entry entry = new Entry();
                        entry.firstName = dataSet.firstName;
                        entry.lastName = dataSet.lastName;
                        entry.ignoreYear = false;
                        entry.birthday = LocalDate.of(dataSet.birthday.get(Calendar.YEAR), dataSet.birthday.get(Calendar.MONTH), dataSet.birthday.get(Calendar.DAY_OF_MONTH));
                        entry.notes = dataSet.others;
                        return entry;
                    })
                    .collect(Collectors.toSet())
            );

            // store in preferences that we did migration
            myPreferences.preferences
                    .edit()
                    .putBoolean(context.getString(R.string.preferences_storage_migrated), true)
                    .apply();

            // remove previous storage file
            try {
                context.deleteFile(StorageHandler.filePath);
            } catch (Exception ignored) {
            }
        }
    }

    private void migrateTwoPreferencesToOne() {
        if (!myPreferences.arePreferencesMigrated()) {
            // get notifications preferences
            @SuppressWarnings("deprecation")
            MyPreferences notificationPreferences = MyPreferences.getNotificationPreferences(context);

            myPreferences.preferences
                    .edit()
                    // migrate all notification preferences to default preferences
                    .putBoolean(context.getString(R.string.preferences_active), notificationPreferences.getActive())
                    .putBoolean(context.getString(R.string.preferences_on_birthday_active), notificationPreferences.getOnBirthdayActive())
                    .putBoolean(context.getString(R.string.preferences_one_day_before_active), notificationPreferences.getOneDayBeforeActive())
                    .putBoolean(context.getString(R.string.preferences_x_days_before_active), notificationPreferences.getXDaysBeforeActive())
                    .putInt(context.getString(R.string.preferences_on_birthday_clock), notificationPreferences.getOnBirthdayClock())
                    .putInt(context.getString(R.string.preferences_one_day_before_clock), notificationPreferences.getOneDayBeforeClock())
                    .putInt(context.getString(R.string.preferences_x_days_before_clock), notificationPreferences.getXDaysBeforeClock())
                    .putInt(context.getString(R.string.preferences_x_days_before_days), notificationPreferences.getXDaysBeforeDays())
                    .putInt(
                            context.getString(R.string.preferences_notification_id),
                            notificationPreferences.preferences.getInt(
                                    context.getString(R.string.preferences_notification_id),
                                    context.getResources().getInteger(R.integer.preferences_notification_id)
                            )
                    )
                    // store that we did migration
                    .putBoolean(context.getString(R.string.preferences_preferences_migrated), true)
                    .apply();

            // delete notification preferences if sdk is new enough
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                MyPreferences.removeNotificationsPreferences(context);
            }

        }
    }

}

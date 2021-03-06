package com.woodplantation.geburtstagsverwaltung.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.notifications.AlarmCreator;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;
import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.storage.StorageHandler;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;
import com.woodplantation.geburtstagsverwaltung.widget.WidgetAlarmReceiver;
import com.woodplantation.geburtstagsverwaltung.widget.WidgetService;

import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Created by Sebu on 23.08.2017.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
@AndroidEntryPoint
public class UpdateReceiver extends BroadcastReceiver {

    @Inject
    Repository repository;
    @Inject
    MyPreferences myPreferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("updatereceiver","on receive!");
        StorageHandler storageHandler = new StorageHandler(context);
        migrateTwoPreferencesToOne(context, myPreferences);
        migrateStorageToDbAndContinueWithAlarmsAndWidget(context, myPreferences, storageHandler);
    }

    private void migrateStorageToDbAndContinueWithAlarmsAndWidget(Context context, MyPreferences preferences, StorageHandler storageHandler) {
        Log.d("update receiveer", "migrateStorageToDbAndContinueWithAlarmsAndWidget!");
        if (!preferences.isStorageMigrated()) {
            // load current data
            ArrayList<DataSet> data = storageHandler.loadData();

            // iterate list and insert into db
            repository.insertData(
                    data.stream()
                            .map(Entry::new)
                            .collect(Collectors.toSet()),
                    () -> {
                        // store in preferences that we did migration
                        preferences.preferences
                                .edit()
                                .putBoolean(context.getString(R.string.preferences_storage_migrated), true)
                                .apply();

                        // remove previous storage file
                        try {
                            context.deleteFile(StorageHandler.filePath);
                        } catch (Exception ignored) {
                        }
                        // also remove file that was used to store last id
                        try {
                            context.deleteFile("id");
                        } catch (Exception ignored) {
                        }

                        AlarmCreator.createFromScratch(context, preferences);
                        WidgetAlarmReceiver.createNextAlarm(context);
                        WidgetService.notifyDataChanged(context);
                    }, error -> {
                        AlarmCreator.createFromScratch(context, preferences);
                        WidgetAlarmReceiver.createNextAlarm(context);
                        WidgetService.notifyDataChanged(context);
                    }
            );
        } else {
            AlarmCreator.createFromScratch(context, preferences);
            WidgetAlarmReceiver.createNextAlarm(context);
            WidgetService.notifyDataChanged(context);
        }
    }

    private void migrateTwoPreferencesToOne(Context context, MyPreferences preferences) {
        if (!preferences.arePreferencesMigrated()) {
            // get notifications preferences
            @SuppressWarnings("deprecation")
            MyPreferences notificationPreferences = MyPreferences.getNotificationPreferences(context);

            preferences.preferences
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

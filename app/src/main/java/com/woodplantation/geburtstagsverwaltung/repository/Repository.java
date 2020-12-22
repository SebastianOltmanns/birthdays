package com.woodplantation.geburtstagsverwaltung.repository;

import android.content.Context;

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

        }
    }

}

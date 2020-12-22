package com.woodplantation.geburtstagsverwaltung.storage;

import android.content.Context;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.database.EntryDao;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class MigrateToDatabase {

    private final StorageHandler storageHandler;
    private final MyPreferences myPreferences;
    private final Context context;
    private final EntryDao entryDao;

    @Inject
    public MigrateToDatabase(StorageHandler storageHandler, MyPreferences myPreferences, Context context, EntryDao entryDao) {
        this.storageHandler = storageHandler;
        this.myPreferences = myPreferences;
        this.context = context;
        this.entryDao = entryDao;
    }

    public void migrate() {
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

}

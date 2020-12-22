package com.woodplantation.geburtstagsverwaltung.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.woodplantation.geburtstagsverwaltung.model.Entry;

@Database(entities = {Entry.class}, version = 1)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract EntryDao entityDao();
}

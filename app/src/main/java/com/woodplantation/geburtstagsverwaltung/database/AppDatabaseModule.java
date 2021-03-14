package com.woodplantation.geburtstagsverwaltung.database;

import android.app.Application;

import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppDatabaseModule {

    @Provides
    @Singleton
    public static AppDatabase provideAppDatabase(Application application) {
        return Room.databaseBuilder(application, AppDatabase.class, "database").build();
    }

    @Provides
    @Singleton
    public static EntryDao provideEntryDao(AppDatabase appDatabase) {
        return appDatabase.entityDao();
    }

}

package com.woodplantation.geburtstagsverwaltung.database;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import com.woodplantation.geburtstagsverwaltung.repository.Repository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;

@Module
@InstallIn(ApplicationComponent.class)
public class AppDatabaseModule {

    @Provides
    @Singleton
    public static AppDatabase provideAppDatabase(Application application) {
        return Room.databaseBuilder(application, AppDatabase.class, "database").build();
    }

    @Provides
    public static EntryDao provideEntryDao(AppDatabase appDatabase) {
        return appDatabase.entityDao();
    }

}

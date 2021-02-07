package com.woodplantation.geburtstagsverwaltung.repository;

import android.app.Application;

import androidx.room.Room;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woodplantation.geburtstagsverwaltung.database.AppDatabase;
import com.woodplantation.geburtstagsverwaltung.database.EntryDao;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;

@Module
@InstallIn(ApplicationComponent.class)
public class RepositoryModule {

    @Provides
    @Singleton
    public static Repository provideRepository(EntryDao entryDao, ObjectMapper objectMapper) {
        return new Repository(entryDao, objectMapper);
    }

}

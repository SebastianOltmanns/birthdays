package com.woodplantation.geburtstagsverwaltung.util;

import android.app.Application;

import com.woodplantation.geburtstagsverwaltung.database.AppDatabase;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;

@Module
@InstallIn(ApplicationComponent.class)
public class MyPreferencesModule {

    @Provides
    @Singleton
    public static MyPreferences provideMyPreferences(@ApplicationContext Application application) {
        return new MyPreferences(application);
    }

}

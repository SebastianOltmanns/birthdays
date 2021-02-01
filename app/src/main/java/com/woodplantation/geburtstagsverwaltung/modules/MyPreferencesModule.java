package com.woodplantation.geburtstagsverwaltung.modules;

import android.app.Application;

import com.woodplantation.geburtstagsverwaltung.database.AppDatabase;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;

@Module
@InstallIn(ApplicationComponent.class)
public class MyPreferencesModule {

    @Provides
    @Singleton
    public static MyPreferences provideMyPreferences(Application application) {
        return new MyPreferences(application);
    }

}

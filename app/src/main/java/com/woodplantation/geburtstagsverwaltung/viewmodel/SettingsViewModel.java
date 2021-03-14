package com.woodplantation.geburtstagsverwaltung.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.woodplantation.geburtstagsverwaltung.view.AppTheme;

public class SettingsViewModel extends ViewModel {

    public final MutableLiveData<Boolean> displayFab = new MutableLiveData<>();
    public final MutableLiveData<AppTheme.Theme> theme = new MutableLiveData<>();

}

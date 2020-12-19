package com.woodplantation.geburtstagsverwaltung.viewmodel;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.ViewModel;

import com.woodplantation.geburtstagsverwaltung.repository.Repository;

public class MainViewModel extends ViewModel {

    private final Repository repository;

    @ViewModelInject
    public MainViewModel(Repository repository) {
        this.repository = repository;
    }
}

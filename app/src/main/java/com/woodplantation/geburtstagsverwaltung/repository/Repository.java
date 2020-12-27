package com.woodplantation.geburtstagsverwaltung.repository;

import androidx.lifecycle.LiveData;

import com.woodplantation.geburtstagsverwaltung.database.EntryDao;
import com.woodplantation.geburtstagsverwaltung.model.Entry;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;

public class Repository {

    private final EntryDao entryDao;
    //TODO cancel compositedisposable when application closes
    public final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public Repository(EntryDao entryDao) {
        this.entryDao = entryDao;
    }

    public LiveData<List<Entry>> getData() {
        return entryDao.getAll();
    }

    public void insertData(Set<Entry> data, Action onComplete) {
        compositeDisposable.add(
                entryDao.insertMany(data).subscribe(onComplete)
        );
    }

}

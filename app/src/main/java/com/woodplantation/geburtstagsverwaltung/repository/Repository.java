package com.woodplantation.geburtstagsverwaltung.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import androidx.lifecycle.LiveData;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woodplantation.geburtstagsverwaltung.database.EntryDao;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoDataToExportException;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.widget.WidgetService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class Repository {

    private final EntryDao entryDao;
    private final ObjectMapper objectMapper;
    //TODO cancel compositedisposable when application closes
    public final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Context context;

    @Inject
    public Repository(EntryDao entryDao, ObjectMapper objectMapper, @ApplicationContext Context context) {
        this.entryDao = entryDao;
        this.objectMapper = objectMapper;
        this.context = context;
    }

    public LiveData<List<Entry>> getData() {
        return entryDao.getAll();
    }

    public void insertData(Set<Entry> data, Action onSuccess, Consumer<Throwable> onFailure) {
        compositeDisposable.add(
                entryDao.insertMany(data)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            onSuccess.run();
                            WidgetService.notifyDataChanged(context);
                        }, onFailure)
        );
    }

    public void insertData(Entry data, Action onSuccess, Consumer<Throwable> onFailure) {
        compositeDisposable.add(
                entryDao.insert(data)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            onSuccess.run();
                            WidgetService.notifyDataChanged(context);
                        }, onFailure)
        );
    }

    public void exportData(ParcelFileDescriptor pfd, Action onSuccess, Consumer<Throwable> onFailure) {
        compositeDisposable.add(
                entryDao.getAllRx()
                        .flatMapCompletable(data -> Completable.fromAction(() -> {
                            if (data.isEmpty()) {
                                throw new NoDataToExportException();
                            }
                            FileOutputStream fileOutputStream =
                                    new FileOutputStream(pfd.getFileDescriptor());
                            objectMapper.writeValue(fileOutputStream, data);
                            // Let the document provider know you're done by closing the stream.
                            fileOutputStream.close();
                            pfd.close();
                        }))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(onSuccess, onFailure)
        );
    }

    public void importData(Uri uri, ContentResolver contentResolver, Action onSuccess, Consumer<Throwable> onFailure) {
        compositeDisposable.add(
                Single
                        .fromCallable(() -> {
                            // VARIANT 1
                            // try to read as new entry set
                            try {
                                return objectMapper.readValue(contentResolver.openInputStream(uri), new TypeReference<Set<Entry>>() {});
                            } catch (JsonParseException | JsonMappingException e1) {
                                e1.printStackTrace();
                                // VARIANT 2
                                // try to read as old set of DataSet and map it to entry
                                try {
                                    return objectMapper.readValue(contentResolver.openInputStream(uri), new TypeReference<Set<DataSet>>() {
                                    })
                                            .stream()
                                            .map(Entry::new)
                                            .collect(Collectors.toSet());
                                } catch (JsonParseException | JsonMappingException e2) {
                                    e2.printStackTrace();
                                    // VARIANT 3
                                    // try to read with old method via serializable interface
                                    InputStream is = contentResolver.openInputStream(uri);
                                    BufferedInputStream bis = new BufferedInputStream(is);
                                    ObjectInputStream ois = new ObjectInputStream(bis);

                                    ArrayList<DataSet> data = (ArrayList<DataSet>) ois.readObject();

                                    ois.close();
                                    bis.close();
                                    is.close();
                                    return data
                                            .stream()
                                            .map(Entry::new)
                                            .collect(Collectors.toSet());
                                }
                            }
                        })
                        .flatMapCompletable(entryDao::insertMany)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            onSuccess.run();
                            WidgetService.notifyDataChanged(context);
                        }, onFailure)
        );
    }

    public void deleteData(long id, Action onSuccess, Consumer<Throwable> onFailure) {
        compositeDisposable.add(
                entryDao.deleteById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            onSuccess.run();
                            WidgetService.notifyDataChanged(context);
                        }, onFailure)
        );
    }

    public void getById(long id, Consumer<Entry> onSuccess, Consumer<Throwable> onFailure) {
        compositeDisposable.add(
                entryDao.getById(id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(onSuccess, onFailure)
        );
    }

    public void updateData(Entry entry, Action onSuccess, Consumer<Throwable> onFailure) {
        compositeDisposable.add(
                entryDao.update(entry)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            onSuccess.run();
                            WidgetService.notifyDataChanged(context);
                        }, onFailure)
        );
    }

    public List<Entry> getDataSynchronously() {
        return entryDao.getAllSynchronously();
    }

    public void getData(Consumer<List<Entry>> onSuccess, Consumer<Throwable> onFailure) {
        compositeDisposable.add(
                entryDao.getAllRx()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(onSuccess, onFailure)
        );
    }

}

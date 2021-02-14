package com.woodplantation.geburtstagsverwaltung.repository;

import android.content.Context;
import android.os.Environment;

import androidx.lifecycle.LiveData;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.woodplantation.geburtstagsverwaltung.activities.MainActivity;
import com.woodplantation.geburtstagsverwaltung.database.EntryDao;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoDataToExportException;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoStorageAvailableException;
import com.woodplantation.geburtstagsverwaltung.exceptions.UnableToCreateDirectoryException;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.widget.WidgetService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
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

    public void exportData(Consumer<String> onSuccess, Consumer<Throwable> onFailure) {
        compositeDisposable.add(
                entryDao.getAllRx()
                        .flatMap(data -> Single.fromCallable(() -> {
                            if (data.isEmpty()) {
                                throw new NoDataToExportException();
                            }
                            String state = Environment.getExternalStorageState();
                            if (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                                File dir = new File(Environment.getExternalStorageDirectory(), MainActivity.FILE_EXPORT_DIRECTORY);
                                if (!dir.exists() && !dir.mkdir()) {
                                    throw new UnableToCreateDirectoryException();
                                }
                                String filename = MainActivity.FILE_EXPORT_NAME
                                        + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Instant.now())
                                        + MainActivity.FILE_EXPORT_EXTENSION;
                                File output = new File(dir + File.separator + filename);
                                objectMapper.writeValue(output, data);
                                return output.getAbsolutePath();
                            } else {
                                throw new NoStorageAvailableException();
                            }
                        }))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(onSuccess, onFailure)
        );
    }

    public void importData(String path, Action onSuccess, Consumer<Throwable> onFailure) {
        compositeDisposable.add(
                Single
                        .fromCallable(() -> {
                            File file = new File(path);
                            // VARIANT 1
                            // try to read as new entry set
                            try {
                                return objectMapper.readValue(file, new TypeReference<Set<Entry>>() {});
                            } catch (JsonParseException | JsonMappingException e1) {
                                // VARIANT 2
                                // try to read as old set of DataSet and map it to entry
                                try {
                                    return objectMapper.readValue(file, new TypeReference<Set<DataSet>>() {
                                    })
                                            .stream()
                                            .map(Entry::new)
                                            .collect(Collectors.toSet());
                                } catch (JsonParseException | JsonMappingException e2) {
                                    // VARIANT 3
                                    // try to read with old method via serializable interface
                                    FileInputStream fis = new FileInputStream(file);
                                    BufferedInputStream bis = new BufferedInputStream(fis);
                                    ObjectInputStream ois = new ObjectInputStream(bis);

                                    ArrayList<DataSet> data = (ArrayList<DataSet>) ois.readObject();

                                    ois.close();
                                    bis.close();
                                    fis.close();
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

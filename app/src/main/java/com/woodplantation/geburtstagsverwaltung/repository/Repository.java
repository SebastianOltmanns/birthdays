package com.woodplantation.geburtstagsverwaltung.repository;

import android.os.Environment;

import androidx.lifecycle.LiveData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woodplantation.geburtstagsverwaltung.activities.MainActivity;
import com.woodplantation.geburtstagsverwaltung.database.EntryDao;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoDataToExportException;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoStorageAvailableException;
import com.woodplantation.geburtstagsverwaltung.exceptions.UnableToCreateDirectoryException;
import com.woodplantation.geburtstagsverwaltung.model.Entry;

import java.io.File;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.woodplantation.geburtstagsverwaltung.activities.MainActivity.FILE_EXPORT_EXTENSION;

public class Repository {

    private final EntryDao entryDao;
    private final ObjectMapper objectMapper;
    //TODO cancel compositedisposable when application closes
    public final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public Repository(EntryDao entryDao, ObjectMapper objectMapper) {
        this.entryDao = entryDao;
        this.objectMapper = objectMapper;
    }

    public LiveData<List<Entry>> getData() {
        return entryDao.getAll();
    }

    public void insertData(Set<Entry> data, Action onComplete) {
        compositeDisposable.add(
                entryDao.insertMany(data)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(onComplete)
        );
    }

    public void exportData(Consumer<String> onSuccess, Consumer<Throwable> onFailure) {
        compositeDisposable.add(
                entryDao.getAllRx()
                        .flatMap(data -> Single.fromCallable(() -> {
                            if (data == null || data.isEmpty()) {
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

}

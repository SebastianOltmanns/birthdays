package com.woodplantation.geburtstagsverwaltung.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.woodplantation.geburtstagsverwaltung.model.Entry;

import java.util.List;
import java.util.Set;

import io.reactivex.Completable;
import io.reactivex.Single;

@Dao
public interface EntryDao {

    @Insert
    Completable insert(Entry entry);

    @Insert
    Completable insertMany(Set<Entry> entries);

    @Query("DELETE FROM Entry WHERE id = :id")
    Completable deleteById(long id);

    @Update
    Completable update(Entry entry);

    @Query("SELECT * FROM Entry")
    LiveData<List<Entry>> getAll();

    @Query("SELECT * FROM Entry")
    Single<List<Entry>> getAllRx();

    @Query("SELECT * FROM Entry WHERE id = :id")
    Single<Entry> getById(long id);

    @Query("SELECT * FROM Entry")
    List<Entry> getAllSynchronously();

}

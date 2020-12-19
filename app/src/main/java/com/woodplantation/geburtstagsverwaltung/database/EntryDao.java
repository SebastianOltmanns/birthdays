package com.woodplantation.geburtstagsverwaltung.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.woodplantation.geburtstagsverwaltung.model.Entry;

import java.util.List;
import java.util.Set;

import io.reactivex.Completable;

@Dao
public interface EntryDao {

    @Insert
    Completable insert(Entry entry);

    @Insert
    Completable insertMany(Set<Entry> entries);

    @Delete
    Completable delete(Entry entry);

    @Update
    Completable update(Entry entry);

    @Query("SELECT * FROM Entry")
    LiveData<List<Entry>> getAll();

}

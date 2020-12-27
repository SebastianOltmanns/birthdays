package com.woodplantation.geburtstagsverwaltung.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.woodplantation.geburtstagsverwaltung.storage.DataSet;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Calendar;

@Entity
public class Entry {

    @PrimaryKey
    public long id;

    @NotNull public String firstName = "";
    @NotNull public String lastName = "";

    @NotNull public LocalDate birthday = LocalDate.now();
    public boolean ignoreYear = false;

    @NotNull public String notes = "";

    @NotNull
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }

    public Entry() {
    }

    public Entry(DataSet dataSet) {
        firstName = dataSet.firstName;
        lastName = dataSet.lastName;
        ignoreYear = false;
        birthday = LocalDate.of(dataSet.birthday.get(Calendar.YEAR), dataSet.birthday.get(Calendar.MONTH), dataSet.birthday.get(Calendar.DAY_OF_MONTH));
        notes = dataSet.others;
    }
}

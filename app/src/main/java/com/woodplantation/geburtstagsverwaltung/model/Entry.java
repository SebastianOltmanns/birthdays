package com.woodplantation.geburtstagsverwaltung.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

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
}

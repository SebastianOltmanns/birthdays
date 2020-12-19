package com.woodplantation.geburtstagsverwaltung.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity
public class Entry {

    @PrimaryKey
    public Long id;

    public String firstName;
    public String lastName;

    public LocalDate birthday;
    public boolean ignoreYear;

    public String notes;

}

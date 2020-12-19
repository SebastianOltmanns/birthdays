package com.woodplantation.geburtstagsverwaltung.repository;

import com.woodplantation.geburtstagsverwaltung.database.EntryDao;

import javax.inject.Inject;

public class Repository {

    private final EntryDao entryDao;

    @Inject
    public Repository(EntryDao entryDao) {
        this.entryDao = entryDao;
    }
}

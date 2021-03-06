package com.woodplantation.geburtstagsverwaltung.comparators;

import com.woodplantation.geburtstagsverwaltung.model.Entry;

import java.util.Comparator;

/**
 * this comparator compares two entries by comparing their full names
 */
public class LexicographicFullNameComparator implements Comparator<Entry> {
    @Override
    public int compare(Entry o1, Entry o2) {
        return o1.getFullName().compareTo(o2.getFullName());
    }
}

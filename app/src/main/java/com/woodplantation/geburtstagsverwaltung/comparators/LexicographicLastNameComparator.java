package com.woodplantation.geburtstagsverwaltung.comparators;

import com.woodplantation.geburtstagsverwaltung.model.Entry;

import java.util.Comparator;

public class LexicographicLastNameComparator implements Comparator<Entry> {
    @Override
    public int compare(Entry o1, Entry o2) {
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        return o1.lastName.compareTo(o2.lastName);
    }
}

package com.woodplantation.geburtstagsverwaltung.comparators;

import com.woodplantation.geburtstagsverwaltung.model.Entry;

import java.util.Comparator;

/**
 * this comparator compares two entries and values the one with the smaller day in the year
 * as smaller.
 */
public class CalendricComparator implements Comparator<Entry> {
    @Override
    public int compare(Entry o1, Entry o2) {
        return Integer.compare(o1.birthday.getDayOfYear(), o2.birthday.getDayOfYear());
    }
}

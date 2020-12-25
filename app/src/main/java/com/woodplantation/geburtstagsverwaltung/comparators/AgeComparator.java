package com.woodplantation.geburtstagsverwaltung.comparators;

import com.woodplantation.geburtstagsverwaltung.model.Entry;

import java.util.Comparator;

/**
 * this comparator compares two entries by comparing the age: it compares thebirthdays
 * and reverts the compare value, such that the smaller (older) date is valued as bigger
 */
public class AgeComparator implements Comparator<Entry> {
    @Override
    public int compare(Entry o1, Entry o2) {
        return -(o1.birthday.compareTo(o2.birthday));
    }
}

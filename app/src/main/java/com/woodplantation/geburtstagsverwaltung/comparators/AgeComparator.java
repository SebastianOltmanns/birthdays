package com.woodplantation.geburtstagsverwaltung.comparators;

import com.woodplantation.geburtstagsverwaltung.model.Entry;

import java.time.LocalDate;
import java.util.Comparator;

/**
 * this comparator compares two entries using the age of the birthday. <br/>
 * if one of the entries is set to ignore the year, the one without the year
 * ignored is considered smaller. if both are set to ignore the year, they are
 * equal. otherwise, the dates are compared s.t. the older date is bigger.
 */
public class AgeComparator implements Comparator<Entry> {
    @Override
    public int compare(Entry o1, Entry o2) {
        if (o1.ignoreYear) {
            if (o2.ignoreYear) {
                return 0;
            } else {
                return 1;
            }
        }
        if (o2.ignoreYear) {
            return -1;
        }
        return -(o1.birthday.compareTo(o2.birthday));
    }
}

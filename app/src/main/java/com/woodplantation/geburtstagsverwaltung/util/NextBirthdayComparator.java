package com.woodplantation.geburtstagsverwaltung.util;

import com.woodplantation.geburtstagsverwaltung.model.Entry;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

/**
 * this comparator compares two entries and sets the entry that has its birthday
 * coming up closer as smaller. <br>
 * e.g. when birthday of a is in 3 days, and birthday of b is in 5 days: o1 < o2.
 */
public class NextBirthdayComparator implements Comparator<Entry> {
    @Override
    public int compare(Entry a, Entry b) {
        LocalDate _a = getNextBirthday(a.birthday);
        LocalDate _b = getNextBirthday(b.birthday);
        return _a.compareTo(_b);
    }

    public static LocalDate getNextBirthday(LocalDate date) {
        LocalDate now = LocalDate.now();
        LocalDate _date = date.with(ChronoField.YEAR, now.getYear());
        if (_date.isBefore(now)) {
            _date = _date.plus(1, ChronoUnit.YEARS);
        }
        return _date;
    }
}

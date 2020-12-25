package com.woodplantation.geburtstagsverwaltung.comparators;

import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.util.DateUtil;

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
        return Long.compare(
                DateUtil.getRemainingDaysUntilNextBirthday(DateUtil.getNextBirthday(a.birthday)),
                DateUtil.getRemainingDaysUntilNextBirthday(DateUtil.getNextBirthday(b.birthday))
        );
    }
}

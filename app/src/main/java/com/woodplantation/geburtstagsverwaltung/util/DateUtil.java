package com.woodplantation.geburtstagsverwaltung.util;

import android.content.Context;

import com.woodplantation.geburtstagsverwaltung.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class DateUtil {

    public static long getRemainingDaysUntilNextBirthday(LocalDate nextBirthday) {
        return nextBirthday.toEpochDay() - LocalDate.now().toEpochDay();
    }

    public static LocalDate getNextBirthday(LocalDate date) {
        LocalDate now = LocalDate.now();
        LocalDate _date = date.with(ChronoField.YEAR, now.getYear());
        if (_date.isBefore(now)) {
            _date = _date.plus(1, ChronoUnit.YEARS);
        }
        return _date;
    }

    public static String getRemainingWithAge(Context context, LocalDate birthday, boolean withAge) {
        LocalDate nextBirthday = getNextBirthday(birthday);
        long daysRemaining = getRemainingDaysUntilNextBirthday(nextBirthday);

        String textRemaining;

        if (daysRemaining == 0) {
            textRemaining = context.getString(R.string.today);
        } else if (daysRemaining == 1) {
            textRemaining = context.getString(R.string.tomorrow);
        } else {
            textRemaining = context.getString(R.string.in_x_days, daysRemaining);
        }

        int age = nextBirthday.getYear() - birthday.getYear();

        if (withAge) {
            return context.getString(R.string.xth_birthday, textRemaining, age);
        } else {
            return textRemaining;
        }
    }

    private final static DateTimeFormatter formatterWithYear = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.US);
    private final static DateTimeFormatter formatterWithoutYear = DateTimeFormatter.ofPattern("dd.MM", Locale.US);

    public static String getBirthdayString(LocalDate birthday, boolean ignoreYear) {
        if (ignoreYear) {
            return formatterWithoutYear.format(birthday);
        } else {
            return formatterWithYear.format(birthday);
        }
    }

}

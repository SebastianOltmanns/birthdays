package com.woodplantation.geburtstagsverwaltung.database;

import androidx.room.TypeConverter;

import java.time.LocalDate;

public class Converters {

    @TypeConverter
    public static Long dateToLong(LocalDate date) {
        return date == null ? null : date.toEpochDay();
    }

    @TypeConverter
    public static LocalDate longToDate(Long epochDay) {
        return epochDay == null ? null : LocalDate.ofEpochDay(epochDay);
    }

}

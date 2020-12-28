package com.woodplantation.geburtstagsverwaltung.viewmodel;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class InputViewModel extends ViewModel {

    private final Repository repository;

    @ViewModelInject
    public InputViewModel(Repository repository) {
        this.repository = repository;
    }

    private final MutableLiveData<String> firstName = new MutableLiveData<>("");
    private final MutableLiveData<String> lastName = new MutableLiveData<>("");
    private final MutableLiveData<LocalDate> birthday = new MutableLiveData<>(LocalDate.now());
    private final MutableLiveData<Boolean> ignoreYear = new MutableLiveData<>(false);
    private final MutableLiveData<String> notes = new MutableLiveData<>("");

    public LiveData<String> getFirstName() {
        return firstName;
    }

    public LiveData<String> getLastName() {
        return lastName;
    }

    public LiveData<LocalDate> getBirthday() {
        return birthday;
    }

    public LiveData<Boolean> getIgnoreYear() {
        return ignoreYear;
    }

    public LiveData<String> getNotes() {
        return notes;
    }

    public void setFirstName(String firstName) {
        this.firstName.setValue(firstName);
    }

    public void setLastName(String lastName) {
        this.lastName.setValue(lastName);
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday.setValue(birthday);
    }

    public void setBirthdayDay(int day) {
        //noinspection ConstantConditions
        this.birthday.setValue(this.birthday.getValue().withDayOfMonth(day));
    }

    public void setBirthdayMonth(int month) {
        //noinspection ConstantConditions
        this.birthday.setValue(this.birthday.getValue().withMonth(month));
    }

    public void setBirthdayYear(int year) {
        //noinspection ConstantConditions
        this.birthday.setValue(this.birthday.getValue().withYear(year));
    }

    public void setIgnoreYear(boolean ignoreYear) {
        this.ignoreYear.setValue(ignoreYear);
    }

    public void setNotes(String notes) {
        this.notes.setValue(notes);
    }

    public void save(Action onSuccess, Consumer<Throwable> onFailure) {
        //noinspection ConstantConditions
        Entry entry = new Entry(firstName.getValue(), lastName.getValue(), birthday.getValue(), ignoreYear.getValue(), notes.getValue());
        Set<Entry> data = new HashSet<>();
        data.add(entry);
        repository.insertData(data, onSuccess, onFailure);
    }
}

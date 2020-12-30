package com.woodplantation.geburtstagsverwaltung.viewmodel;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.woodplantation.geburtstagsverwaltung.exceptions.NoIdToDeleteException;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;

import java.time.LocalDate;

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
    private Long id;

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
        if (hasId()) {
            entry.id = id;
            repository.updateData(entry, onSuccess, onFailure);
        } else {
            repository.insertData(entry, onSuccess, onFailure);
        }
    }

    public void delete(Action onSuccess, Consumer<Throwable> onFailure) {
        if (hasId()) {
            repository.deleteData(id, onSuccess, onFailure);
        } else {
            try {
                onFailure.accept(new NoIdToDeleteException());
            } catch (Exception e) {
                // this should NEVER happen.
                e.printStackTrace();
            }
        }
    }

    public void init(Long id, Consumer<Throwable> onFailure) {
        this.id = id;
        if (hasId()) {
            repository.getById(
                    id,
                    entry -> {
                        firstName.setValue(entry.firstName);
                        lastName.setValue(entry.lastName);
                        birthday.setValue(entry.birthday);
                        ignoreYear.setValue(entry.ignoreYear);
                        notes.setValue(entry.notes);
                    },
                    onFailure
            );
        } else {
            firstName.setValue("");
            lastName.setValue("");
            birthday.setValue(LocalDate.now());
            ignoreYear.setValue(false);
            notes.setValue("");
        }
    }

    public boolean hasId() {
        return id != null && id != -1;
    }
}

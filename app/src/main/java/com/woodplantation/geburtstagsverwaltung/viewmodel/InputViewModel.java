package com.woodplantation.geburtstagsverwaltung.viewmodel;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.woodplantation.geburtstagsverwaltung.exceptions.InvalidDateException;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoFirstNameSpecifiedException;
import com.woodplantation.geburtstagsverwaltung.exceptions.NoIdToDeleteException;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.function.Consumer;

import io.reactivex.functions.Action;

public class InputViewModel extends ViewModel {

    private final Repository repository;

    @ViewModelInject
    public InputViewModel(Repository repository) {
        this.repository = repository;
        LocalDate now = LocalDate.now();
        birthdayDay.setValue(String.valueOf(now.getDayOfMonth()));
        birthdayMonth.setValue(String.valueOf(now.getMonthValue()));
        birthdayYear.setValue(String.valueOf(now.getYear()));
    }

    private final MutableLiveData<String> firstName = new MutableLiveData<>("");
    private final MutableLiveData<String> lastName = new MutableLiveData<>("");
    private final MutableLiveData<String> birthdayDay = new MutableLiveData<>();
    private final MutableLiveData<String> birthdayMonth = new MutableLiveData<>();
    private final MutableLiveData<String> birthdayYear = new MutableLiveData<>();
    private final MutableLiveData<Boolean> ignoreYear = new MutableLiveData<>(false);
    private final MutableLiveData<String> notes = new MutableLiveData<>("");
    private Long id;

    public LiveData<String> getFirstName() {
        return firstName;
    }

    public LiveData<String> getLastName() {
        return lastName;
    }

    public LiveData<String> getBirthdayDay() {
        return birthdayDay;
    }

    public LiveData<String> getBirthdayMonth() {
        return birthdayMonth;
    }

    public LiveData<String> getBirthdayYear() {
        return birthdayYear;
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

    public void setBirthdayDay(String birthdayDay) {
        this.birthdayDay.setValue(birthdayDay);
    }

    public void setBirthdayMonth(String birthdayMonth) {
        this.birthdayMonth.setValue(birthdayMonth);
    }

    public void setBirthdayYear(String birthdayYear) {
        this.birthdayYear.setValue(birthdayYear);
    }

    public void setIgnoreYear(boolean ignoreYear) {
        this.ignoreYear.setValue(ignoreYear);
    }

    public void setNotes(String notes) {
        this.notes.setValue(notes);
    }

    public void save(Action onSuccess, Consumer<Throwable> onFailure) {
        if (firstName.getValue() == null || firstName.getValue().equals("")) {
            try {
                onFailure.accept(new NoFirstNameSpecifiedException());
            } catch (Exception e) {
                // this should NEVER happen
                e.printStackTrace();
            }
            return;
        }
        try {
            //noinspection ConstantConditions
            LocalDate birthday = LocalDate.of(
                    Integer.parseInt(birthdayYear.getValue()),
                    Integer.parseInt(birthdayMonth.getValue()),
                    Integer.parseInt(birthdayDay.getValue()));

            //noinspection ConstantConditions
            Entry entry = new Entry(firstName.getValue(), lastName.getValue(), birthday, ignoreYear.getValue(), notes.getValue());
            if (hasId()) {
                entry.id = id;
                repository.updateData(entry, onSuccess, onFailure::accept);
            } else {
                repository.insertData(entry, onSuccess, onFailure::accept);
            }
        } catch (DateTimeException e) {
            try {
                onFailure.accept(new InvalidDateException());
            } catch (Exception e2) {
                // this should NEVER happen
                e.printStackTrace();
            }
        }
    }

    public void delete(Action onSuccess, Consumer<Throwable> onFailure) {
        if (hasId()) {
            repository.deleteData(id, onSuccess, onFailure::accept);
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
                        birthdayDay.setValue(String.valueOf(entry.birthday.getDayOfMonth()));
                        birthdayMonth.setValue(String.valueOf(entry.birthday.getMonthValue()));
                        birthdayYear.setValue(String.valueOf(entry.birthday.getYear()));
                        ignoreYear.setValue(entry.ignoreYear);
                        notes.setValue(entry.notes);
                    },
                    onFailure::accept
            );
        } else {
            firstName.setValue("");
            lastName.setValue("");
            LocalDate now = LocalDate.now();
            birthdayYear.setValue(String.valueOf(now.getYear()));
            birthdayMonth.setValue(String.valueOf(now.getMonthValue()));
            birthdayDay.setValue(String.valueOf(now.getDayOfMonth()));
            ignoreYear.setValue(false);
            notes.setValue("");
        }
    }

    public boolean hasId() {
        return id != null && id != -1;
    }
}

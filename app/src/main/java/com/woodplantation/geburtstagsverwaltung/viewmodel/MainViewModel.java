package com.woodplantation.geburtstagsverwaltung.viewmodel;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;
import com.woodplantation.geburtstagsverwaltung.util.NextBirthdayComparator;
import com.woodplantation.geburtstagsverwaltung.util.SortingCategory;
import com.woodplantation.geburtstagsverwaltung.util.SortingOrder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainViewModel extends ViewModel {

    private final Repository repository;

    @ViewModelInject
    public MainViewModel(Repository repository) {
        this.repository = repository;

        rawData = repository.getData();
        data.addSource(sortingCategory, __ -> sort());
        data.addSource(sortingOrder, __ -> reverse());
        data.addSource(rawData, __ -> sort());
    }

    private final MutableLiveData<SortingCategory> sortingCategory = new MutableLiveData<>(SortingCategory.NEXT_BIRTHDAY);
    private final MutableLiveData<SortingOrder> sortingOrder = new MutableLiveData<>(SortingOrder.ASC);
    private final MediatorLiveData<List<Entry>> data = new MediatorLiveData<>();
    private final LiveData<List<Entry>> rawData;

    public LiveData<List<Entry>> getData() {
        return data;
    }

    private void sort() {
        List<Entry> _data = rawData.getValue();
        if (_data != null) {
            Comparator<Entry> comparator;
            SortingCategory _sortingCategory = sortingCategory.getValue();
            if (_sortingCategory == SortingCategory.NEXT_BIRTHDAY) {
                comparator = new NextBirthdayComparator();
            }
            Collections.sort(_data, comparator);
            data.setValue(_data);
        }
    }

    private void reverse() {
        List<Entry> _data = data.getValue();
        if (_data != null) {
            Collections.reverse(_data);
            data.setValue(_data);
        }
    }
}

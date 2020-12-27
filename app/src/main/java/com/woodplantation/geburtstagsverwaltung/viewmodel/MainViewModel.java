package com.woodplantation.geburtstagsverwaltung.viewmodel;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.woodplantation.geburtstagsverwaltung.comparators.AgeComparator;
import com.woodplantation.geburtstagsverwaltung.comparators.LexicographicComparator;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;
import com.woodplantation.geburtstagsverwaltung.comparators.CalendricComparator;
import com.woodplantation.geburtstagsverwaltung.comparators.NextBirthdayComparator;
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
            switch (_sortingCategory) {
                case NEXT_BIRTHDAY: {
                    comparator = new NextBirthdayComparator();
                    break;
                }
                case CALENDRIC: {
                    comparator = new CalendricComparator();
                    break;
                }
                case LEXICOGRAPHIC: {
                    comparator = new LexicographicComparator();
                    break;
                }
                case AGE: {
                    comparator = new AgeComparator();
                    break;
                }
                default: {
                    comparator = null;
                }
            }
            if (comparator != null) {
                Collections.sort(_data, comparator);
                data.setValue(_data);
            }
        }
    }

    private void reverse() {
        List<Entry> _data = data.getValue();
        if (_data != null) {
            Collections.reverse(_data);
            data.setValue(_data);
        }
    }

    /**
     * if the category changed, the new category will be chosen. if the category stayed the
     * same, the order will be changed.
     */
    public void sortingCategoryClicked(SortingCategory newSortingCategory) {
        SortingCategory oldSortingCategory = sortingCategory.getValue();
        if (oldSortingCategory == newSortingCategory) {
            sortingOrder.setValue(sortingOrder.getValue() == SortingOrder.ASC ? SortingOrder.DESC : SortingOrder.ASC);
        } else {
            sortingCategory.setValue(newSortingCategory);
        }
    }

    public LiveData<SortingCategory> getSortingCategory() {
        return sortingCategory;
    }

    public LiveData<SortingOrder> getSortingOrder() {
        return sortingOrder;
    }

}

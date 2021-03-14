package com.woodplantation.geburtstagsverwaltung.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

/**
 * text watcher that automatically requests focus for the next
 * textview once the given text length is reached
 */
public class FocusNextViewTextWatcher implements TextWatcher {

    private final int count;
    private final View view;

    public FocusNextViewTextWatcher(int count, View view) {
        this.count = count;
        this.view = view;
    }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.length() == count) view.requestFocus();
    }
}
package com.woodplantation.geburtstagsverwaltung.view;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.function.Consumer;

/**
 * text watcher that accepts one {@link Consumer} in the constructor. this consumer will
 * be called in {@link TextWatcher#afterTextChanged(Editable)}.
 */
public class AfterTextChanged implements TextWatcher {

    private final Consumer<Editable> afterTextChanged;

    public AfterTextChanged(Consumer<Editable> afterTextChanged) {
        this.afterTextChanged = afterTextChanged;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        afterTextChanged.accept(s);
    }
}

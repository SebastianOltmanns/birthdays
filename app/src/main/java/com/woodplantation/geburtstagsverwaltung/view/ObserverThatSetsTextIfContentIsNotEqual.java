package com.woodplantation.geburtstagsverwaltung.view;

import android.widget.TextView;

import androidx.lifecycle.Observer;

import java.util.function.Function;

public class ObserverThatSetsTextIfContentIsNotEqual<T> implements Observer<T> {

    private final TextView textView;
    private final Function<T, String> toString;

    private ObserverThatSetsTextIfContentIsNotEqual(TextView textView, Function<T, String> toString) {
        this.textView = textView;
        this.toString = toString;
    }

    private static final Function<String, String> stringIdentity = s -> s;

    public static ObserverThatSetsTextIfContentIsNotEqual<String> forString(TextView textView) {
        return new ObserverThatSetsTextIfContentIsNotEqual<>(textView, stringIdentity);
    }

    public static ObserverThatSetsTextIfContentIsNotEqual<Integer> forInteger(TextView textView) {
        return new ObserverThatSetsTextIfContentIsNotEqual<>(textView, String::valueOf);
    }

    @Override
    public void onChanged(T value) {
        String strValue = toString.apply(value);
        if (!strValue.contentEquals(textView.getText() == null ? "" : textView.getText())) {
            textView.setText(strValue);
        }
    }
}

package com.woodplantation.geburtstagsverwaltung.view;

import android.app.Activity;
import android.util.TypedValue;

import androidx.annotation.ColorInt;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;

import javax.inject.Inject;

public class AppTheme {

    private final MyPreferences myPreferences;

    @Inject
    public AppTheme(MyPreferences myPreferences) {
        this.myPreferences = myPreferences;
    }

    public enum Theme {
        SAN_MARINO_WILD_STRAWBERRY(R.style.SanMarinoWildStrawberry, "San Marino / Wild Strawberry"),
        SILVER_TREE_TAPESTRY(R.style.SilverTreeTapestry, "Silver Tree / Tapestry"),
        LIMED_OAK_WEDGEWOOD(R.style.LimedOakWedgewood, "Limed Oak / Wedgewood");

        public final int themeResource;
        public final String name;

        Theme(int themeResource, String name) {
            this.themeResource = themeResource;
            this.name = name;
        }
    }

    public void applyAppTheme(Activity activity) {
        Theme theme = myPreferences.getAppTheme();
        activity.getTheme().applyStyle(theme.themeResource, true);
    }

    public static void applyAppTheme(Activity activity, AppTheme.Theme theme) {
        activity.getTheme().applyStyle(theme.themeResource, true);
    }

    @ColorInt
    public int getColorAccent(Activity activity) {
        final TypedValue value = new TypedValue();
        activity.getTheme().resolveAttribute(R.attr.colorAccent, value, true);
        return value.data;
    }

}

package com.woodplantation.geburtstagsverwaltung.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.util.IntentCodes;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;
import com.woodplantation.geburtstagsverwaltung.view.AppTheme;
import com.woodplantation.geburtstagsverwaltung.viewmodel.SettingsViewModel;

import java.util.HashMap;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsActivity extends AppCompatActivity {

    @Inject
    MyPreferences preferences;
    @Inject
    AppTheme appTheme;

    SettingsViewModel settingsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        if (savedInstanceState == null) {
            /*
            if bundle is null: apply default app theme, as stored in preferences
             */
            appTheme.applyAppTheme(this);
        } else {
            /*
            if bundle is not null: try to apply theme from viewmodel
             */
            AppTheme.Theme theme = settingsViewModel.theme.getValue();
            if (theme != null) {
                AppTheme.applyAppTheme(this, theme);
            } else {
                appTheme.applyAppTheme(this);
            }
        }

        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /*
        set display fab switch
         */
        Boolean displayFab = settingsViewModel.displayFab.getValue();
        if (displayFab == null) {
            displayFab = preferences.getDisplayFAB();
        }
        SwitchCompat switchDisplayFab = findViewById(R.id.activity_settings_display_fab);
        switchDisplayFab.setOnCheckedChangeListener((buttonView, isChecked) -> settingsViewModel.displayFab.setValue(isChecked));
        switchDisplayFab.setChecked(displayFab);

        /*
        create themes list.
         */
        RadioGroup themesList = findViewById(R.id.themes_list);
        AppTheme.Theme currentTheme = settingsViewModel.theme.getValue();
        if (currentTheme == null) {
            currentTheme = preferences.getAppTheme();
        }
        final HashMap<Integer, AppTheme.Theme> mapping = new HashMap<>();
        for (AppTheme.Theme theme : AppTheme.Theme.values()) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());
            mapping.put(radioButton.getId(), theme);
            if (theme == currentTheme) {
                radioButton.setChecked(true);
                settingsViewModel.theme.setValue(theme);
            } else {
                radioButton.setChecked(false);
            }
            radioButton.setText(theme.name);
            themesList.addView(radioButton);
        }
        themesList.setOnCheckedChangeListener((group, checkedId) -> {
            AppTheme.Theme theme = mapping.get(checkedId);
            if (theme != null) {
                settingsViewModel.theme.setValue(theme);
                recreate();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_cancel || item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        } else if (item.getItemId() == R.id.menu_ok) {
            Boolean displayFab = settingsViewModel.displayFab.getValue();
            if (displayFab == null) {
                displayFab = getResources().getBoolean(R.bool.preferences_display_fab);
            }
            AppTheme.Theme newTheme = settingsViewModel.theme.getValue();
            int newThemeOrdinal;
            if (newTheme == null) {
                newThemeOrdinal = getResources().getInteger(R.integer.preferences_theme);
            } else {
                newThemeOrdinal = newTheme.ordinal();
            }
            boolean themeChanged = newThemeOrdinal != preferences.getAppTheme().ordinal();
            preferences.preferences
                    .edit()
                    .putBoolean(getString(R.string.preferences_display_fab), displayFab)
                    .putInt(getString(R.string.preferences_theme), newThemeOrdinal)
                    .apply();
            Intent intent = new Intent();
            intent.putExtra(IntentCodes.THEME_CHANGED, themeChanged);
            setResult(Activity.RESULT_OK, intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

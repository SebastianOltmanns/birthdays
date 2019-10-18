package com.woodplantation.geburtstagsverwaltung.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.notifications.MyPreferences;

public class SettingsActivity extends AppCompatActivity {

    private MyPreferences preferences;

    private Switch switchDisplayFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        switchDisplayFAB = findViewById(R.id.activity_settings_display_fab);

        preferences = new MyPreferences(this, MyPreferences.FILEPATH_SETTINGS);

        switchDisplayFAB.setChecked(preferences.getDisplayFAB());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cancel_and_save, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_cancel:
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_ok:
                SharedPreferences.Editor editor = preferences.preferences.edit();
                editor.putBoolean(getString(R.string.preferences_display_fab), switchDisplayFAB.isChecked());
                editor.apply();
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

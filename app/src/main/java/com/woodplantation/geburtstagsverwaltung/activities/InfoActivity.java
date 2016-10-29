package com.woodplantation.geburtstagsverwaltung.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.woodplantation.geburtstagsverwaltung.R;

/**
 * Created by Sebu on 22.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class InfoActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

}

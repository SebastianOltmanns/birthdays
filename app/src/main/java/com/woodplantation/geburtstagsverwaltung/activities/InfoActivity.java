package com.woodplantation.geburtstagsverwaltung.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.woodplantation.geburtstagsverwaltung.BuildConfig;
import com.woodplantation.geburtstagsverwaltung.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;

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

		TextView versionTextView = (TextView) findViewById(R.id.information_activity_version_text);
		versionTextView.setText(getResources().getString(R.string.info_about_version, BuildConfig.VERSION_NAME));
		TextView motivationTextView = (TextView) findViewById(R.id.information_activity_motivation_text);
		motivationTextView.setText(getString(R.string.info_motivation_text, getResources().getInteger(R.integer.MAXIMUM_DATA_SIZE)));
		TextView infoTextView = (TextView) findViewById(R.id.information_activity_about_text);
		infoTextView.setMovementMethod(LinkMovementMethod.getInstance());
		TextView githubTextView = (TextView) findViewById(R.id.information_activity_github_text);
		githubTextView.setMovementMethod(LinkMovementMethod.getInstance());

		readPrivacyPolicy();
	}

	private void readPrivacyPolicy() {
		String title;
		String text;
		try {
			InputStream is = getResources().openRawResource(R.raw.privacy_policy);
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			title = br.readLine();

			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = br.readLine()) != null) {
				builder.append(line + "\n");
			}
			text = builder.toString();

			is.close();
			isr.close();
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			title = getString(R.string.info_privacy_error_title);
			text = getString(R.string.info_privacy_error_text);
		}

		TextView privacyTitle = (TextView) findViewById(R.id.information_activity_privacy_title);
		TextView privacyText = (TextView) findViewById(R.id.information_activity_privacy_text);

		privacyTitle.setText(title);
		privacyText.setText(text);
	}

}

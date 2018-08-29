package com.woodplantation.geburtstagsverwaltung.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.woodplantation.geburtstagsverwaltung.BuildConfig;
import com.woodplantation.geburtstagsverwaltung.R;

import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Sebu on 22.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class InfoActivity extends AppCompatActivity {

	private AlertDialog aboutDialog, motivationDialog, notificationDialog, developerDialog, githubDialog, privacyDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		TextView versionTextView = findViewById(R.id.information_activity_version_text);
		versionTextView.setText(getResources().getString(R.string.info_about_version, BuildConfig.VERSION_NAME));

		initDialogs();
	}

	private static DialogInterface.OnShowListener makeLinksClickableListener(final AlertDialog dialog, final boolean email) {
		return new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialogInterface) {
				if (email) ((TextView) dialog.findViewById(android.R.id.message)).setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
				((TextView) dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
			}
		};
	}

	private class UlTagHandler implements Html.TagHandler{
		@Override
		public void handleTag(boolean opening, String tag, Editable output,
							  XMLReader xmlReader) {
			if (tag.equals("ul") && !opening) output.append("\n\n");
			if (tag.equals("li") && opening) output.append("\n\n\t•");
		}
	}

	private void initDialogs() {

		aboutDialog =
				new AlertDialog.Builder(this).
						setTitle(R.string.info_about_title).
                        setMessage(R.string.info_about_text).
						setNeutralButton(R.string.ok, null).
						create();
		aboutDialog.setOnShowListener(makeLinksClickableListener(aboutDialog, false));

		motivationDialog =
				new AlertDialog.Builder(this).
						setTitle(R.string.info_motivation_title).
						setMessage(getString(R.string.info_motivation_text, getResources().getInteger(R.integer.MAXIMUM_DATA_SIZE))).
						setNeutralButton(R.string.ok, null).
						create();

		WebView notificationView = new WebView(this);

		String html = getString(R.string.info_notification_text_introduction)
				+ "<ul>"
				+ "<li> " + getString(R.string.info_notification_text_permissions) + "</li>"
				+ "<li> " + getString(R.string.info_notification_text_settings) + "</li>"
				+ "<li> " + getString(R.string.info_notification_text_energy) + "</li>"
				+ "<li> " + getString(R.string.info_notification_text_update) + "</li>"
				+ "<li> " + getString(R.string.info_notification_text_restart) + "</li>"
				+ "</ul>"
				+ getString(R.string.info_notification_text_otherwise);
		notificationView.loadData(html, "text/html; charset=UTF-8", null);
		notificationDialog =
				new AlertDialog.Builder(this).
						setTitle(R.string.info_notification_title).
						setMessage(Html.fromHtml(html, null, new UlTagHandler())).
						setNeutralButton(R.string.ok, null).
						create();

		developerDialog =
				new AlertDialog.Builder(this).
						setTitle(R.string.info_about_developer_title).
						setMessage(R.string.info_about_developer_text).
						setNeutralButton(R.string.ok, null).
						create();
		developerDialog.setOnShowListener(makeLinksClickableListener(developerDialog, true));

		readPrivacyPolicy();
	}

	private void readPrivacyPolicy() {
		try {
			InputStream is = getResources().openRawResource(R.raw.privacy_policy);
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			String text = br.readLine();

			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = br.readLine()) != null) {
				builder.append(line + "\n");
			}
			text += "\n\n" + builder.toString();

			is.close();
			isr.close();
			br.close();

			privacyDialog = new AlertDialog.Builder(this).
					setTitle(R.string.info_privacy_short_title).
					setMessage(text).
					setNeutralButton(R.string.ok, null).
					create();
			privacyDialog.setOnShowListener(makeLinksClickableListener(privacyDialog, true));
		} catch (IOException e) {
			e.printStackTrace();
			privacyDialog = new AlertDialog.Builder(this).
					setTitle(R.string.info_privacy_short_title).
					setMessage(R.string.info_privacy_error_text).
					setNeutralButton(R.string.ok, null).
					create();
			privacyDialog.setOnShowListener(makeLinksClickableListener(privacyDialog, false));
		}
	}

	public void openDialog(View view) {
		switch(view.getId()) {
			case R.id.information_activity_about_button: {
				aboutDialog.show();
				break;
			}
			case R.id.information_activity_motivation_button: {
				motivationDialog.show();
				break;
			}
			case R.id.information_activity_notification_button: {
				notificationDialog.show();
				break;
			}
			case R.id.information_activity_developer_button: {
				developerDialog.show();
				break;
			}
			case R.id.information_activity_privacy_button: {
				privacyDialog.show();
				break;
			}
		}
	}

}

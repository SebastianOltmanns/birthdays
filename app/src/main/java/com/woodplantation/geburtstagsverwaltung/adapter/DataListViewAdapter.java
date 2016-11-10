package com.woodplantation.geburtstagsverwaltung.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.storage.DataSet;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class DataListViewAdapter extends ArrayAdapter<DataSet> {

	private int resource;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

	public DataListViewAdapter(Context context, int resource) {
		super(context, resource);
		this.resource = resource;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().
					getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resource, parent, false);
		}

		DataSet dataSet = getItem(position);

		String remaining = getRemaining(dataSet);
		String firstname = dataSet.firstName;
		String lastname = dataSet.lastName;
		String others = dataSet.others;
		others = others.replaceAll(System.getProperty("line.separator"), " ");
		Calendar birthday = dataSet.birthday;

		TextView nameTextView = (TextView) convertView.findViewById(R.id.data_list_view_text_name);
		TextView othersTextView = (TextView) convertView.findViewById(R.id.data_list_view_text_others);
		TextView birthdayTextView = (TextView) convertView.findViewById(R.id.data_list_view_text_birthday);
		TextView remainingTextView = (TextView) convertView.findViewById(R.id.data_list_view_text_remaining);

		String name = firstname + " " + lastname;
		nameTextView.setText(name);
		othersTextView.setText(others);
		birthdayTextView.setText(sdf.format(birthday.getTime()));
		remainingTextView.setText(remaining);

		return convertView;
	}

	private String getRemaining(DataSet dataSet) {
		Calendar now = Calendar.getInstance();
		int daysRemaining = dataSet.getRemaining(now);

		String textRemaining;

		switch (daysRemaining) {
			case 0:
				textRemaining = getContext().getString(R.string.today);
				break;
			case 1:
				textRemaining = getContext().getString(R.string.tomorrow);
				break;
			default:
				textRemaining = getContext().getString(R.string.in_x_days, daysRemaining);
				break;
		}

		return textRemaining;
	}
}

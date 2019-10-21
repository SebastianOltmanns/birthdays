package com.woodplantation.geburtstagsverwaltung.storage;

import android.content.Context;
import android.text.TextUtils;

import com.woodplantation.geburtstagsverwaltung.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class DataSet implements Serializable {

	static final long serialVersionUID =-2227872867228907805L;

	public int id;
	public Calendar birthday;
	public String firstName;
	public String lastName;
	public String others;

	public DataSet(int id, Calendar birthday, String firstName, String lastName, String others) {
		this.id = id;
		this.birthday = birthday;
		this.firstName = firstName;
		this.lastName = lastName;
		this.others = others;
	}

	public DataSet(JSONObject json) throws JSONException {
		this.id = (int) json.get("id");
		this.birthday = Calendar.getInstance();
		this.birthday.setTimeInMillis((long) json.get("birthday"));
		this.firstName = (String) json.get("firstName");
		this.lastName = (String) json.get("lastName");
		this.others = (String) json.get("others");
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();

		try {
			json.put("id", id);
			json.put("birthday", birthday.getTimeInMillis());
			json.put("firstName", firstName);
			json.put("lastName", lastName);
			json.put("others", others);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	public String getRemainingWithAge(Context context) {
		Calendar now = Calendar.getInstance();
		int daysRemaining = getRemaining(now);

		String textRemaining;

		switch (daysRemaining) {
			case 0:
				textRemaining = context.getString(R.string.today);
				break;
			case 1:
				textRemaining = context.getString(R.string.tomorrow);
				break;
			default:
				textRemaining = context.getString(R.string.in_x_days, daysRemaining);
				break;
		}

		int age = getNextAge(now);

		return context.getString(R.string.xth_birthday, textRemaining, age);
	}

	private int getRemaining(Calendar now) {
		//make copy from birthday since we dont want to change original
		Calendar tempBirthday = Calendar.getInstance();
		tempBirthday.setTimeInMillis(birthday.getTimeInMillis());
		//set birthday year to this year
		tempBirthday.set(Calendar.YEAR, now.get(Calendar.YEAR));

		if (tempBirthday.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
			//today
			return 0;
		} else {
			//get amount of days reamining to birthday
			int daysRemaining = tempBirthday.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
			//if we have negative amount, birthday was already this year.
			if (daysRemaining < 0) {
				//set bday to next year
				tempBirthday.set(Calendar.YEAR, now.get(Calendar.YEAR)+1);
				//get remaining days
				daysRemaining = tempBirthday.get(Calendar.DAY_OF_YEAR) + now.getActualMaximum(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
			}
			return daysRemaining;
		}
	}

	public int getNextAge(Calendar now) {
		//make copy from birthday since we dont want to change original
		Calendar tempBirthday = Calendar.getInstance();
		tempBirthday.setTimeInMillis(birthday.getTimeInMillis());
		//set birthday year to this year
		tempBirthday.set(Calendar.YEAR, now.get(Calendar.YEAR));

		//calculate the next age if birthday was this year already
		int nextAge = now.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);

		//if birthday was already this year, need to add one year for next age
		return (now.get(Calendar.DAY_OF_YEAR) <= tempBirthday.get(Calendar.DAY_OF_YEAR)) ? nextAge : nextAge + 1;
	}

	/**
	 * compares two datasets. the dataset with the next birthday to come will be returned as smaller.
	 */
	public static class NextBirthdayComparator implements Comparator<DataSet> {
		@Override
		public int compare(DataSet t0, DataSet t1) {
			Calendar now = Calendar.getInstance();
			int t0Remains = t0.getRemaining(now);
			int t1Remains = t1.getRemaining(now);

			if (t0Remains > t1Remains) return 1;
			if (t0Remains < t1Remains) return -1;

			int t0Year = t0.birthday.get(Calendar.YEAR);
			int t1Year = t1.birthday.get(Calendar.YEAR);

			if (t0Year > t1Year) return 1;
			if (t0Year < t1Year) return -1;

			return 0;
		}
	}

	/**
	 * compares two datasets. the dataset with the birthday earlier in year will be returned as smaller.
	 */
	public static class CalendarComparator implements Comparator<DataSet> {
		@Override
		public int compare(DataSet t0, DataSet t1) {
			Calendar now = Calendar.getInstance();
			Calendar t0Calendar = Calendar.getInstance();
			Calendar t1Calendar = Calendar.getInstance();

			t0Calendar.setTimeInMillis(t0.birthday.getTimeInMillis());
			t1Calendar.setTimeInMillis(t1.birthday.getTimeInMillis());

			t0Calendar.set(Calendar.YEAR, now.get(Calendar.YEAR));
			t1Calendar.set(Calendar.YEAR, now.get(Calendar.YEAR));

			int t0Days = t0Calendar.get(Calendar.DAY_OF_YEAR);
			int t1Days = t1Calendar.get(Calendar.DAY_OF_YEAR);

			if (t0Days > t1Days) return 1;
			if (t0Days < t1Days) return -1;

			int t0Year = t0.birthday.get(Calendar.YEAR);
			int t1Year = t1.birthday.get(Calendar.YEAR);

			if (t0Year > t1Year) return 1;
			if (t0Year < t1Year) return -1;

			return 0;
		}
	}

	/**
	 * compares two datasets using String.compareToIgnoreCase, with respect to the name of the dataset.
	 */
	public static class NameComparator implements Comparator<DataSet> {
		@Override
		public int compare(DataSet t0, DataSet t1) {
			String t0Name, t1Name;

			if (TextUtils.isEmpty(t0.firstName)) {
				t0Name = t0.lastName;
			} else if (TextUtils.isEmpty(t0.lastName)) {
				t0Name = t0.firstName;
			} else {
				t0Name = t0.firstName + " " + t0.lastName;
			}

			if (TextUtils.isEmpty(t1.firstName)) {
				t1Name = t1.lastName;
			} else if (TextUtils.isEmpty(t1.lastName)) {
				t1Name = t1.firstName;
			} else {
				t1Name = t1.firstName + " " + t1.lastName;
			}

			return t0Name.compareToIgnoreCase(t1Name);
		}
	}

	/**
	 * compares two datasets. the dataset with the younger age will be returned as smaller
	 */
	public static class AgeComparator implements Comparator<DataSet> {
		@Override
		public int compare(DataSet t0, DataSet t1) {
			return t1.birthday.compareTo(t0.birthday);
		}
	}

}

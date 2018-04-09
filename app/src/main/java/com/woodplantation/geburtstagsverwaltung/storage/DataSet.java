package com.woodplantation.geburtstagsverwaltung.storage;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class DataSet implements Serializable, Comparable<DataSet> {

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

	public int getRemaining(Calendar now) {
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

	@Override
	public int compareTo(@NonNull DataSet another) {
		try {
			Calendar now = Calendar.getInstance();
			int tRemains = getRemaining(now);
			int oRemains = another.getRemaining(now);

			if (tRemains > oRemains) return 1;
			if (tRemains < oRemains) return -1;

			int tYear = birthday.get(Calendar.YEAR);
			int oYear = another.birthday.get(Calendar.YEAR);

			if (tYear > oYear) return 1;
			else return -1;
		} catch (NullPointerException e) {
			//if we catch nullpointerexception, we dont know what to do and return -1. whatever.
			return -1;
		}
	}
}

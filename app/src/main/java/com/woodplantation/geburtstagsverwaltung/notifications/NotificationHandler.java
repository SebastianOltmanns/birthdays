package com.woodplantation.geburtstagsverwaltung.notifications;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.woodplantation.geburtstagsverwaltung.storage.DataSet;
import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.storage.StorageHandler;
import com.woodplantation.geburtstagsverwaltung.activities.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Sebu on 10.03.2016.
 * Contact: sebastian.oltmanns.developer@gmail.com
 *
 */
public class NotificationHandler extends BroadcastReceiver {

	public static final String INTENT_NOTIFICATION_DATASET = "NOTIFICATION_DATASET";
	public static final String INTENT_NOTIFICATION_ID = "NOTIFICATION_ID";
	public static final String INTENT_NOTIFICATION_WHICH = "NOTIFICATION_WHICH";
	public static final String INTENT_NOTIFICATION_WHEN = "NOTIFICATION_WHEN";

	private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm a", Locale.GERMAN);

	private static final String FILEPATH_IDMAP = "idmap";
	private static final String FILEPATH_LASTID = "lastid";

	private static void save(Context context, String filePath, Object object) {
		try {
			FileOutputStream fos = context.openFileOutput(filePath, Context.MODE_PRIVATE);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream output = new ObjectOutputStream(bos);

			output.writeObject(object);

			output.close();
			bos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Object load(Context context, String filePath) {
		try {
			FileInputStream fis = context.openFileInput(filePath);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream input = new ObjectInputStream(bis);

			Object ret = input.readObject();

			input.close();
			bis.close();
			fis.close();
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Integer getNewId(Context context) {
		Object object = load(context, FILEPATH_LASTID);
		int newId;
		if (object == null) {
			newId = 0;
		} else {
			newId = (Integer) object+1;
		}
		save(context, FILEPATH_LASTID, newId);
		return newId;
	}

	private static Integer[] getNewIds(Context context) {
		Object object = load(context, FILEPATH_LASTID);
		Integer[] newIds;
		if (object == null) {
			newIds = new Integer[]{0,1,2};
		} else {
			int oldId = (int) object;
			newIds = new Integer[]{oldId+1, oldId+2, oldId+3};
		}
		save(context, FILEPATH_LASTID, newIds[2]);
		return newIds;
	}

	private static Integer refreshId(Context context, DataSet dataSet, int which) {
		Object object = load(context, FILEPATH_IDMAP);
		HashMap<Integer,Integer[]> idMap;
		if (object == null) {
			idMap = new HashMap<Integer,Integer[]>();
		} else {
			idMap = (HashMap<Integer,Integer[]>) object;
		}
		Integer[] ids = idMap.get(dataSet.getId());
		if (ids == null) {
			ids = getNewIds(context);
		} else {
			ids[which] = getNewId(context);
		}
		idMap.put(dataSet.getId(), ids);
		save(context, FILEPATH_IDMAP, idMap);
		return ids[which];
	}

	private static void deleteId(Context context, DataSet dataSet, int which) {
		Object object = load(context, FILEPATH_IDMAP);
		HashMap<Integer,Integer[]> idMap;
		if (object == null) {
			return;
		}
		idMap = (HashMap<Integer,Integer[]>) object;
		Integer[] ids = idMap.get(dataSet.getId());
		ids[which] = -1;
		boolean flagDeleteAll = true;
		for (int i = 0; i < 3; i++) {
			if (ids[i] != -1) {
				flagDeleteAll = false;
				break;
			}
		}
		if (flagDeleteAll) {
			idMap.remove(dataSet.getId());
		} else {
			idMap.put(dataSet.getId(), ids);
		}
		save(context, FILEPATH_IDMAP, idMap);
	}

	private static Integer[] getIds(Context context, DataSet dataSet) {
		Object object = load(context, FILEPATH_IDMAP);
		HashMap<Integer,Integer[]> idMap;
		if (object == null) {
			Log.d("NotificationHandler","getIds. object null!");
			idMap = new HashMap<Integer,Integer[]>();
		} else {
			idMap = (HashMap<Integer,Integer[]>) object;
		}
		Integer[] ids = idMap.get(dataSet.getId());
		if (ids == null) {
			Log.d("NotificationHandler","getIds. ids null!");
			ids = getNewIds(context);
			idMap.put(dataSet.getId(), ids);
			save(context, FILEPATH_IDMAP, idMap);
			return ids;
		}
		for (int i = 0; i < 3; i++) {
			if (ids[i] == -1) {
				ids[i] = getNewId(context);
			}
		}
		idMap.put(dataSet.getId(), ids);
		save(context, FILEPATH_IDMAP, idMap);
		Log.d("Notificationhandler","getids. idmap: " + idMap);
		return ids;
	}

	/**
	 *
	 * @param birthday the birthday
	 * @param which true for which ones we need (preferences). 0: on birthday, 1: one day before, 2: x days before (must be size 3)
	 * @param clocks hour * 60 + minutes: the time at which calendar should be, fitting to parameter which
	 * @param xDaysBefore amount of days before birhtday. should be value MyPreferences.getXDaysBeforeDays
	 * @return the calendars for alarms. null if which was false
	 */
	private static Calendar[] getCalendars(Calendar birthday, boolean[] which, int[] clocks, int xDaysBefore) {
		Calendar[] calendars = new Calendar[3];
		for (int i = 0; i < 3; i++) {
			if (!which[i]) {
				calendars[i] = null;
			} else {
				Calendar calendar = getNextBirthday(birthday);
				calendar.set(Calendar.HOUR_OF_DAY, clocks[i] / 60);
				calendar.set(Calendar.MINUTE, clocks[i] % 60);
				if (i == 1) {
					calendar.add(Calendar.DAY_OF_YEAR, -1);
				} else if (i == 2) {
					calendar.add(Calendar.DAY_OF_YEAR, -xDaysBefore);
				}
				calendars[i] = calendar;
			}
		}
		return calendars;
	}

	private static boolean[] getWhich(MyPreferences pref) {
		boolean[] which = new boolean[3];
		which[0] = pref.getOnBirthdayActive();
		which[1] = pref.getOneDayBeforeActive();
		which[2] = pref.getXDaysBeforeActive();
		return which;
	}

	private static int[] getClocks(MyPreferences pref) {
		int[] clocks = new int[3];
		clocks[0] = pref.getOnBirthdayClock();
		clocks[1] = pref.getOneDayBeforeClock();
		clocks[2] = pref.getXDaysBeforeClock();
		return clocks;
	}

	private static void create(Context context, int id, Calendar when, DataSet dataSet, int which, boolean update) {
		String name = dataSet.getFirstName() + " " + dataSet.getLastName();
		Intent intent = new Intent(context, NotificationHandler.class);
		intent.putExtra(INTENT_NOTIFICATION_DATASET, dataSet);
		intent.putExtra(INTENT_NOTIFICATION_ID, id);
		intent.putExtra(INTENT_NOTIFICATION_WHICH, which);
		intent.putExtra(INTENT_NOTIFICATION_WHEN, when);
		int flag = update ? PendingIntent.FLAG_CANCEL_CURRENT : 0;
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent, flag);

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC, when.getTimeInMillis(), pendingIntent);

		Log.d("NotificationHandler", "alarm for " + name + " got created! time: " + sdf.format(when.getTime()) + " with id: " + id);
	}

	private static void addOrChangeBirthday(Context context, DataSet dataSet, boolean[] which, int[] clocks, int xDaysBefore, boolean update) {
		Integer[] ids = getIds(context, dataSet);
		Calendar[] calendars = getCalendars(dataSet.getBirthday(), which, clocks, xDaysBefore);
		for (int i = 0; i < 3; i++) {
			if (which[i]) {
				create(context,
						ids[i],
						calendars[i],
						dataSet,
						i,
						update);
			}
		}
	}

	private static void deleteBirthday(Context context, DataSet dataSet, boolean[] which) {
		Integer[] ids = getIds(context, dataSet);
		for (int i = 0; i < 3; i++) {
			if (which[i]) {
				Log.d("notificationHandler","deleting " + dataSet.getFirstName() + " " + dataSet.getLastName() + " which: " + i);
				deleteId(context, dataSet, i);
				Intent intent = new Intent(context, NotificationHandler.class);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ids[i], intent, PendingIntent.FLAG_CANCEL_CURRENT);

				AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				am.cancel(pendingIntent);
			}
		}
	}

	public static void addBirthday(Context context, DataSet dataSet) {
		MyPreferences pref = new MyPreferences(context);
		addOrChangeBirthday(context, dataSet, getWhich(pref), getClocks(pref), pref.getXDaysBeforeDays(), false);
	}

	public static void updateBirthday(Context context, DataSet dataSet) {
		MyPreferences pref = new MyPreferences(context);
		addOrChangeBirthday(context, dataSet, getWhich(pref), getClocks(pref), pref.getXDaysBeforeDays(), true);
	}

	public static void deleteBirthday(Context context, DataSet dataSet) {
		MyPreferences pref = new MyPreferences(context);
		deleteBirthday(context, dataSet, getWhich(pref));
	}

	private static void addOrChangeAllBirthdays(Context context, ArrayList<DataSet> data, boolean[] which, boolean update) {
		boolean cancelFlag = true;
		for (boolean b : which) {
			if (b) {
				cancelFlag = false;
				break;
			}
		}
		if (cancelFlag) {
			return;
		}

		MyPreferences pref = new MyPreferences(context);
		int[] clocks = getClocks(pref);
		int xDaysBefore = pref.getXDaysBeforeDays();
		for (DataSet dataSet : data) {
			addOrChangeBirthday(context, dataSet, which, clocks, xDaysBefore, update);
		}
	}

	private static void deleteAllBirthdays(Context context, ArrayList<DataSet> data, boolean[] which) {
		boolean cancelFlag = true;
		for (boolean b : which) {
			if (b) {
				cancelFlag = false;
				break;
			}
		}
		if (cancelFlag) {
			return;
		}

		for (DataSet dataSet : data) {
			deleteBirthday(context, dataSet, which);
		}
	}

	public static void handlePreferences(Context context, Map<String, ?> oldPref, ArrayList<DataSet> data) {
		boolean[] oldWhich = new boolean[3];
		oldWhich[0] = (Boolean) oldPref.get(context.getString(R.string.preferences_on_birthday_active));
		oldWhich[1] = (Boolean) oldPref.get(context.getString(R.string.preferences_one_day_before_active));
		oldWhich[2] = (Boolean) oldPref.get(context.getString(R.string.preferences_x_days_before_active));

		MyPreferences pref = new MyPreferences(context);
		boolean[] newWhich = getWhich(pref);

		int[] oldClocks = new int[3];
		oldClocks[0] = (Integer) oldPref.get(context.getString(R.string.preferences_on_birthday_clock));
		oldClocks[1] = (Integer) oldPref.get(context.getString(R.string.preferences_one_day_before_clock));
		oldClocks[2] = (Integer) oldPref.get(context.getString(R.string.preferences_x_days_before_clock));
		int oldXDaysBeforeDays = (Integer) oldPref.get(context.getString(R.string.preferences_x_days_before_days));

		int[] newClocks = getClocks(pref);
		int newXDaysBeforeDays = pref.getXDaysBeforeDays();

		//Falls jetzt keine Notifikiation aktiv sind
		if (!pref.getActive()) {
			//Falls vorher Notifikation aktiv waren: alle löschen
			if ((Boolean) oldPref.get(context.getString(R.string.preferences_active))) {
				deleteAllBirthdays(context, data, oldWhich);
				Log.d("NotificationHandler","handle pref ruft delete all auf mit which: " + Arrays.toString(oldWhich));
			}
			return;
		}

		//Notifikation sind aktiv.

		//Falls vorher keine aktiv waren: alle erstellen
		if (!(Boolean) oldPref.get(context.getString(R.string.preferences_active))) {
			addOrChangeAllBirthdays(context, data, newWhich, false);
			Log.d("NotificationHandler","handle pref ruft add all auf mit which: " + Arrays.toString(newWhich));
			return;
		}

		//Notifikationen ändern
		boolean[] whichDelete = new boolean[3];
		boolean[] whichAdd = new boolean[3];
		boolean[] whichChange = new boolean[3];
		for (int i = 0; i < 3; i++) {
			boolean clockChanged = (oldClocks[i] != newClocks[i])
					|| (i == 2 && oldXDaysBeforeDays != newXDaysBeforeDays);

			//add: falls vorher nicht aktiv, jetzt schon
			whichAdd[i] = !oldWhich[i] && newWhich[i];
			//delete: falls vorher aktiv, jetzt nicht
			whichDelete[i] = oldWhich[i] && !newWhich[i];
			//change: falls vorher aktiv, jetzt auch
			whichChange[i] = oldWhich[i] && newWhich[i] && (clockChanged);
		}
		Log.d("NotificationHandler","handle pref ruft delete all auf mit which: " + Arrays.toString(whichDelete));
		deleteAllBirthdays(context, data, whichDelete);
		Log.d("NotificationHandler","handle pref ruft add all auf mit which: " + Arrays.toString(whichAdd));
		addOrChangeAllBirthdays(context, data, whichAdd, false);
		Log.d("NotificationHandler","handle pref ruft change all auf mit which: " + Arrays.toString(whichChange));
		addOrChangeAllBirthdays(context, data, whichChange, true);
	}

	private static Calendar getNextBirthday(Calendar _birthday) {
		Calendar birthday = Calendar.getInstance();
		Calendar now = Calendar.getInstance();
		birthday.setTimeInMillis(_birthday.getTimeInMillis());
		birthday.set(Calendar.YEAR, now.get(Calendar.YEAR));
		if (birthday.get(Calendar.DAY_OF_YEAR) <= now.get(Calendar.DAY_OF_YEAR)) {
			birthday.add(Calendar.YEAR, 1);
		}
		return birthday;
	}

	public static void afterBootCompleted(Context context) {
		StorageHandler storageHandler = new StorageHandler(context);
		ArrayList<DataSet> data = storageHandler.loadData();
		boolean[] which = getWhich(new MyPreferences(context));
		addOrChangeAllBirthdays(context, data, which, false);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("notificationhandler", "onreceive");

		//get id and check it
		int id = intent.getIntExtra(INTENT_NOTIFICATION_ID, -1);
		if (id == -1){
			return;
		}

		int which = intent.getIntExtra(INTENT_NOTIFICATION_WHICH, -1);
		if (which == -1) {
			return;
		}

		DataSet dataSet = (DataSet) intent.getSerializableExtra(INTENT_NOTIFICATION_DATASET);
		if (dataSet == null) {
			return;
		}

		Calendar when = (Calendar) intent.getSerializableExtra(INTENT_NOTIFICATION_WHEN);
		if (when == null) {
			return;
		}

		//create for next year
		int newId = refreshId(context, dataSet, which);
		Calendar newDate = Calendar.getInstance();
		newDate.setTimeInMillis(when.getTimeInMillis());
		newDate.add(Calendar.YEAR, 1);
		create(context, newId, newDate, dataSet, which, false);

		//check if we show notification now or if we are passed the time (e.g. user did time settings)

		Calendar birthday = dataSet.getBirthday();
		Calendar now = Calendar.getInstance();

		String text = "";
		String name = dataSet.getFirstName() + " " + dataSet.getLastName();

		if (when.get(Calendar.YEAR) != now.get(Calendar.YEAR)
				|| when.get(Calendar.DAY_OF_YEAR) != now.get(Calendar.DAY_OF_YEAR)) {
			Log.d("Notificationhandler","cancelling. now: " + sdf.format(now.getTime()) + " and when: " + sdf.format(when.getTime()));
			return;
		}


		Log.d("NotificationHandler","before switch. which: " + which);

		switch (which) {
			case 0:
				text = context.getString(R.string.content_text_today, name);
				break;
			case 1: {
				text = context.getString(R.string.content_text_tomorrow, name);
				break;
			}
			case 2: {
				Calendar nextBirthday = getNextBirthday(birthday);
				int diff = nextBirthday.get(Calendar.DAY_OF_YEAR) - now.get(Calendar.DAY_OF_YEAR);
				if (diff < 0) {
					diff += now.getMaximum(Calendar.DAY_OF_YEAR);
				}
				if (diff < 2 || diff > 28) {
					return;
				}
				text = context.getString(R.string.content_text_in_x_days, name, diff);
				break;
			}
		}

		//create notification:
		Intent notificationIntent = new Intent(context, MainActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pi = PendingIntent.getActivity(context, id, notificationIntent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.ic_event_note);
		builder.setContentTitle(context.getString(R.string.content_title));
		builder.setContentText(text);
		builder.setContentIntent(pi);
		builder.setAutoCancel(true);

		Log.d("notificationhandler", "setting up notification for: " + name);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(id, builder.build());
	}
}

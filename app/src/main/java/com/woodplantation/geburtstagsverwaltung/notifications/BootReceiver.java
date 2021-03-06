package com.woodplantation.geburtstagsverwaltung.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;
import com.woodplantation.geburtstagsverwaltung.widget.WidgetAlarmReceiver;
import com.woodplantation.geburtstagsverwaltung.widget.WidgetService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Created by Sebu on 27.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
@AndroidEntryPoint
public class BootReceiver extends BroadcastReceiver {

	@Inject
	MyPreferences preferences;

	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmCreator.createFromScratch(context, preferences);
		WidgetAlarmReceiver.createNextAlarm(context);
		WidgetService.notifyDataChanged(context);
	}
}

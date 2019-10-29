package com.woodplantation.geburtstagsverwaltung.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.woodplantation.geburtstagsverwaltung.widget.WidgetAlarmReceiver;
import com.woodplantation.geburtstagsverwaltung.widget.WidgetService;

/**
 * Created by Sebu on 27.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmCreator.createFromScratch(context);
		WidgetAlarmReceiver.createNextAlarm(context);
		WidgetService.notifyDataChanged(context);
	}
}

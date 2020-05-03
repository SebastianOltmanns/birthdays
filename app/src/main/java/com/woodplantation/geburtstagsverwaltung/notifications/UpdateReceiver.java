package com.woodplantation.geburtstagsverwaltung.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.woodplantation.geburtstagsverwaltung.widget.WidgetAlarmReceiver;
import com.woodplantation.geburtstagsverwaltung.widget.WidgetService;

/**
 * Created by Sebu on 23.08.2017.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */

public class UpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmCreator.createFromScratch(context);
        WidgetAlarmReceiver.createNextAlarm(context);
        WidgetService.notifyDataChanged(context);
    }
}

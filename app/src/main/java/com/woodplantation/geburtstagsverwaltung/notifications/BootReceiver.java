package com.woodplantation.geburtstagsverwaltung.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Sebu on 27.03.2016.
 * Contact: sebastian.oltmanns.developer@googlemail.com
 */
public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			NotificationHandler.createAllNotifications(context);
		}
	}
}

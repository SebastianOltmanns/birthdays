package com.woodplantation.geburtstagsverwaltung.notifications;

import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.woodplantation.geburtstagsverwaltung.R;
import com.woodplantation.geburtstagsverwaltung.activities.MainActivity;
import com.woodplantation.geburtstagsverwaltung.model.Entry;
import com.woodplantation.geburtstagsverwaltung.repository.Repository;
import com.woodplantation.geburtstagsverwaltung.util.DateUtil;
import com.woodplantation.geburtstagsverwaltung.util.IntentCodes;
import com.woodplantation.geburtstagsverwaltung.util.MyPreferences;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

/**
 * Created by Sebu on 29.10.2019.
 * Contact: sebastian.oltmanns.developer@gmail.com
 */
public class AlarmReceiver extends BroadcastReceiver {

	private static final int JOB_ID = 0;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("alarmreceiver","onreceive!");
		JobScheduler jobScheduler = (JobScheduler) context.getSystemService(JOB_SCHEDULER_SERVICE);
		ComponentName serviceName = new ComponentName(context.getPackageName(), NotificationJobService.class.getName());
		PersistableBundle extras = new PersistableBundle();
		extras.putInt(IntentCodes.WHICH, intent.getIntExtra(IntentCodes.WHICH, -1));
		JobInfo jobInfo = new JobInfo
				.Builder(JOB_ID, serviceName)
				.setExtras(extras)
				// this is needed as a pseudo constraint, because creating a job without constraint is not allowed (WTF GOOGLE)
				// see also https://stackoverflow.com/questions/51064731/firing-jobservice-without-constraints
				.setOverrideDeadline(0)
				.build();
		jobScheduler.schedule(jobInfo);
	}



}

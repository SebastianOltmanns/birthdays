package com.woodplantation.geburtstagsverwaltung.notifications;

import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;

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
import io.reactivex.functions.Consumer;

@AndroidEntryPoint
public class NotificationJobService extends JobService {

    @Inject
    Repository repository;
    @Inject
    MyPreferences preferences;

    /**
     * this is called on main thread. <br/>
     * this starts asynchronously getting the data. <br/>
     * see also https://developer.android.com/codelabs/android-training-job-scheduler#2
     * @return true because true notifies that asynchronous work on another thread is done.
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        repository.getData(dataCallback(params), failureCallback(params));
        return true;
    }

    /**
     * this callback iterates all data and checks for what birthday a notification should be shown.
     */
    private Consumer<List<Entry>> dataCallback(JobParameters params) {
        return dataList -> {
            // get which alarm this is
            int which = params.getExtras().getInt(IntentCodes.WHICH, -1);
            // get preferences
            int xDaysBeforeDays = preferences.getXDaysBeforeDays();
            // iterate all data
            LocalDate now = LocalDate.now();
            for (Entry data : dataList) {
                LocalDate birthdayAlarm = DateUtil.getNextBirthday(data.birthday);
                if (which == 1) {
                    birthdayAlarm = birthdayAlarm.minus(1, ChronoUnit.DAYS);
                } else if (which == 2) {
                    birthdayAlarm = birthdayAlarm.minus(xDaysBeforeDays, ChronoUnit.DAYS);
                }

                if (now.equals(birthdayAlarm)) {
                    // show notification
                    createNotification(getApplicationContext(), data, which, xDaysBeforeDays, preferences);
                }
            }

            // notify that the job is finished
            jobFinished(params, false);
        };
    }

    private void createNotification(Context context, Entry dataSet, int which, int xDaysBeforeDays, MyPreferences notificationPreferences) {
        String dayText;
        if ((which < 0) || (which > 2)) {
            // if which is something weird, we find out how many days are left on our own
            LocalDate now = LocalDate.now();
            LocalDate nextBirthday = DateUtil.getNextBirthday(dataSet.birthday);
            long diffDays = nextBirthday.toEpochDay() - now.toEpochDay();
            if (diffDays == 0) {
                which = 0;
            } else if (diffDays == 1) {
                which = 1;
            } else {
                which = 2;
            }
        }
        switch (which) {
            case 0: {
                dayText = context.getString(R.string.today);
                break;
            }
            case 1: {
                dayText = context.getString(R.string.tomorrow);
                break;
            }
            default: {
                dayText = context.getString(R.string.in_x_days, xDaysBeforeDays);
                break;
            }
        }

        String name = dataSet.firstName + " " + dataSet.lastName;
        int textResource;
        if (name.endsWith("s")) {
            textResource = R.string.content_text_name_ending_with_s;
        } else {
            textResource = R.string.content_text;
        }
        String text = context.getString(textResource, dayText, name);


        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id));
        builder.setSmallIcon(R.drawable.ic_event_note);
        builder.setContentTitle(context.getString(R.string.content_title));
        builder.setContentText(text);
        builder.setContentIntent(pi);
        builder.setAutoCancel(true);
        builder.setCategory(NotificationCompat.CATEGORY_REMINDER);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationPreferences.getNextNotificationId(), builder.build());
    }

    /**
     * this callback is called when loading data was not successful. the job
     * will be rescheduled using {@link JobService#jobFinished(JobParameters, boolean)}.
     * this applies an exponential backoff, s.t. no infinite-loop happens.
     */
    private Consumer<Throwable> failureCallback(JobParameters params) {
        return throwable -> {
            throwable.printStackTrace();
            jobFinished(params, true);
        };
    }

    /**
     * this disposes the current reactive work.
     * @return true because we want to get rescheduled.
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        repository.compositeDisposable.dispose();
        return true;
    }
}

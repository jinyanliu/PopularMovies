package se.sugarest.jane.popularmovies.notification.lowerversion;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

/**
 * Created by jane on 17-8-31.
 */

public class ScheduleNotificationLowerVersion {

    private static boolean sInitialized;

    private static final String NOTIFICATION_JOB_TAG = "notification_job_tag";

    private static final String TAG = ScheduleNotificationLowerVersion.class.getSimpleName();

    /***********************************************************************************************
     * Use JobScheduler will have a minimum interval time 15 min, a minimum flex time 5 min.       *
     * But use the FirebaseJobDispatcher doesn't have a minimun interval, nor a minimun flex time. *
     ***********************************************************************************************/

    /*
     * Interval to notify. Use TimeUnit for convenience, rather
     * than writing out a bunch of multiplication ourselves and risk making a silly mistake.
     */
    private static final int NOTIFICATION_INTERVAL_MINUTES = 24 * 60;
    private static final int NOTIFICATION_INTERVAL_SECONDS
            = (int) (TimeUnit.MINUTES.toSeconds(NOTIFICATION_INTERVAL_MINUTES)); // one day
    private static final int NOTIFICATION_FLEXTIME_SECONDS = 10; // 10 seconds

    synchronized public static void scheduleNotification(@NonNull final Context context) {
        Log.i(TAG, "jag support lower version schedule notification!");

        if (sInitialized) {
            return;
        }

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        /* Create the Job to periodically create notifications */
        Job constraintNotificationJob = dispatcher.newJobBuilder()
                /* The Service that will be used to write to preferences */
                .setService(NotificationFirebaseJobService.class)
                /* Set the UNIQUE tag used to identify this Job.*/
                .setTag(NOTIFICATION_JOB_TAG)
                /*
                 * setLifetime sets how long this job should persist. The options are to keep the
                 * Job "forever" or to have it die the next time the device boots up.
                 */
                .setLifetime(Lifetime.FOREVER)
                /*
                 * We want these notifications to continuously happen, so we tell this Job to recur.
                 */
                .setRecurring(true)
                 /*
                 * We want the notification to happen every 15 minutes or so. The first argument for
                 * Trigger class's static executionWindow method is the start of the time frame
                 * when the
                 * job should be performed. The second argument is the latest point in time at
                 * which the data should be synced. Please note that this end time is not
                 * guaranteed, but is more of a guideline for FirebaseJobDispatcher to go off of.
                 */
                .setTrigger(Trigger.executionWindow(
                        NOTIFICATION_INTERVAL_SECONDS,
                        NOTIFICATION_INTERVAL_SECONDS + NOTIFICATION_FLEXTIME_SECONDS))
                /*
                 * If a Job with the tag with provided already exists, this new job will replace
                 * the old one.
                 */
                .setReplaceCurrent(true)
                /* Once the Job is ready, call the builder's build method to return the Job */
                .build();

         /* Schedule the Job with the dispatcher */
        dispatcher.schedule(constraintNotificationJob);

        /* The job has been initialized */
        sInitialized = true;
    }
}

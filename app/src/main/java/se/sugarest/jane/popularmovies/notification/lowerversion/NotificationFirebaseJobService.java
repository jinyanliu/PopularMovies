package se.sugarest.jane.popularmovies.notification.lowerversion;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import se.sugarest.jane.popularmovies.notification.NotificationTasks;

/**
 * Created by jane on 17-8-31.
 */

public class NotificationFirebaseJobService extends JobService {

    private static final String TAG = NotificationFirebaseJobService.class.getSimpleName();

    private AsyncTask mBackgroundTask;

    /**
     * The entry point to your Job. Implementations should offload work to another thread of
     * execution as soon as possible.
     * <p>
     * This is called by the Job Dispatcher to tell us we should start our job. Keep in mind this
     * method is run on the application's main thread, so we need to offload work to a background
     * thread.
     *
     * @return whether there is more work remaining.
     */
    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.i(TAG, "notification firebase job service onstartjob get called.");

        mBackgroundTask = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                Log.i(TAG, "notification firebase job service doinbackground get called.");
                Context context = NotificationFirebaseJobService.this;
                NotificationTasks.executeTask(context, NotificationTasks.ACTION_NOTIFY);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                /*
                 * Once the AsyncTask is finished, the job is finished. To inform JobManager that
                 * you're done, you call jobFinished with the jobParamters that were passed to your
                 * job and a boolean representing whether the job needs to be rescheduled. This is
                 * usually if something didn't work and you want the job to try running again.
                 */

                jobFinished(params, false);
            }
        };
        mBackgroundTask.execute();
        return true;
    }

    /**
     * Called when the scheduling engine has decided to interrupt the execution of a running job,
     * most likely because the runtime constraints associated with the job are no longer satisfied.
     *
     * @return whether the job should be retried
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        if (mBackgroundTask != null) {
            mBackgroundTask.cancel(true);
        }
        return true;
    }
}

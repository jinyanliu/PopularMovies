package se.sugarest.jane.popularmovies.jobscheduler.jobservice;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import se.sugarest.jane.popularmovies.notification.NotificationTasks;

/**
 * Created by jane on 17-8-31.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NotificationService extends JobService {

    final static String TAG = NotificationService.class.getSimpleName();

    /**
     * @return false because the job in onStartJob is really short. Send the broadcast the job is
     * done. We should let the system know that the job is done. And after the periodic intervals,
     * the onStartJob() will be fired up again.
     */
    @Override
    public boolean onStartJob(JobParameters params) {

        Log.i(TAG, "Halloooooooooo, jag ar pa start job notification vag.");

        Context context = NotificationService.this;
        NotificationTasks.executeTask(context, NotificationTasks.ACTION_NOTIFY);
        return false;
    }

    /**
     * Because the task in onStartJob() is very short, so this method probably won't be called.
     * And we can do nothing in it.
     *
     * @return true so if something happens, and the job stops in the middle, it will reschedule.
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "Halloooooooooo, jag ar pa stop job notification vag.");
        return true;
    }
}

package se.sugarest.jane.popularmovies.jobscheduler.jobservice;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

/**
 * Created by jane on 17-8-29.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class UpdateWidgetService extends JobService {

    public static final String ACTION_DATA_UPDATED = "se.sugarest.jane.popularmovies.ACTION_DATA_UPDATED";

    final static String TAG = UpdateWidgetService.class.getSimpleName();

    /**
     * @return false because the job in onStartJob is really short. Send the broadcast the job is
     * done. We should let the system know that the job is done. And after the periodic intervals,
     * the onStartJob() will be fired up again.
     */
    @Override
    public boolean onStartJob(JobParameters params) {

        Log.i(TAG, "Halloooooooooo, jag ar pa start job update widget vag.");

        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
        this.sendBroadcast(dataUpdatedIntent);
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
        Log.i(TAG, "Halloooooooooo, jag ar pa stop job update widget vag.");
        return true;
    }
}

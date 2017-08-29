package se.sugarest.jane.popularmovies.jobscheduler;

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

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.i(TAG, "Halloooooooooo, jag ar pa start job update widget vag.");

        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
        this.sendBroadcast(dataUpdatedIntent);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "Stop update widget service.");
        return true;
    }
}

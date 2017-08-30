package se.sugarest.jane.popularmovies.jobscheduler.jobservice;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import se.sugarest.jane.popularmovies.jobscheduler.PersistFavMovie;

/**
 * Created by jane on 17-7-10.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PersistFavService extends JobService {

    private static final String TAG = PersistFavService.class.getSimpleName();

    @Override
    public boolean onStartJob(final JobParameters params) {

        Log.i(TAG, "Halloooooooooo, jag ar pa start job fav vag.");

        PersistFavMovie.persistFavMovie(this);

        return false;
    }

    /**
     * Because 2 background task were executed, that will need to be canceled if they are still
     * running. This is where you want to be very careful, because any lingering threads could
     * create a memory leak in the app! So clean up the code !
     *
     * @return true so if something happens, and the job stops in the middle, it will reschedule.
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(TAG, "Halloooooooooo, jag ar pa stop job fav vag.");
        return true;
    }
}

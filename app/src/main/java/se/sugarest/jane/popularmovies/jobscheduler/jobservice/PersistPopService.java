package se.sugarest.jane.popularmovies.jobscheduler.jobservice;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.List;

import se.sugarest.jane.popularmovies.jobscheduler.PersistPopMovieTask;
import se.sugarest.jane.popularmovies.movie.Movie;

/**
 * Created by jane on 17-7-10.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PersistPopService extends JobService {

    private static final String TAG = PersistPopService.class.getSimpleName();

    PersistPopMovieTask mPersistPopMovieTask;

    /**
     * @return true because the job in onStartJob is long, and on the other thread, won't be finish
     * in a second. We should let the system know that the job needs time to finish, but override
     * onPostExecute() to let the system know that the job is done. And after the periodic intervals,
     * the onStartJob() will be fired up again.
     */
    @Override
    public boolean onStartJob(final JobParameters params) {

        Log.i(TAG, "Halloooooooooo, jag ar pa start job popular vag.");

        mPersistPopMovieTask = new PersistPopMovieTask(this) {
            @Override
            protected void onPostExecute(List<Movie> movieData) {
                jobFinished(params, false);
                Log.i(TAG, "Halloooooooooo, jag ar pa finish job popular vag.");
            }
        };

        mPersistPopMovieTask.execute();
        return true;
    }

    /**
     * Because background task was executed, that will need to be canceled if it is still
     * running. This is where you want to be very careful, because any lingering threads could
     * create a memory leak in the app! So clean up the code !
     *
     * @return true so if something happens, and the job stops in the middle, it will reschedule.
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        if (mPersistPopMovieTask != null) {
            mPersistPopMovieTask.cancel(true);
        }
        Log.i(TAG, "Halloooooooooo, jag ar pa stop job popular vag.");
        return true;
    }
}


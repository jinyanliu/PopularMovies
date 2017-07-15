package se.sugarest.jane.popularmovies.jobscheduler;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

/**
 * Created by jane on 17-7-10.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class FetchMovieService extends JobService {

    private static final String TAG = FetchMovieService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {

        new PersistPopMovieTask(this.getApplicationContext()).execute();

        Log.i(TAG, "Halloooooooooo, jag ar pa start job popular vag.");

        new PersistTopMovieTask(this.getApplicationContext()).execute();

        Log.i(TAG, "Halloooooooooo, jag ar pa start job top vag.");

        PersistFavMovie.persistFavMovie(this.getApplicationContext());

        Log.i(TAG, "Halloooooooooo, jag ar pa start job fav vag.");

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(FetchMovieService.class.getName(), "Stop fetch movie service.");
        return true;
    }
}

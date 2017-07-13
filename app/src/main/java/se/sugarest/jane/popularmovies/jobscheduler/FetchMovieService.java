package se.sugarest.jane.popularmovies.jobscheduler;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

import se.sugarest.jane.popularmovies.R;
import se.sugarest.jane.popularmovies.tasks.PersistMovieTask;

/**
 * Created by jane on 17-7-10.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class FetchMovieService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        PersistMovieTask persistMovieTask = new PersistMovieTask(this.getApplicationContext());
        String orderBy = "movie/" + getPreference();
        persistMovieTask.execute(orderBy);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.i(FetchMovieService.class.getName(), "Stop fetch movie service.");
        return true;
    }

    private String getPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }
}

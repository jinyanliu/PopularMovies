package se.sugarest.jane.popularmovies.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jane on 17-8-31.
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Alarm: On Alarm Receive !");
        WatchMovieNotificationUtils.notifyUserWatchMovie(context);
    }
}

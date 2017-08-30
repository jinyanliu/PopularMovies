package se.sugarest.jane.popularmovies.notification;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by jane on 17-8-30.
 */

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class IgnoreNotificationIntentService extends IntentService {

    public IgnoreNotificationIntentService() {
        super("IgnoreNotificationIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String action = intent.getAction();
        IgnoreNotificationTask.executeTask(this, action);
    }
}

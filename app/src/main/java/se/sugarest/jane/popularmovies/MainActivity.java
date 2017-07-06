package se.sugarest.jane.popularmovies;

import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieMostPopularEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.CacheMovieTopRatedEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.FavMovieEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.ReviewEntry;
import se.sugarest.jane.popularmovies.data.MovieContract.TrailerEntry;
import se.sugarest.jane.popularmovies.movie.FullMovie;
import se.sugarest.jane.popularmovies.movie.MovieAdapter;
import se.sugarest.jane.popularmovies.movie.MovieAdapter.MovieAdapterOnClickHandler;
import se.sugarest.jane.popularmovies.movie.MovieBasicInfo;
import se.sugarest.jane.popularmovies.tasks.FetchExternalStorageFavMovieImageThumbnailsTask;
import se.sugarest.jane.popularmovies.tasks.FetchExternalStorageFavMoviePosterImagesTask;
import se.sugarest.jane.popularmovies.tasks.FetchMoviePostersTask;
import se.sugarest.jane.popularmovies.tasks.PersistMovieTask;

public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler
        , android.app.LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final String BASE_IMAGE_URL = "http://image.tmdb.org/t/p/";
    private final String IMAGE_SIZE_W780 = "w780/";
    private final String IMAGE_SIZE_W185 = "w185/";

    public static final int MOVIE_LOADER = 0;

    private Toast mToast;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;

    private MovieAdapter mMovieAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    public MovieAdapter getmMovieAdapter() {
        return mMovieAdapter;
    }

    public ProgressBar getmLoadingIndicator() {
        return mLoadingIndicator;
    }

    public SwipeRefreshLayout getmSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    public Toast getmToast() {
        return mToast;
    }

    public void setmToast(Toast mToast) {
        this.mToast = mToast;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String titleOrderBy = getPreference();
        if ("popular".equals(titleOrderBy)) {
            setTitle(getString(R.string.main_activity_title_most_popular));
        } else if ("top_rated".equals(titleOrderBy)) {
            setTitle(getString(R.string.main_activity_title_top_rated));
        } else {
            setTitle(getString(R.string.main_activity_title_favorite));
        }

        /**
         * Using findViewById, get a reference to the RecyclerView from xml.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movieposters);

        /**
         * This TextView is used to display errors and will be hidden if there are no errors.
         */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);

        /**
         * this: Current context, will be used to access resources.
         * 4: The number of columns in the grid
         * GridLayoutManager.VERTICAL: Layout orientation.
         * false: When set to true, layouts from end to start.
         */
        final GridLayoutManager layoutManager
                = new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        /**
         * Use this setting to improve performance that changes in content do not change the child
         * layout size in the RecyclerView.
         */
        mRecyclerView.setHasFixedSize(true);

        /**
         * The MovieAdapter is responsible for linking the movie posters data with the Views that
         * will end up displaying the posters data.
         */
        if (mMovieAdapter == null) {
            mMovieAdapter = new MovieAdapter(this, this);
        }

        /**
         * Setting the adapter attaches it to the RecyclerView in the layout.
         */
        mRecyclerView.setAdapter(mMovieAdapter);

        /**
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {

                    @Override
                    public void onRefresh() {
                        refreshMovie();
                    }
                }
        );

        // Change swipeRefreshLayout 's loading indicator background color.
        int swipeRefreshBgColor = ContextCompat.getColor(this, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(swipeRefreshBgColor);

        // Change swipeRefreshLayout 's loading indicator loading circle color.
        // You can have as many as colors you want.
        mSwipeRefreshLayout.setColorSchemeResources(
                // if the loading is fast, it shows white from the beginning and finish
                R.color.colorWhiteFavoriteStar,
                // if the loading is slow, it shows different blue
                R.color.trailer10,
                R.color.trailer9,
                R.color.trailer8,
                R.color.trailer7,
                R.color.trailer6,
                R.color.trailer5,
                R.color.trailer4,
                R.color.trailer3,
                R.color.trailer2,
                R.color.trailer1,
                R.color.trailer0);


        // If there is a network connection, fetch data
        if (getNetworkInfo() != null && getNetworkInfo().isConnected()) {

            Calendar calendar = Calendar.getInstance();
            calendar.roll(Calendar.MINUTE, -10);
            Date tenMinAgoThisTime = calendar.getTime();

            String orderBy = getPreference();

            if (!"favorites".equals(orderBy)) {

                boolean refreshPop = false;
                boolean refreshTop = false;

                if ("popular".equals(orderBy)) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    Long default_time = new Date().getTime();
                    long mPopLatestRefreshed = preferences.getLong
                            (getString(R.string.pref_pop_date_key), default_time);

                    if (mPopLatestRefreshed == default_time) {
                        refreshPop = true;
                    } else {
                        if (new Date(mPopLatestRefreshed).before(tenMinAgoThisTime)) {
                            refreshPop = true;
                        }
                    }
                } else {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    Long default_time = new Date().getTime();
                    long mTopLatestRefreshed = preferences.getLong
                            (getString(R.string.pref_top_date_key), default_time);

                    if (mTopLatestRefreshed == default_time) {
                        refreshTop = true;
                    } else {
                        if (new Date(mTopLatestRefreshed).before(tenMinAgoThisTime)) {
                            refreshTop = true;
                        }
                    }
                }

                if (refreshPop || refreshTop) {
                    orderBy = "movie/" + orderBy;
                    new FetchMoviePostersTask(this).execute(orderBy);
                    new PersistMovieTask(this).execute(orderBy);

                    if (refreshPop) {
                        Log.i(TAG, getString(R.string.log_information_refreshPop_true));
                    }
                    if (refreshTop) {
                        Log.i(TAG, getString(R.string.log_information_refreshTop_true));
                    }
                } else {
                    initCursorLoader();
                }
            } else {
                // if selected "fav"
                // if has network
                persistFavMovie();
            }
        } else {
            hideLoadingIndicators();
            initCursorLoader();
        }
    }

    private void persistFavMovie() {
        /*
        download pictures for favorite movies.

        First reason: When we save the movie to fav list database, the 2 pics might not be downloaded
        successfully yet.
        And it will stay break, because we don't refresh fav list again(only call from database for
        fav list).
        (What we are doing now is giving fav list the opportunity to refresh the its pictures' external
        url.)

        Second reason (IMPORTANT): Every time we receive new movie data, we delete the cache tables
        (both POP and TOP) and their external folders completely. But one old movie we have saved to
        fav list before is still stay in our fav list, when it wants to fetch its external url, the
        cache movie folders is already cleaned for new data. There is no way to find it.
        We have to create folders for fav list to maintain its own data.
        */

        downloadExtraFavMoviePosterFilePic();
        downloadExtraFavMovieImageThumbnailFilePic();

        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(getString(R.string.pref_fav_date_key), currentTime.getTime());
        editor.apply();

        initCursorLoader();

        deleteExtraFavMoviePosterFilePic();
        deleteExtraFavMovieImageThumbnailFilePic();
    }

    private void downloadExtraFavMoviePosterFilePic() {

        File favMoviePicsFolder
                = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + "/favmovies/");

        if (favMoviePicsFolder.exists()) {

            String[] fileNameArray = new String[favMoviePicsFolder.listFiles().length];
            Log.i(TAG, "download / filepath: fav poster file name count in external folder: " + favMoviePicsFolder.listFiles().length);
            int j = 0;
            for (File pic : favMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                fileNameArray[j] = fileName;
                Log.i(TAG, "download / filepath: fav poster file name in external folder: " + fileNameArray[j]);
                j++;
            }

            String[] projection = {FavMovieEntry.COLUMN_MOVIE_ID, FavMovieEntry.COLUMN_POSTER_PATH};
            Cursor cursor = getContentResolver().query(
                    FavMovieEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String currentPosterPath = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_POSTER_PATH));
                String currentMovieId = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_ID));
                if (!Arrays.asList(fileNameArray).contains(currentPosterPath)) {
                    Log.i(TAG, "download / filepath: download fav external poster pic:" + currentPosterPath);
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                            .concat(currentPosterPath);
                    new FetchExternalStorageFavMoviePosterImagesTask(this).execute(
                            new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                }
                cursor.moveToNext();
            }
            cursor.close();
        } else {
            String[] projection = {FavMovieEntry.COLUMN_MOVIE_ID, FavMovieEntry.COLUMN_POSTER_PATH};
            Cursor cursor = getContentResolver().query(
                    FavMovieEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String currentPosterPath = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_POSTER_PATH));
                String currentMovieId = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_ID));
                Log.i(TAG, "download / filepath: download fav external poster pic:" + currentPosterPath);
                String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W185)
                        .concat(currentPosterPath);
                new FetchExternalStorageFavMoviePosterImagesTask(this).execute(
                        new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    private void downloadExtraFavMovieImageThumbnailFilePic() {

        File favMovieThumbnailsFolder
                = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + "/favthumbnails/");

        if (favMovieThumbnailsFolder.exists()) {

            String[] fileNameArray = new String[favMovieThumbnailsFolder.listFiles().length];
            Log.i(TAG, "download / filepath: fav image thumbnail file name count in external folder: " + favMovieThumbnailsFolder.listFiles().length);
            int j = 0;
            for (File pic : favMovieThumbnailsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                fileNameArray[j] = fileName;
                Log.i(TAG, "download / filepath: fav image thumbnail file name in external folder: " + fileNameArray[j]);
                j++;
            }

            String[] projection = {FavMovieEntry.COLUMN_MOVIE_ID, FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
            Cursor cursor = getContentResolver().query(
                    FavMovieEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String currentImageThumbnail = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                String currentMovieId = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_ID));
                if (!Arrays.asList(fileNameArray).contains(currentImageThumbnail)) {
                    Log.i(TAG, "download / filepath: download fav external image thumbnail pic:" + currentImageThumbnail);
                    String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                            .concat(currentImageThumbnail);
                    new FetchExternalStorageFavMovieImageThumbnailsTask(this).execute(
                            new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                }
                cursor.moveToNext();
            }
            cursor.close();
        } else {
            String[] projection = {FavMovieEntry.COLUMN_MOVIE_ID, FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
            Cursor cursor = getContentResolver().query(
                    FavMovieEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String currentImageThumbnail = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
                String currentMovieId = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_ID));
                Log.i(TAG, "download / filepath: download fav external image thumbnail pic:" + currentImageThumbnail);
                String fullMoviePosterForOneMovie = BASE_IMAGE_URL.concat(IMAGE_SIZE_W780)
                        .concat(currentImageThumbnail);
                new FetchExternalStorageFavMovieImageThumbnailsTask(this).execute(
                        new MovieBasicInfo(currentMovieId, fullMoviePosterForOneMovie));
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    private void deleteExtraFavMoviePosterFilePic() {
        String[] projection = {FavMovieEntry.COLUMN_POSTER_PATH};
        Cursor cursor = getContentResolver().query(
                FavMovieEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        String[] posterPathArray = new String[cursor.getCount()];
        int i = 0;

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            posterPathArray[i] = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_POSTER_PATH));
            Log.i(TAG, "delete / filepath: fav poster path in database: " + posterPathArray[i]);
            i++;
            cursor.moveToNext();
        }
        cursor.close();

        File favMoviePicsFolder
                = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + "/favmovies/");
        if (favMoviePicsFolder.exists()) {
            for (File pic : favMoviePicsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(posterPathArray).contains(fileName)) {
                    Log.i(TAG, "delete / filepath: delete fav external poster pic:" + fileName);
                    pic.delete();
                }
            }
        }
    }

    private void deleteExtraFavMovieImageThumbnailFilePic() {
        String[] projection = {FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL};
        Cursor cursor = getContentResolver().query(
                FavMovieEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        String[] imageThumbnailArray = new String[cursor.getCount()];
        int i = 0;

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            imageThumbnailArray[i] = cursor.getString(cursor.getColumnIndex(FavMovieEntry.COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL));
            Log.i(TAG, "delete / filepath: fav image thumbnail path in database: " + imageThumbnailArray[i]);
            i++;
            cursor.moveToNext();
        }
        cursor.close();

        File favMovieThumbnailsFolder
                = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath()
                + "/favthumbnails/");
        if (favMovieThumbnailsFolder.exists()) {
            for (File pic : favMovieThumbnailsFolder.listFiles()) {
                String fileName = "/" + pic.getName();
                if (!Arrays.asList(imageThumbnailArray).contains(fileName)) {
                    Log.i(TAG, "delete / filepath: delete fav external thumbnail pic:" + fileName);
                    pic.delete();
                }
            }
        }
    }

    private void refreshMovie() {
        if (getNetworkInfo() != null && getNetworkInfo().isConnected()) {

            Calendar calendar = Calendar.getInstance();
            calendar.roll(Calendar.MINUTE, -1);
            Date oneMinuteAgo = calendar.getTime();

            String orderBy = getPreference();

            if (!"favorites".equals(orderBy)) {

                boolean refreshPop = false;
                boolean refreshTop = false;

                if ("popular".equals(orderBy)) {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    Long default_time = new Date().getTime();
                    long mPopLatestRefreshed = preferences.getLong
                            (getString(R.string.pref_pop_date_key), default_time);

                    if (mPopLatestRefreshed == default_time) {
                        refreshPop = true;
                    } else {
                        if (new Date(mPopLatestRefreshed).before(oneMinuteAgo)) {
                            refreshPop = true;
                        } else {
                            if (mToast != null) {
                                mToast.cancel();
                            }
                            mToast = Toast.makeText(MainActivity.this, getString(R.string.toast_message_refresh_pop_in_one_minute), Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.BOTTOM, 0, 0);
                            mToast.show();
                        }
                    }
                } else {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    Long default_time = new Date().getTime();
                    long mTopLatestRefreshed = preferences.getLong
                            (getString(R.string.pref_top_date_key), default_time);

                    if (mTopLatestRefreshed == default_time) {
                        refreshTop = true;
                    } else {
                        if (new Date(mTopLatestRefreshed).before(oneMinuteAgo)) {
                            refreshTop = true;
                        } else {
                            if (mToast != null) {
                                mToast.cancel();
                            }
                            mToast = Toast.makeText(MainActivity.this, getString(R.string.toast_message_refresh_top_in_one_minute), Toast.LENGTH_SHORT);
                            mToast.setGravity(Gravity.BOTTOM, 0, 0);
                            mToast.show();
                        }
                    }
                }

                if (refreshPop || refreshTop) {
                    orderBy = "movie/" + orderBy;
                    new FetchMoviePostersTask(MainActivity.this).execute(orderBy);
                    new PersistMovieTask(MainActivity.this).execute(orderBy);

                    if (refreshPop) {
                        Log.i(TAG, getString(R.string.log_information_refreshPop_true));
                    }
                    if (refreshTop) {
                        Log.i(TAG, getString(R.string.log_information_refreshTop_true));
                    }
                } else {
                    initCursorLoader();
                }
            } else {
                boolean refreshFav = false;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                Long default_time = new Date().getTime();
                long mFavLatestRefreshed = preferences.getLong
                        (getString(R.string.pref_fav_date_key), default_time);

                if (mFavLatestRefreshed == default_time) {
                    refreshFav = true;
                } else {
                    if (new Date(mFavLatestRefreshed).before(oneMinuteAgo)) {
                        refreshFav = true;
                    } else {
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        mToast = Toast.makeText(MainActivity.this, getString(R.string.toast_message_refresh_fav_in_one_minute), Toast.LENGTH_SHORT);
                        mToast.setGravity(Gravity.BOTTOM, 0, 0);
                        mToast.show();
                    }
                }
                if (refreshFav) {
                    persistFavMovie();
                } else {
                    initCursorLoader();
                }
            }
        } else {
            hideLoadingIndicators();
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(MainActivity.this, getString(R.string.toast_message_refresh_no_internet), Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.BOTTOM, 0, 0);
            mToast.show();
        }
    }

    public void initCursorLoader() {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    /**
     * This method is overridden by the MainActivity class in order to handle RecyclerView item
     * clicks.
     */
    @Override
    public void onClick(FullMovie movie) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("movie", movie);
        startActivity(intentToStartDetailActivity);
    }

    /**
     * This method will make the View for the movie data visible and
     * hide the error message
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    public void showMovieDataView() {
        // First, make sure the error is invisible.
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        // Then, make sure the movie data is visible.
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movie data View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    public void showErrorMessage() {
        // First, hide the currently visible data.
        mRecyclerView.setVisibility(View.INVISIBLE);
        // Then, show the error.
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem deleteAll = menu.findItem(R.id.action_delete_all);
        String orderBy = getPreference();
        if ("favorites".equals(orderBy)) {
            deleteAll.setVisible(true);
        } else {
            deleteAll.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // Have a refresh menu button to perform the refresh again, in case some device or some
            // users cannot perform swipe to refresh.
            case R.id.action_refresh:
                mLoadingIndicator.setVisibility(View.VISIBLE);
                refreshMovie();
                return true;
            case R.id.action_settings:
                Intent settingIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingIntent);
                return true;
            case R.id.action_delete_all:
                showDeleteConfirmationDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAllMovies() {

        int rowsDeletedFavMovie = getContentResolver().delete(FavMovieEntry.CONTENT_URI, null, null);
        int rowsDeletedReview = getContentResolver().delete(ReviewEntry.CONTENT_URI, null, null);
        int rowsDeletedTrailer = getContentResolver().delete(TrailerEntry.CONTENT_URI, null, null);

        if (rowsDeletedFavMovie == 0) {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(this, getString(R.string.delete_all_movie_failed), Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.BOTTOM, 0, 0);
            mToast.show();
        } else {
            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(this, getString(R.string.delete_all_movie_successful), Toast.LENGTH_SHORT);
            mToast.setGravity(Gravity.BOTTOM, 0, 0);
            mToast.show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String orderBy = getPreference();

        if ("popular".equals(orderBy)) {
            return new CursorLoader(
                    this,
                    CacheMovieMostPopularEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        } else if ("top_rated".equals(orderBy)) {
            return new CursorLoader(
                    this,
                    CacheMovieTopRatedEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        } else {
            return new CursorLoader(
                    this,
                    FavMovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor != null && cursor.getCount() > 0) {
            hideLoadingIndicators();
            showMovieDataView();
            mMovieAdapter.swapCursor(cursor);

        } else {

            supportStartPostponedEnterTransition();
            hideLoadingIndicators();
            showErrorMessage();

            String orderBy = getPreference();

            if ("popular".equals(orderBy)) {
                // Because we only fetch data when there is network, if there is no network, we load
                // movie data from database. so this message will only happen when users open the app
                // for the first time without network (no data in database).
                // We will display "Please check your network connection."
                mErrorMessageDisplay.setText(getString(R.string.error_message_no_popular_movie));
            } else if ("top_rated".equals(orderBy)) {
                // Because we only fetch data when there is network, if there is no network, we load
                // movie data from database. so this message will only happen when users open the app
                // for the first time without network (no data in database).
                // We will display "Please check your network connection."
                mErrorMessageDisplay.setText(getString(R.string.error_message_no_top_rated_movie));
            } else {
                mErrorMessageDisplay.setText(getString(R.string.error_message_no_fav_movie));
            }
        }
    }

    private void hideLoadingIndicators() {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        // this setRefreshing method is controlling the visible or invisible of the loading
        // indicator of the swipeRefreshlayout
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieAdapter.swapCursor(null);

    }

    public void restartLoader() {
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @NonNull
    private String getPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
    }

    private NetworkInfo getNetworkInfo() {
        // Get a reference to the ConnectivityManager to check state of network connectivity.
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        return connMgr.getActiveNetworkInfo();
    }

    /**
     * Prompt the user to confirm that they want to delete all movies.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all movies.
                deleteAllMovies();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}

package se.sugarest.jane.popularmovies.data;

/**
 * Created by jane on 17-4-11.
 */

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines tables and column names for the movie database.
 * It contains MovieEntry, ReviewEntry and TrailerEntry.
 */
public class MovieContract {

    /*
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website. A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * Play Store.
     */
    public static final String CONTENT_AUTHORITY = "se.sugarest.jane.popularmovies";

    /*
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider for Popularmovies.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*
     * Possible paths that can be appended to BASE_CONTENT_URI to form valid URI's that Popularmovies
     * can handle. For instance,
     *
     *     content://se.sugarest.jane.popularmovies/movie/
     *     [           BASE_CONTENT_URI          ][ PATH_MOVIE ]
     *
     * is a valid path for looking at movie data.
     *
     *      content://se.sugarest.jane.popularmovies/givemeroot/
     *
     * will fail, as the ContentProvider hasn't been given any information on what to do with
     * "givemeroot".
     */
    public static final String PATH_MOVIE = "movie";

    public static final String PATH_REVIEW = "review";

    public static final String PATH_TRAILER = "trailer";

    /**
     * Inner class that defines the table contents of the movie table.
     */
    public static final class MovieEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Movie table from the content provider */
        public static final Uri CONTENT_URI_MOVIE = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE)
                .build();

        /* Used internally as the name of our movie table. */
        public static final String TABLE_NAME = "movie";

        /**
         * PosterPath is stored as a String representing movie's poster_path url, used for
         * display on main screen when user setting sortOrder by favorite.
         */
        public static final String COLUMN_POSTER_PATH = "poster_path";

        /**
         * OriginalTitle is stored as a String representing movie's original title.
         */
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";

        /**
         * MoviePosterImageThumbnail is stored as a String representing movie's poster_image_thumbnail
         * url, used for display on detail screen.
         */
        public static final String COLUMN_MOVIE_POSTER_IMAGE_THUMBNAIL
                = "movie_poster_image_thumbnail";

        /**
         * APlotSynopsis is stored as a String representing movie's plot synopsis.
         */
        public static final String COLUMN_A_PLOT_SYNOPSIS = "a_plot_synopsis";

        /**
         * UserRating is stored as a String representing movie's average rating.
         */
        public static final String COLUMN_USER_RATING = "user_rating";

        /**
         * ReleaseDate is stored as a String representing movie's release date.
         */
        public static final String COLUMN_RELEASE_DATE = "release_date";

        /**
         * MovieId is stored as a String representing movie's id, used to identify the movie.
         */
        public static final String COLUMN_MOVIE_ID = "movie_id";
    }

    /**
     * Inner class that defines the table contents of the review table.
     */
    public static final class ReviewEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Review table from the content provider */
        public static final Uri CONTENT_URI_REVIEW = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_REVIEW)
                .build();

        /* Used internally as the name of our review table. */
        public static final String TABLE_NAME = "review";

        /**
         * MovieIdReview is stored as a String representing movie's id, used to identify the movie.
         */
        public static final String COLUMN_MOVIE_ID_REVIEW = "movie_id_review";

        /**
         * Author is stored as a String representing review's author.
         */
        public static final String COLUMN_AUTHOR = "author";

        public static final String COLUMN_REVIEW_CONTENT = "review_content";

    }

    /**
     * Inner class that defines the table contents of the trailer table.
     */
    public static final class TrailerEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the Trailer table from the content provider */
        public static final Uri CONTENT_URI_REVIEW = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TRAILER)
                .build();

        /* Used internally as the name of our trailer table. */
        public static final String TABLE_NAME = "trailer";

        /**
         * MovieIdTrailer is stored as a String representing movie's id, used to identify the movie.
         */
        public static final String COLUMN_MOVIE_ID_TRAILER = "movie_id_trailer";

        /**
         * KeyOfTrailer is stored as a String representing youtube url to play the trailer either on
         * Youtube App or on a Web Browser.
         */
        public static final String COLUMN_KEY_OF_TRAILER = "key_of_trailer";

    }
}

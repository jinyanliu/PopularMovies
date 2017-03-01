package se.sugarest.jane.popularmovies;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by jane on 3/1/17.
 */

public class DetailActivity extends AppCompatActivity {

    private ImageView mMoviePosterImageThumbnailImageView;

    private TextView mOriginalTitleTextView;

    private TextView mUserRatingTextView;

    private TextView mReleaseDateTextView;

    private TextView mAPlotSynopsis;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mMoviePosterImageThumbnailImageView = (ImageView) findViewById(R.id.iv_movie_poster_image_thumbnail);
        mOriginalTitleTextView = (TextView) findViewById(R.id.tv_original_title);
        mUserRatingTextView = (TextView) findViewById(R.id.tv_user_rating);
        mReleaseDateTextView = (TextView) findViewById(R.id.tv_release_date);
        mAPlotSynopsis = (TextView) findViewById(R.id.tv_a_plot_synopsis);
    }
}

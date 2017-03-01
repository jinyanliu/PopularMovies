package se.sugarest.jane.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by jane on 3/1/17.
 */

public class DetailActivity extends AppCompatActivity {

    private ImageView mMoviePosterImageThumbnailImageView;

    private TextView mOriginalTitleTextView;

    private TextView mUserRatingTextView;

    private TextView mReleaseDateTextView;

    private TextView mAPlotSynopsisTextView;

    private Movie mCurrentMovie;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mMoviePosterImageThumbnailImageView = (ImageView) findViewById(R.id.iv_movie_poster_image_thumbnail);
        mOriginalTitleTextView = (TextView) findViewById(R.id.tv_original_title);
        mUserRatingTextView = (TextView) findViewById(R.id.tv_user_rating);
        mReleaseDateTextView = (TextView) findViewById(R.id.tv_release_date);
        mAPlotSynopsisTextView = (TextView) findViewById(R.id.tv_a_plot_synopsis);

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("movie")) {
                mCurrentMovie = (Movie) getIntent().getExtras().getSerializable("movie");
            }
        }

        Picasso.with(DetailActivity.this).load(mCurrentMovie.getMoviePosterImageThumbnail())
                .into(mMoviePosterImageThumbnailImageView);
        mOriginalTitleTextView.setText(mCurrentMovie.getOriginalTitle());
        mUserRatingTextView.setText(mCurrentMovie.getUserRating());
        mReleaseDateTextView.setText(mCurrentMovie.getReleaseDate());
        mAPlotSynopsisTextView.setText(mCurrentMovie.getAPlotSynopsis());

    }
}

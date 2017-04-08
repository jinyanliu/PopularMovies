package se.sugarest.jane.popularmovies;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.squareup.picasso.Picasso;

import se.sugarest.jane.popularmovies.databinding.ActivityDetailBinding;

/**
 * Created by jane on 3/1/17.
 */

public class DetailActivity extends AppCompatActivity {

    private Movie mCurrentMovie;

    /**
     * This field is used for data binding. Normally, we would have to call findViewById many
     * times to get references to the Views in this Activity. With data binding however, we only
     * need to call DateBindingUtil.setContentView and pass in a Context and a layout, as we do
     * in onCreate of this class. Then, we can access all of the Views in our layout
     * programmatically without cluttering up the code with findViewById.
     */
    private ActivityDetailBinding mDetailBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_detail);

        // Setup FAB to add favorite movies into database and change FAB color to yellow
        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int yellowColorValue = Color.parseColor("#FFEB3B");
                fab.setColorFilter(yellowColorValue);
            }
        });

        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity != null) {
            if (intentThatStartedThisActivity.hasExtra("movie")) {
                mCurrentMovie = (Movie) getIntent().getExtras().getSerializable("movie");
            }
        }

        // Set current movie poster image thumbnail
        Picasso.with(DetailActivity.this).load(mCurrentMovie.getMoviePosterImageThumbnail())
                .into(mDetailBinding.ivMoviePosterImageThumbnail);

        // Set current movie textViews content
        mDetailBinding.tvOriginalTitle.setText(mCurrentMovie.getOriginalTitle());
        mDetailBinding.tvUserRating.setText(mCurrentMovie.getUserRating());
        mDetailBinding.tvReleaseDate.setText(mCurrentMovie.getReleaseDate());
        mDetailBinding.tvAPlotSynopsis.setText(mCurrentMovie.getAPlotSynopsis());

    }
}

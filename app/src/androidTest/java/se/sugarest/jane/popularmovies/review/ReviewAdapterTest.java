package se.sugarest.jane.popularmovies.review;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jane on 17-8-16.
 */

public class ReviewAdapterTest {

    List<Review> reviewData = new ArrayList<>();

    ReviewAdapter reviewAdapter = new ReviewAdapter();

    /***********************************************************************************************
     *                                                                                             *
     * reviewData == null happens when reviewData hasn't been loaded yet, it won't                 *
     * trigger reviewAdapter.setReviewData(reviewData) method.                                     *
     * The getItemCountTest_noReviewData() method is testing the situation that                    *
     * reviewData != null but reviewData.size == 0,                                                *
     * which means the reviewData has already been loaded, but the movie doesn't have reviews.     *
     * So we don't initialize reviewData, just pass it to setReviewData(reviewData),               *
     * meaning no reviews available.                                                               *
     *                                                                                             *
     ***********************************************************************************************/

    @Test
    public void getItemCountTest_nullReviewData() throws Exception {
        // reviewData == null;
        Assert.assertEquals(0, reviewAdapter.getItemCount());
    }

    @Test
    public void getItemCountTest_noReviewData() throws Exception {
        reviewAdapter.setReviewData(reviewData);
        Assert.assertEquals(0, reviewAdapter.getItemCount());
    }

    @Test
    public void getItemCountTest_oneReview() throws Exception {
        String oneReviewAuthor = "Salt-and-Limes";
        String oneReviewContent = "It is good.";
        Review review = new Review(oneReviewAuthor, oneReviewContent);
        reviewData.add(review);
        reviewAdapter.setReviewData(reviewData);
        Assert.assertEquals(1, reviewAdapter.getItemCount());
    }

    @Test
    public void getItemCountTest_twoReview() throws Exception {
        String oneReviewAuthor = "Salt-and-Limes";
        String oneReviewContent = "It is good.";
        String secondReviewAuthor = "Gimly";
        String secondReviewContent = "It is fun.";
        Review reviewOne = new Review(oneReviewAuthor, oneReviewContent);
        Review reviewTwo = new Review(secondReviewAuthor, secondReviewContent);
        reviewData.add(reviewOne);
        reviewData.add(reviewTwo);
        reviewAdapter.setReviewData(reviewData);
        Assert.assertEquals(2, reviewAdapter.getItemCount());
    }
}
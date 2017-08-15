package se.sugarest.jane.popularmovies.review;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Created by jane on 17-8-15.
 */
public class ReviewTest {

    private String author = "Gimly";
    private String content = "Disney's done a great job with Gaston and The Beast, the two aspects that I always thought would be the most important, and the most difficult, to nail.\\r\\n\\r\\n_Final rating:★★★ - I personally recommend you give it a go._";

    private Review review;

    @Before
    public void setUp() {
        this.review = new Review(author, content);
    }

    @Test
    public void getAuthorTest() throws Exception {
        Assert.assertEquals(author, review.getAuthor());

    }

    @Test
    public void getReviewContentTest() throws Exception {
        Assert.assertEquals(content, review.getReviewContent());
    }

}
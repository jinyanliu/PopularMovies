package se.sugarest.jane.popularmovies.utilities;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by jane on 17-8-15.
 */
public class ReviewJsonUtilsAndroidTest {

    @Test
    public void testExtractResultsFromMovieReviewJson_emptyJson() {
        String emptyJson = "{}";
        Assert.assertTrue(ReviewJsonUtils.extractResultsFromMovieReviewJson(emptyJson).isEmpty());
    }

    @Test
    public void testExtractResultsFromMovieReviewJson_noTrailer() {
        String noReviewJson = "{\n" +
                "\"id\": 321612,\n" +
                "\"page\": 1,\n" +
                "\"results\": [\n" +
                "]\n" +
                "}";
        Assert.assertTrue(ReviewJsonUtils.extractResultsFromMovieReviewJson(noReviewJson).isEmpty());
    }

    @Test
    public void testExtractResultsFromMovieReviewJson_invalidJson() {
        String invalidJson = "{\n" +
                "\"id\": 321612,\n" +
                "\"page\": 1,\n" +
                "\"results\": [\n" +
                //"{\n" +
                "\"id\": \"58d04679c3a3682dcd0002c6\",\n" +
                "\"author\": \"Salt-and-Limes\",\n" +
                "\"content\": \"It is good.\",\n" +
                "\"url\": \"https://www.themoviedb.org/review/58d04679c3a3682dcd0002c6\"\n" +
                "}\n" +
                "]\n" +
                "}";
        Assert.assertTrue(ReviewJsonUtils.extractResultsFromMovieReviewJson(invalidJson).isEmpty());
    }

    @Test
    public void testExtractResultsFromMovieReviewJson_oneReview() {
        String oneReviewJson = "{\n" +
                "\"id\": 321612,\n" +
                "\"page\": 1,\n" +
                "\"results\": [\n" +
                "{\n" +
                "\"id\": \"58d04679c3a3682dcd0002c6\",\n" +
                "\"author\": \"Salt-and-Limes\",\n" +
                "\"content\": \"It is good.\",\n" +
                "\"url\": \"https://www.themoviedb.org/review/58d04679c3a3682dcd0002c6\"\n" +
                "}\n" +
                "]\n" +
                "}";
        Assert.assertTrue(ReviewJsonUtils.extractResultsFromMovieReviewJson(oneReviewJson).size() == 1);
        Assert.assertEquals("Salt-and-Limes",
                ReviewJsonUtils.extractResultsFromMovieReviewJson(oneReviewJson).get(0).getAuthor());
        Assert.assertEquals("It is good.",
                ReviewJsonUtils.extractResultsFromMovieReviewJson(oneReviewJson).get(0).getReviewContent());
    }

    @Test
    public void testExtractResultsFromMovieReviewJson_twoReviews() {
        String twoReviewJson = "{\n" +
                "\"id\": 321612,\n" +
                "\"page\": 1,\n" +
                "\"results\": [\n" +
                "{\n" +
                "\"id\": \"58d04679c3a3682dcd0002c6\",\n" +
                "\"author\": \"Salt-and-Limes\",\n" +
                "\"content\": \"It is good.\",\n" +
                "\"url\": \"https://www.themoviedb.org/review/58d04679c3a3682dcd0002c6\"\n" +
                "},\n" +
                "{\n" +
                "\"id\": \"58e3b31892514127f6020406\",\n" +
                "\"author\": \"Gimly\",\n" +
                "\"content\": \"It is fun.\",\n" +
                "\"url\": \"https://www.themoviedb.org/review/58e3b31892514127f6020406\"\n" +
                "}\n" +
                "]\n" +
                "}";
        Assert.assertTrue(ReviewJsonUtils.extractResultsFromMovieReviewJson(twoReviewJson).size() == 2);
        Assert.assertEquals("Salt-and-Limes",
                ReviewJsonUtils.extractResultsFromMovieReviewJson(twoReviewJson).get(0).getAuthor());
        Assert.assertEquals("It is good.",
                ReviewJsonUtils.extractResultsFromMovieReviewJson(twoReviewJson).get(0).getReviewContent());
        Assert.assertEquals("Gimly",
                ReviewJsonUtils.extractResultsFromMovieReviewJson(twoReviewJson).get(1).getAuthor());
        Assert.assertEquals("It is fun.",
                ReviewJsonUtils.extractResultsFromMovieReviewJson(twoReviewJson).get(1).getReviewContent());
    }

}
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
                "\"content\": \"**Spoilers**\\r\\n\\r\\nThe live action remake of \\\"Beauty and the Beast\\\" was good, but it failed to capture the magic of the cartoon version. There were somethings that they got right, and others that dragged on.\\r\\n\\r\\nI thought \\\"Be Our Guest\\\" was done beautifully. The 3d made it even more enchanting. The main characters' backstories also added some depth to them. However, there were some scenes that I felt added nothing to the story. Such as the search for Belle by Gaston and her father. The \\\"No one is like Gaston\\\" scene didn't have the bravado or arrogance of the original.\\r\\n\\r\\nI also felt that Luke Evans was miscast. He wasn't the handsomest guy in town, nor was he the strongest. Which is why it was hard for me to accept him as the character. Emma Watson was serviceable. Her voice was fine, but it wasn't strong enough to carry Belle's songs. Dan Stevens was the best part of the film. I felt that he should have had more songs, because he has a beautiful baritone. Although his beast costume should have been more frightening. \\r\\n\\r\\nOverall, it's a fun film to watch. Though, I wouldn't call it a classic.\",\n" +
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
                "\"content\": \"**Spoilers**\\r\\n\\r\\nThe live action remake of \\\"Beauty and the Beast\\\" was good, but it failed to capture the magic of the cartoon version. There were somethings that they got right, and others that dragged on.\\r\\n\\r\\nI thought \\\"Be Our Guest\\\" was done beautifully. The 3d made it even more enchanting. The main characters' backstories also added some depth to them. However, there were some scenes that I felt added nothing to the story. Such as the search for Belle by Gaston and her father. The \\\"No one is like Gaston\\\" scene didn't have the bravado or arrogance of the original.\\r\\n\\r\\nI also felt that Luke Evans was miscast. He wasn't the handsomest guy in town, nor was he the strongest. Which is why it was hard for me to accept him as the character. Emma Watson was serviceable. Her voice was fine, but it wasn't strong enough to carry Belle's songs. Dan Stevens was the best part of the film. I felt that he should have had more songs, because he has a beautiful baritone. Although his beast costume should have been more frightening. \\r\\n\\r\\nOverall, it's a fun film to watch. Though, I wouldn't call it a classic.\",\n" +
                "\"url\": \"https://www.themoviedb.org/review/58d04679c3a3682dcd0002c6\"\n" +
                "}\n" +
                "]\n" +
                "}";
        Assert.assertTrue(ReviewJsonUtils.extractResultsFromMovieReviewJson(oneReviewJson).size() == 1);
        Assert.assertEquals("Salt-and-Limes",
                ReviewJsonUtils.extractResultsFromMovieReviewJson(oneReviewJson).get(0).getAuthor());
        Assert.assertEquals("**Spoilers**\r\n\r\nThe live action remake of \"Beauty and the Beast\" was good, but it failed to capture the magic of the cartoon version. There were somethings that they got right, and others that dragged on.\r\n\r\nI thought \"Be Our Guest\" was done beautifully. The 3d made it even more enchanting. The main characters' backstories also added some depth to them. However, there were some scenes that I felt added nothing to the story. Such as the search for Belle by Gaston and her father. The \"No one is like Gaston\" scene didn't have the bravado or arrogance of the original.\r\n\r\nI also felt that Luke Evans was miscast. He wasn't the handsomest guy in town, nor was he the strongest. Which is why it was hard for me to accept him as the character. Emma Watson was serviceable. Her voice was fine, but it wasn't strong enough to carry Belle's songs. Dan Stevens was the best part of the film. I felt that he should have had more songs, because he has a beautiful baritone. Although his beast costume should have been more frightening. \r\n\r\nOverall, it's a fun film to watch. Though, I wouldn't call it a classic.",
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
                "\"content\": \"**Spoilers**\\r\\n\\r\\nThe live action remake of \\\"Beauty and the Beast\\\" was good, but it failed to capture the magic of the cartoon version. There were somethings that they got right, and others that dragged on.\\r\\n\\r\\nI thought \\\"Be Our Guest\\\" was done beautifully. The 3d made it even more enchanting. The main characters' backstories also added some depth to them. However, there were some scenes that I felt added nothing to the story. Such as the search for Belle by Gaston and her father. The \\\"No one is like Gaston\\\" scene didn't have the bravado or arrogance of the original.\\r\\n\\r\\nI also felt that Luke Evans was miscast. He wasn't the handsomest guy in town, nor was he the strongest. Which is why it was hard for me to accept him as the character. Emma Watson was serviceable. Her voice was fine, but it wasn't strong enough to carry Belle's songs. Dan Stevens was the best part of the film. I felt that he should have had more songs, because he has a beautiful baritone. Although his beast costume should have been more frightening. \\r\\n\\r\\nOverall, it's a fun film to watch. Though, I wouldn't call it a classic.\",\n" +
                "\"url\": \"https://www.themoviedb.org/review/58d04679c3a3682dcd0002c6\"\n" +
                "},\n" +
                "{\n" +
                "\"id\": \"58e3b31892514127f6020406\",\n" +
                "\"author\": \"Gimly\",\n" +
                "\"content\": \"Disney's done a great job with Gaston and The Beast, the two aspects that I always thought would be the most important, and the most difficult, to nail.\\r\\n\\r\\n_Final rating:★★★ - I personally recommend you give it a go._\",\n" +
                "\"url\": \"https://www.themoviedb.org/review/58e3b31892514127f6020406\"\n" +
                "}\n" +
                "]\n" +
                "}";
        Assert.assertTrue(ReviewJsonUtils.extractResultsFromMovieReviewJson(twoReviewJson).size() == 2);
        Assert.assertEquals("Salt-and-Limes",
                ReviewJsonUtils.extractResultsFromMovieReviewJson(twoReviewJson).get(0).getAuthor());
        Assert.assertEquals("**Spoilers**\r\n\r\nThe live action remake of \"Beauty and the Beast\" was good, but it failed to capture the magic of the cartoon version. There were somethings that they got right, and others that dragged on.\r\n\r\nI thought \"Be Our Guest\" was done beautifully. The 3d made it even more enchanting. The main characters' backstories also added some depth to them. However, there were some scenes that I felt added nothing to the story. Such as the search for Belle by Gaston and her father. The \"No one is like Gaston\" scene didn't have the bravado or arrogance of the original.\r\n\r\nI also felt that Luke Evans was miscast. He wasn't the handsomest guy in town, nor was he the strongest. Which is why it was hard for me to accept him as the character. Emma Watson was serviceable. Her voice was fine, but it wasn't strong enough to carry Belle's songs. Dan Stevens was the best part of the film. I felt that he should have had more songs, because he has a beautiful baritone. Although his beast costume should have been more frightening. \r\n\r\nOverall, it's a fun film to watch. Though, I wouldn't call it a classic.",
                ReviewJsonUtils.extractResultsFromMovieReviewJson(twoReviewJson).get(0).getReviewContent());
        Assert.assertEquals("Gimly",
                ReviewJsonUtils.extractResultsFromMovieReviewJson(twoReviewJson).get(1).getAuthor());
        Assert.assertEquals("Disney's done a great job with Gaston and The Beast, the two aspects that I always thought would be the most important, and the most difficult, to nail.\r\n\r\n_Final rating:★★★ - I personally recommend you give it a go._",
                ReviewJsonUtils.extractResultsFromMovieReviewJson(twoReviewJson).get(1).getReviewContent());
    }

}
package data_access;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import data_access.UserRatingHandler;
import model.POIProfile;
import model.Preference;
import model.Rating;
import model.User;

public class UserRatingHandlerTest {
	private UserRatingHandler userDataHandler;
	private User user;
	private static final String RATING_PATH = "src/test/resources/ratings.csv";
	private static final String TEST_PATH = "src/test/resources/test.csv";
	private File testFile;

	@Before
	public void setUp() throws Exception {
		testFile = new File(TEST_PATH);
		this.userDataHandler = new UserRatingHandler(TEST_PATH);
		long id = 1001;
		POIProfile profile = new POIProfile(Preference.TRUE, Preference.FALSE, Preference.NOT_RATED,
				Preference.NOT_RATED, Preference.FALSE, Preference.NOT_RATED);
		user = new User(id, profile);
	}

	@After
	public void tearDown() throws Exception {
		testFile.delete();
	}

	@Test
	public void testSaveRating() throws IOException {
		// Prepare
		Files.copy(new File(RATING_PATH), testFile);
		long userId = user.getId();
		long itemId = 5555;
		Rating rating = Rating._2;
		long previousCount = 0;
		long currentCount = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(TEST_PATH))) {
			while (br.readLine() != null)
				previousCount++;
		}

		// Action
		userDataHandler.saveRating(userId, itemId, rating);
		
		// Check
		boolean containsString = false;
		String resultString = userId + "," + itemId +"," + rating.getValue();
		
		try (BufferedReader br = new BufferedReader(new FileReader(TEST_PATH))) {
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.equals(resultString)) {
					containsString = true;
				}
				currentCount++;
			}

			assertEquals(previousCount + 1, currentCount);
			assertTrue(containsString);
		}
	}

}

package touristbot;

import static org.junit.Assert.*;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

import data_access.UserDataHandler;
import model.POIProfile;
import model.Preference;
import model.Rating;
import model.User;

public class UserDataHandlerTest {
	private UserDataHandler userDataHandler;
	private User user;
	private static final String PROFILE_PATH = "src/test/resources/userProfile.csv";
	private static final String RATING_PATH = "src/test/resources/ratings.csv";
	private static final String TEST_PATH = "src/test/resources/test.csv";
	private File testFile;

	@Before
	public void setUp() throws Exception {
		testFile = new File(TEST_PATH);
		this.userDataHandler = new UserDataHandler(TEST_PATH, TEST_PATH);
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
	public void testAppendToFile() throws IOException {
		// Prepare
		Files.copy(new File(PROFILE_PATH), testFile);
		User newUser = new User(999, user.getProfile());
		long previousCount = 0;
		long currentCount = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(TEST_PATH))) {
			while (br.readLine() != null)
				previousCount++;
		}

		// Action
		userDataHandler.appendToProfile(newUser);

		// Check
		boolean containsString = false;
		String resultString = newUser.getId() + "," + newUser.getProfile();
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

	@Test
	public void testGetUser() throws IOException {
		// Prepare
		Files.copy(new File(PROFILE_PATH), testFile);
		
		// Action
		User resultUser = userDataHandler.getUserFromProfile(user.getId());

		// Check
		assertEquals(resultUser, user);
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

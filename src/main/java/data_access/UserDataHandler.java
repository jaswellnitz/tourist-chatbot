package data_access;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import model.POIProfile;
import model.Preference;
import model.Rating;
import model.User;

public class UserDataHandler {

	private final static String DEFAULT_PROFILE_PATH = "src/main/resources/userProfile.csv";
	private final static String DEFAULT_RATING_PATH = "src/main/resources/ratings.csv";
	private String profilePath;
	private String ratingPath;

	public UserDataHandler() {
		this(DEFAULT_PROFILE_PATH, DEFAULT_RATING_PATH);
	}

	public UserDataHandler(String profilePath, String ratingPath) {
		this.profilePath = profilePath;
		this.ratingPath = ratingPath;

	}
	
	public void saveRating(long userId, long itemId, Rating newRating) {
		String line = userId+","+itemId+","+newRating.getValue();
		appendToFile(ratingPath, line);
	}

	public void appendToProfile(User user){
		long id = user.getId();
		String line = id + "," + user.getProfile().toString();
		appendToFile(profilePath, line);
	}
	
	public User getUserFromProfile(long id) {
		String[] fields = null;
		try {
			fields = searchUserInProfile(id);
		} catch (IOException exception) {
			exception.printStackTrace();
		}

		Preference[] values = new Preference[fields.length - 1];
		for (int i = 1; i < fields.length; i++) {
			values[i - 1] = Preference.valueByFieldName(fields[i]);
		}
		POIProfile profile = new POIProfile(values[0], values[1], values[2], values[3], values[4], values[5]);
		User user = new User(id, profile);
		return user;
	}

	
	private void appendToFile(String path, String line){
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
			bw.write(line);
			bw.newLine();
		}
		catch(IOException exception){
			exception.printStackTrace();
		}
	}

	private String[] searchUserInProfile(long id) throws IOException {
		long foundId = -1;
		String[] fields = null;
		try (BufferedReader br = new BufferedReader(new FileReader(profilePath))) {
			do {
				String line = br.readLine();
				if (line == null) {
					throw new IOException("User not found.");
				}
				fields = line.split(",");
				foundId = Long.valueOf(fields[0]);
			} while (id != foundId);
		}
		return fields;
	}
}

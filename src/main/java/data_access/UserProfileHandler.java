package data_access;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import model.POIProfile;
import model.User;

public class UserProfileHandler {

	private final static String PATH_NAME = "src/main/resources/userProfile.csv";

	public UserProfileHandler() {
	}

	public void appendToFile(User user) throws IOException {
		long id = user.getId();
		String userString = id + ", " + user.getProfile().toString();
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(PATH_NAME))) {
			bw.write(userString + "\n");
		}
	}

	public User getUser(long id) throws IOException {
		String[] fields = searchUser(id);

		boolean[] values = new boolean[fields.length - 1];
		for (int i = 1; i < fields.length; i++) {
			values[i - 1] = fields[i].equals("1");
		}
		POIProfile profile = new POIProfile(values[0], values[1], values[2], values[3], values[4], values[5]);
		User user = new User(id, profile);
		return user;
	}

	private String[] searchUser(long id) throws IOException {
		long foundId = -1;
		String[] fields = null;
		try (BufferedReader br = new BufferedReader(new FileReader(PATH_NAME))) {
			do {
				String line = br.readLine();
				if (line == null) {
					throw new IOException("User not found.");
				}
				fields = line.split(",");
				foundId = Long.valueOf(fields[0]);
				System.out.println(foundId);
			} while (id != foundId);
		}
		return fields;
	}
}

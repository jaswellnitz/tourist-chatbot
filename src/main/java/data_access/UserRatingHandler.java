package data_access;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import model.Rating;

public class UserRatingHandler {

	private final static String DEFAULT_RATING_PATH = "src/main/resources/ratings.csv";
	private String ratingPath;

	public UserRatingHandler() {
		this(DEFAULT_RATING_PATH);
	}

	public UserRatingHandler(String ratingPath) {
		this.ratingPath = ratingPath;

	}
	
	public void saveRating(long userId, long itemId, Rating newRating) {
		String line = userId+","+itemId+","+newRating.getValue();
		appendToFile(ratingPath, line);
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
}

package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import model.Rating;

public class UserRatingHandler {

	private String ratingPath;

	@Deprecated
	public UserRatingHandler(String ratingPath) {
		this.ratingPath = ratingPath;

	}

	public void deleteAllUserRatings(long userId) {
		Path path = Paths.get(ratingPath);
		List<String> fileContent;
		try {
			fileContent = new ArrayList<>(Files.readAllLines(Paths.get(ratingPath), StandardCharsets.UTF_8));
			List<String> entries = new ArrayList<>();
			for (int i = 0; i < fileContent.size(); i++) {
				String[] splitLine = fileContent.get(i).split(",");
				long uid = Long.valueOf(splitLine[0]);
				if (uid == userId) {
					entries.add(fileContent.get(i));
				}
			}
			fileContent.removeAll(entries);
			Files.write(path, fileContent, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateRating(long userId, long itemId, Rating newRating) {
		Path path = Paths.get(ratingPath);
		List<String> fileContent;
		try {
			fileContent = new ArrayList<>(Files.readAllLines(Paths.get(ratingPath), StandardCharsets.UTF_8));
			for (int i = 0; i < fileContent.size(); i++) {
				String[] splitLine = fileContent.get(i).split(",");
				long uid = Long.valueOf(splitLine[0]);
				long iid = Long.valueOf(splitLine[1]);
				if (uid == userId && iid == itemId) {
					String newLine = userId + "," + itemId + "," + newRating.getValue();
					fileContent.set(i, newLine);
					break;
				}
			}

			Files.write(path, fileContent, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveRating(long userId, long itemId, Rating newRating) {
		String line = userId + "," + itemId + "," + newRating.getValue();
		appendToFile(ratingPath, line);
	}

	public boolean hasRatingForUser(long userId) {
		List<String> fileContent;
		try {
			fileContent = new ArrayList<>(Files.readAllLines(Paths.get(ratingPath), StandardCharsets.UTF_8));
			for (int i = 0; i < fileContent.size(); i++) {
				String[] splitLine = fileContent.get(i).split(",");
				long uid = Long.valueOf(splitLine[0]);
				if (uid == userId) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean hasUserRatingForItem(long userId, long itemId) {
		return getUserRatingForItem(userId, itemId) != null;
	}

	public Rating getUserRatingForItem(long userId, long itemId) {
		Rating rating = null;
		try (BufferedReader br = new BufferedReader(new FileReader(ratingPath))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitLine = line.split(",");
				long uid = Long.valueOf(splitLine[0]);
				long iid = Long.valueOf(splitLine[1]);
				if (uid == userId && iid == itemId) {
					int rat = Integer.valueOf(splitLine[2]);
					rating = Rating.valueOf(rat);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rating;
	}

	private void appendToFile(String path, String line) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
			bw.write(line);
			bw.newLine();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
}

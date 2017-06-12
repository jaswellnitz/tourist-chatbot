package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dataAccess.RatingDB;

import java.util.Random;

import model.Rating;

public class RatingGenerator {

	private static final String GENERATED_RATINGS_PATH = "src/main/resources/generatedRatings.csv";
	private static final String RATINGS_PATH = "src/main/resources/ratings.csv";

	private static boolean hasVisitedPOI(boolean interested) {
		double prob = new Random().nextDouble();
		boolean visited = interested ? prob < 0.8 : prob > 0.8;
		return visited;
	}

	private static Rating getPositiveRatingForPOI() {
		double prob = new Random().nextDouble();
		if (prob < 0.05) {
			return Rating._1;
		} else if (prob < 0.1) {
			return Rating._2;

		} else if (prob < 0.3) {
			return Rating._3;

		} else if (prob < 0.7) {
			return Rating._4;

		} else {
			return Rating._5;
		}
	}

	private static Rating getNegativeRatingForPOI() {
		double prob = new Random().nextDouble();
		if (prob < 0.1) {
			return Rating._5;
		} else if (prob < 0.15) {
			return Rating._4;

		} else if (prob < 0.8) {
			return Rating._3;

		} else if (prob < 0.9) {
			return Rating._2;

		} else {
			return Rating._1;
		}
	}

	private static List<List<Long>> createPOICategories() {
		List<List<Long>> categories = new ArrayList<>();

		List<Long> sightseeingIds = new ArrayList<>();
		sightseeingIds.add(359086841l); // Sagrada Familia
		sightseeingIds.add(249401770l); // Casa Battlo
		sightseeingIds.add(4357845893l); // Font Mágica
		sightseeingIds.add(66713401l); // Parc Guell
		sightseeingIds.add(166565579l); // Palau Guell
		sightseeingIds.add(51260215l); // Santa Maria Basilica
		sightseeingIds.add(34607291l); // Camp Nou

		List<Long> cultureIds = new ArrayList<>();
		cultureIds.add(377216661l); // Palau de la Música Catalana
		cultureIds.add(43995751l); // Museu Nacional d'Art de Catalunya
		cultureIds.add(1643199193l); // Museu d'Història de la Ciutat
		cultureIds.add(44026969l); // Museu d'Arqueologia de Catalunya

		List<Long> natureIds = new ArrayList<>();
		natureIds.add(180344732l); // Platja de Barceloneta
		natureIds.add(28183842l); // Parc de Joan Miró
		natureIds.add(8956042l); // Parc del Palau
		natureIds.add(66713401l); // Parc Guell

		List<Long> shoppingIds = new ArrayList<>();
		shoppingIds.add(422943231l); // Mercat de Sant Josep
		shoppingIds.add(248666372l); // La Maquinista
		shoppingIds.add(122432799l); // Mercat de la Barceloneta

		List<Long> meditarraneanFoodIds = new ArrayList<>();
		meditarraneanFoodIds.add(2200465185l); // Rossini
		meditarraneanFoodIds.add(2489047099l); // Bambarol
		meditarraneanFoodIds.add(4171922456l); // Bodega Biarritz

		List<Long> vegetarianFoodIds = new ArrayList<>();
		vegetarianFoodIds.add(2056514586l); // Sesamo
		vegetarianFoodIds.add(2485085325l); // Teresa Carles

		List<Long> asianFoodIds = new ArrayList<>();
		asianFoodIds.add(3074791956l); // Sushi Ya
		asianFoodIds.add(4726582895l); // Mosquito

		categories.add(sightseeingIds);
		categories.add(cultureIds);
		categories.add(natureIds);
		categories.add(shoppingIds);
		categories.add(meditarraneanFoodIds);
		categories.add(asianFoodIds);
		categories.add(vegetarianFoodIds);

		return categories;

	}

	private static Map<Long, boolean[]> createUsers(int categorySize) {

		Map<Long, boolean[]> users = new HashMap<>();
		long currentId = 1017l;
		int userPerCat = 2;
		// add user for each tourist category
		for (int i = 0; i < categorySize; i++) {
			boolean[] userPref = new boolean[categorySize];
			userPref[i] = true;
			for (int j = 0; j < userPerCat; j++) {
				users.put(currentId, userPref);
				currentId++;
			}
		}

		boolean[] userPrefFoodie = new boolean[categorySize];
		userPrefFoodie[4] = true;
		userPrefFoodie[5] = true;
		userPrefFoodie[6] = true;

		boolean[] userPrefExplorer = new boolean[categorySize];
		userPrefExplorer[0] = true;
		userPrefExplorer[1] = true;

		boolean[] userPrefRelax = new boolean[categorySize];
		userPrefRelax[2] = true;
		userPrefRelax[4] = true;
		userPrefRelax[5] = true;

		for (int j = 0; j < userPerCat; j++) {
			users.put(currentId, userPrefFoodie); // Foodie
			currentId++;

			users.put(currentId, userPrefExplorer); // Explorer
			currentId++;

			users.put(currentId, userPrefRelax); // Relaxer
			currentId++;
		}
		return users;
	}

	public static void main(String... args) throws IOException {
		// generateRatings(true);
		dataFromCSVToDB(RATINGS_PATH, 0);
	}

	// idshift is used to e.g. save already existing user again in database with different id
	private static void dataFromCSVToDB(String path, int idshift) throws IOException {
		List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8));
		RatingDB ratingDB = new RatingDB(System.getenv("DATABASE_URL"));
		for (int i = 0; i < fileContent.size(); i++) {
			String[] splitLine = fileContent.get(i).split(",");
			long uid = Long.valueOf(splitLine[0]) + idshift;
			long iid = Long.valueOf(splitLine[1]);
			int rat = Integer.valueOf(splitLine[2]);
			Rating rating = Rating.valueOf(rat);
			ratingDB.saveRating(uid, iid, rating);
		}
	}

	
	private static void generateRatings(boolean writeToDB) {
		RatingDB ratingDB = writeToDB ? new RatingDB(System.getenv("DATABASE_URL")) : null;
		UserRatingHandler userDataHandler = new UserRatingHandler(GENERATED_RATINGS_PATH);
		List<List<Long>> categories = createPOICategories();
		Map<Long, boolean[]> users = createUsers(categories.size());
		for (Entry<Long, boolean[]> entrySet : users.entrySet()) {
			boolean[] user = entrySet.getValue();
			long id = entrySet.getKey();
			for (int i = 0; i < categories.size(); i++) {
				System.out.println("Generate for UserId: " + id + " and category " + i);
				boolean interested = user[i];
				Rating rating;
				for (Long poiId : categories.get(i)) {
					if (hasVisitedPOI(interested)) {
						if (interested) {
							rating = getPositiveRatingForPOI();
						} else {
							rating = getNegativeRatingForPOI();
						}
						userDataHandler.saveRating(id, poiId, rating);
						if (writeToDB) {
							ratingDB.saveRating(id, poiId, rating);
						}
					}
				}
			}
		}

		System.out.println("Ratings generated.");
	}
}

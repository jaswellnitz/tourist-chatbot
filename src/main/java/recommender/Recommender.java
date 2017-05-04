package recommender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import data_access.PointConverter;
import data_access.UserDataHandler;
import model.Location;
import model.POIDataModel;
import model.POIProfile;
import model.PointOfInterest;
import model.ProfileItem;
import model.User;

public class Recommender {
	private PointConverter pointConverter;
	private static final String DEFAULT_RATING_PATH = "src/main/resources/ratings.csv";
	private final String ratingPath;
	private static final int NUM_RECOMMENDATIONS = 4;

	public Recommender(PointConverter pointConverter) {
		this(pointConverter,DEFAULT_RATING_PATH);
	}
	
	public Recommender(PointConverter pointConverter, String ratingPath){
		this.pointConverter = pointConverter;
		this.ratingPath = ratingPath;
	}

	public static void main(String... args) {
		PointConverter pointConverter = new PointConverter();
		UserDataHandler userDataHandler = new UserDataHandler();

		Recommender rec = new Recommender(pointConverter);
		long userId = 1001;
		User user = userDataHandler.getProfileForUser(userId);
		Location location = new Location(9.991636,53.550090);
		
		user.setCurrentLocation(location);
//		List<PointOfInterest> recommendedItems = rec.recommend(userId, "9.991636", "53.550090");
		List<PointOfInterest> recommendedItems = rec.recommendCollaborative(user);
		
		
		for (PointOfInterest recommendedPOI : recommendedItems) {
			System.out.println("Recommended item for user " + userId + ": " + recommendedPOI);
		}
		// User rates recommendation with a 2
		
		// Rating newRating = Rating._4;
		// userDataHandler.saveRating(userId,
		// recommendedItem.getId(),newRating);
	}
	
	public List<PointOfInterest> recommendCategory(User user, POIProfile profile){
		POIProfile originalProfile = user.getProfile();
		user.setProfile(profile);
		List<PointOfInterest> recommendedItems = recommendCollaborative(user);
		
		recommendContentBased(user, NUM_RECOMMENDATIONS);
		// TODO implement
		return null;
	}

	public List<PointOfInterest> recommend(User user){
		List<PointOfInterest> recommendedItems = recommendCollaborative(user);
		if(recommendedItems.size() < NUM_RECOMMENDATIONS){
			System.out.println(recommendedItems.size() + " result found from user ratings.");
			System.out.println("Content based recommendation...");
			recommendedItems.addAll(recommendContentBased(user, NUM_RECOMMENDATIONS-recommendedItems.size()));
		}
		return recommendedItems;
	}

	public List<PointOfInterest> recommendCollaborative(User user) {
		List<PointOfInterest> recommendedPOI = new ArrayList<>();
		Location location = user.getCurrentLocation();
		try {
			DataModel model = new FileDataModel(new File(ratingPath));
			UserSimilarity similarity = new LogLikelihoodSimilarity(model);
			UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
			UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
			List<RecommendedItem> recommendations = recommender.recommend(user.getId(), NUM_RECOMMENDATIONS);
			for (RecommendedItem recommendation : recommendations) {
				long itemId = recommendation.getItemID();
				System.out.println(itemId + ", " + recommendation.getValue());
				recommendedPOI.addAll(pointConverter.getPOIForId(itemId,location.getLatitude(), location.getLongitude(), user.getPrefRecommendationRadius()));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return recommendedPOI;
	}
	
	public List<PointOfInterest> recommendContentBased(User user,
			int numRecommendations) {
		Location location = user.getCurrentLocation();
		List<PointOfInterest> pois = pointConverter.getPOIInRadius(location.getLatitude(), location.getLongitude(), user.getPrefRecommendationRadius());
		List<ProfileItem> items = new ArrayList<>();
		items.addAll(pois);
		items.add(user);
		DataModel dm = new POIDataModel(items);
		long id = user.getId();
		UserSimilarity similarity = new ProfileSimilarity(dm);
		UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.5, similarity, dm);
		UserBasedRecommender rec = new GenericUserBasedRecommender(dm, neighborhood, similarity);
		List<Long> userIds = new ArrayList<>();

		try {
			userIds = LongStream.of(rec.mostSimilarUserIDs(id, numRecommendations)).boxed()
					.collect(Collectors.toList());
		} catch (TasteException e) {
			e.printStackTrace();
		}
		final List<Long> mostSimilarUserIDs = userIds;
		List<PointOfInterest> result = pois.stream().filter(t -> mostSimilarUserIDs.contains(t.getId()))
				.collect(Collectors.toList());
		return result;
	}
}

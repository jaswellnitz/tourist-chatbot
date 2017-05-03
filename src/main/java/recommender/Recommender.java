package recommender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import data_access.PointConverter;
import data_access.UserDataHandler;
import model.POIDataModel;
import model.PointOfInterest;
import model.ProfileItem;
import model.Rating;
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
//		List<PointOfInterest> recommendedItems = rec.recommend(userId, "9.991636", "53.550090");
		List<PointOfInterest> recommendedItems = rec.recommendCollaborative(user, "9.991636", "53.550090");
		
		
		for (PointOfInterest recommendedPOI : recommendedItems) {
			System.out.println("Recommended item for user " + userId + ": " + recommendedPOI);
		}
		// User rates recommendation with a 2
		
		// Rating newRating = Rating._4;
		// userDataHandler.saveRating(userId,
		// recommendedItem.getId(),newRating);
	}

	public List<PointOfInterest> recommend(User user, String lat, String lon){
		List<PointOfInterest> recommendedItems = recommendCollaborative(user,lat, lon);
		if(recommendedItems.size() < NUM_RECOMMENDATIONS){
			System.out.println(recommendedItems.size() + " result found from user ratings.");
			System.out.println("Content based recommendation...");
			recommendedItems.addAll(recommendContentBased(user, lat, lon, NUM_RECOMMENDATIONS-recommendedItems.size()));
		}
		return recommendedItems;
	}

	public List<PointOfInterest> recommendCollaborative(User user, String latitude, String longitude) {
		List<PointOfInterest> recommendedPOI = new ArrayList<>();
		try {
			DataModel model = new FileDataModel(new File(ratingPath));
			UserSimilarity similarity = new LogLikelihoodSimilarity(model);
			UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
			UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
			List<RecommendedItem> recommendations = recommender.recommend(user.getId(), NUM_RECOMMENDATIONS);
			for (RecommendedItem recommendation : recommendations) {
				long itemId = recommendation.getItemID();
				System.out.println(itemId + ", " + recommendation.getValue());
				recommendedPOI.addAll(pointConverter.getPOIForId(latitude, longitude, itemId,user.getPrefRecommendationRadius()));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return recommendedPOI;
	}

	public List<PointOfInterest> recommendContentBased(User user, String latitude, String longitude,
			int numRecommendations) {
		List<PointOfInterest> pois = pointConverter.getPOIInRadius(latitude, longitude, user.getPrefRecommendationRadius());
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

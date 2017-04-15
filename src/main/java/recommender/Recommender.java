package recommender;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	private UserDataHandler userDataHandler;
	private static final String RATING_PATH = "src/main/resources/ratings.csv";

	public Recommender(PointConverter pointConverter, UserDataHandler userDataHandler) {
		this.pointConverter = pointConverter;
		this.userDataHandler = userDataHandler;
	}

	public static void main(String... args) {
		PointConverter pointConverter = new PointConverter();
		UserDataHandler userDataHandler = new UserDataHandler();
		
		Recommender rec = new Recommender(pointConverter, userDataHandler);
		long userId = 1001;
		PointOfInterest recommendedItem = rec.recommend(userId, "9.991636", "53.550090");
		System.out.println("Recommended item for user "+ userId + ": " + recommendedItem);
		// User rates recommendation with a 2
		Rating newRating = Rating._4;
		userDataHandler.saveRating(userId, recommendedItem.getId(),newRating);
	}
	

	public PointOfInterest recommend(long userId, String lat, String lon){
		PointOfInterest recommendedItem = recommendCollaborative(userId,lat, lon);
		if (recommendedItem == null) {
			System.out.println("No result found from user ratings. Content based recommendation...");
			recommendedItem = recommendContentBased(userId, lat,lon);
		}
		return recommendedItem;
	}

	private PointOfInterest recommendCollaborative(long userId,String latitude, String longitude) {
		PointOfInterest recommendedPOI = null;
		try {
			DataModel model = new FileDataModel(new File(RATING_PATH));
			UserSimilarity similarity = new LogLikelihoodSimilarity(model);
			UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
			UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
			List<RecommendedItem> recommendations = recommender.recommend(userId, 3);
			if (!recommendations.isEmpty()) {
				long itemId = recommendations.get(0).getItemID();
				recommendedPOI = pointConverter.getPOIForId(latitude, longitude, itemId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return recommendedPOI;
	}

	private PointOfInterest recommendContentBased(long userId,String latitude, String longitude) {
		List<PointOfInterest> pois = pointConverter.getPOIInRadius(latitude, longitude);
		User user = userDataHandler.getUserFromProfile(userId);
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
			userIds = LongStream.of(rec.mostSimilarUserIDs(id, 5)).boxed().collect(Collectors.toList());
		} catch (TasteException e) {
			e.printStackTrace();
		}
		final List<Long> mostSimilarUserIDs = userIds;
//		System.out.println("Ids similar to " + id + " :" + mostSimilarUserIDs);
		List<PointOfInterest> result = pois.stream().filter(t -> mostSimilarUserIDs.contains(t.getId()))
				.collect(Collectors.toList());
//		System.out.println(result);
		return result.get(0);
	}
}

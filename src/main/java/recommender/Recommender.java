package recommender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import dataAccess.PointDB;
import dataAccess.RatingDB;
import model.Location;
import model.POIProfile;
import model.Preference;
import model.RecommendedPointOfInterest;
import model.ProfileItem;
import model.User;

public class Recommender {
	private PointDB pointConverter;
	public final int numRecommendations;
	private RatingDB ratingDB;
	private static final int DEFAULT_NUM_RECOMMENDATIONS = 4;

	public Recommender(PointDB pointConverter, RatingDB ratingDB) {
		this(pointConverter, ratingDB, DEFAULT_NUM_RECOMMENDATIONS);
	}

	public Recommender(PointDB pointConverter, RatingDB ratingDB, int numRecommendations) {
		this.pointConverter = pointConverter;
		this.numRecommendations = numRecommendations;
		this.ratingDB = ratingDB;
	}

	public List<RecommendedPointOfInterest> recommendForCategory(User user, int categoryIndex) {
		POIProfile originalProfile = user.getProfile();
		POIProfile categoryProfile = POIProfile.getProfileForCategoryIndex(categoryIndex);
		user.setProfile(categoryProfile);
		List<RecommendedPointOfInterest> recommendations = new ArrayList<>();
		
		if (ratingDB.hasRatingForUser(user.getId())) {
			recommendations = recommendCollaborative(user);
			List<RecommendedPointOfInterest> toRemove = filterRecommendationsForCategory(recommendations,
					categoryIndex);
			recommendations.removeAll(toRemove);
		}

		List<RecommendedPointOfInterest> recommendContentBased = recommendContentBased(user, recommendations);
		recommendations.addAll(recommendContentBased);

		user.setProfile(originalProfile);
		return recommendations;
	}

	public List<RecommendedPointOfInterest> recommend(User user) {
		List<RecommendedPointOfInterest> recommendedItems = new ArrayList<>();
		if (ratingDB.hasRatingForUser(user.getId())) {
			recommendedItems = recommendCollaborative(user);
		}
		recommendedItems.addAll(recommendContentBased(user, recommendedItems));
		return recommendedItems;
	}

	public List<RecommendedPointOfInterest> recommendCollaborative(User user) {
		List<RecommendedPointOfInterest> recommendedPOI = new ArrayList<>();
		Location location = user.getCurrentLocation();
		try {
			DataModel model = new PostgreSQLJDBCDataModel(ratingDB.getDataSource(),"ratings", "userId", "pointId", "ratings", null);
			UserSimilarity similarity = new LogLikelihoodSimilarity(model);
			UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
			UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
			List<RecommendedItem> recommendations = recommender.recommend(user.getId(), numRecommendations);
			for (RecommendedItem recommendation : recommendations) {
				long itemId = recommendation.getItemID();
				List<RecommendedPointOfInterest> pois = pointConverter.getPOIForId(itemId, location.getLatitude(),
						location.getLongitude(), user.getPrefRecommendationRadius());
				for (RecommendedPointOfInterest poi : pois) {
					poi.setRecommendationValue(recommendation.getValue());
					recommendedPOI.add(poi);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return recommendedPOI;
	}

	public List<RecommendedPointOfInterest> recommendContentBased(User user) {
		return recommendContentBased(user, new ArrayList<>());
	}

	private List<RecommendedPointOfInterest> filterRecommendationsForCategory(
			List<RecommendedPointOfInterest> recommendations, int categoryIndex) {
		List<RecommendedPointOfInterest> toRemove = new ArrayList<>();
		for (RecommendedPointOfInterest recommendedPointOfInterest : recommendations) {
			POIProfile poi = recommendedPointOfInterest.getProfile();
			if (poi.getAllCategories().get(categoryIndex) != Preference.TRUE) {
				toRemove.add(recommendedPointOfInterest);
			}
		}
		return toRemove;
	}

	// TODO compute recommendation value
	private List<RecommendedPointOfInterest> recommendContentBased(User user,
			List<RecommendedPointOfInterest> alreadyRecommendedPOIs) {

		List<RecommendedPointOfInterest> result = new ArrayList<>();

		if (alreadyRecommendedPOIs.size() < numRecommendations) {
			Location location = user.getCurrentLocation();
			List<RecommendedPointOfInterest> pois = pointConverter.getPOIInRadius(location.getLatitude(),
					location.getLongitude(), user.getPrefRecommendationRadius());

			// do not evaluate already recommended POIs again
			for (RecommendedPointOfInterest alreadyRecommendedPointOfInterest : alreadyRecommendedPOIs) {
				if (pois.contains(alreadyRecommendedPointOfInterest)) {
					pois.remove(alreadyRecommendedPointOfInterest);
				}
			}

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
				long[] mostSimilarUserIDs = rec.mostSimilarUserIDs(id,
						numRecommendations - alreadyRecommendedPOIs.size());
				userIds = LongStream.of(mostSimilarUserIDs).boxed().collect(Collectors.toList());
			} catch (TasteException e) {
				e.printStackTrace();
			}

			final List<Long> mostSimilarUserIDs = userIds;
			result.addAll(
					pois.stream().filter(t -> mostSimilarUserIDs.contains(t.getId())).collect(Collectors.toList()));
		}

		return result;
	}
	
}

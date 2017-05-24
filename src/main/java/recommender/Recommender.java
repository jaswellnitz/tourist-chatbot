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

import dataAccess.PointConverter;
import model.Location;
import model.POIProfile;
import model.Preference;
import model.RecommendedPointOfInterest;
import model.ProfileItem;
import model.User;

public class Recommender {
	private PointConverter pointConverter;
	private final String ratingPath;
	public final int numRecommendations;
	private static final String DEFAULT_RATING_PATH = "src/main/resources/ratings.csv";
	private static final int DEFAULT_NUM_RECOMMENDATIONS = 4;

	public Recommender(PointConverter pointConverter) {
		this(pointConverter, DEFAULT_RATING_PATH, DEFAULT_NUM_RECOMMENDATIONS);
	}

	public Recommender(PointConverter pointConverter, String ratingPath, int numRecommendations) {
		this.pointConverter = pointConverter;
		this.ratingPath = ratingPath;
		this.numRecommendations = numRecommendations;
	}

	// TODO category specific recommendation
	public List<RecommendedPointOfInterest> recommendForCategory(User user, int categoryIndex) {
		POIProfile originalProfile = user.getProfile();
		POIProfile categoryProfile = POIProfile.getProfileForCategoryIndex(categoryIndex);
		user.setProfile(categoryProfile);

		List<RecommendedPointOfInterest> recommendations = recommendCollaborative(user);
		List<RecommendedPointOfInterest> toRemove = filterRecommendationsForCategory(recommendations, categoryIndex);
		recommendations.removeAll(toRemove);

		recommendations.addAll(recommendContentBased(user, recommendations));

		user.setProfile(originalProfile);
		return recommendations;
	}

	public List<RecommendedPointOfInterest> recommend(User user, boolean existingRatings) {
		List<RecommendedPointOfInterest> recommendedItems = new ArrayList<>();
		if (existingRatings) {
			recommendedItems = recommendCollaborative(user);
		}
		recommendedItems.addAll(recommendContentBased(user, recommendedItems));
		return recommendedItems;
	}

	public List<RecommendedPointOfInterest> recommendCollaborative(User user) {
		List<RecommendedPointOfInterest> recommendedPOI = new ArrayList<>();
		Location location = user.getCurrentLocation();
		try {
			DataModel model = new FileDataModel(new File(ratingPath));
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

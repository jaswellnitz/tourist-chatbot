package recommender;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.log4j.Logger;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import dataAccess.PointDB;
import dataAccess.RatingDB;
import domain.Location;
import domain.RecommendedPointOfInterest;
import domain.User;
/**
 * The recommender in which the tourist recommendations are calculated. The recommendations are based on the user data using the machine learning library Apache Mahout.
 * @author Jasmin Wellnitz
 *
 */
public class Recommender {
	/**
	 * The total number of points of interest that should be returned by the recommender
	 */
	public static final int NUM_RECOMMENDATIONS = 3;
	
	private PointDB pointDB;
	private RatingDB ratingDB;
	Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Creates a recommender which has access on the ratings and POI database.
	 * @param pointConverter
	 * @param ratingDB
	 */
	public Recommender(PointDB pointConverter, RatingDB ratingDB) {
		this.pointDB = pointConverter;
		this.ratingDB = ratingDB;
	}

	/**
	 * Performs a category-specific recommendation, so only points of interest are returned that match the given tourist category
	 * @param user 
	 * @param touristCategory 
	 * @return Recommended points of interest
	 */
	public List<RecommendedPointOfInterest> recommendForCategory(User user, TouristCategory touristCategory) {
		POIProfile originalProfile = user.getProfile();
		POIProfile categoryProfile = POIProfile.getProfileForCategory(touristCategory);
		user.setProfile(categoryProfile);
		List<RecommendedPointOfInterest> recommendations = new ArrayList<>();
		
		if (ratingDB.hasRatingForUser(user.getId())) {
			recommendations = recommendCollaborative(user);
			List<RecommendedPointOfInterest> toRemove = filterRecommendationsForCategory(recommendations,
					touristCategory);
			recommendations.removeAll(toRemove);
		}

		List<RecommendedPointOfInterest> recommendContentBased = recommendContentBased(user, recommendations);
		recommendations.addAll(recommendContentBased);

		user.setProfile(originalProfile);
		return recommendations;
	}

	/**
	 * Performs a recommendation using first collaborative filtering and then content-based filtering if not enough recommendations were produced before.
	 * @param user
	 * @return recommended points of interest
	 */
	public List<RecommendedPointOfInterest> recommend(User user) {
		List<RecommendedPointOfInterest> recommendedItems = new ArrayList<>();
		if (ratingDB.hasRatingForUser(user.getId())) {
			recommendedItems = recommendCollaborative(user);
		}
		recommendedItems.addAll(recommendContentBased(user, recommendedItems));
		return recommendedItems;
	}

	/**
	 * Performs the collaborative filtering recommendation, based on user ratings.
	 * @param user
	 * @return
	 */
	public List<RecommendedPointOfInterest> recommendCollaborative(User user) {
		List<RecommendedPointOfInterest> recommendedPOI = new ArrayList<>();
		Location location = user.getCurrentLocation();
		try {
			DataModel model = new PostgreSQLJDBCDataModel(ratingDB.getDataSource(),"ratings", "userId", "pointId", "ratings", null);
			UserSimilarity similarity = new LogLikelihoodSimilarity(model);
			UserNeighborhood neighborhood = new NearestNUserNeighborhood(5, similarity, model);
			UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
			List<RecommendedItem> recommendations = recommender.recommend(user.getId(), NUM_RECOMMENDATIONS);
			for (RecommendedItem recommendation : recommendations) {
				long itemId = recommendation.getItemID();
				List<RecommendedPointOfInterest> pois = pointDB.getPOIForId(itemId, location.getLatitude(),
						location.getLongitude(), user.getPrefRecommendationRadius());
				for (RecommendedPointOfInterest poi : pois) {
					poi.setRecommendationValue(recommendation.getValue());
					recommendedPOI.add(poi);
				}
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return recommendedPOI;
	}

	/**
	 * Performs the content-based approach, based on the similarity between POI characteristics and user interests.
	 * @param user
	 * @return recommended points of interest
	 */
	public List<RecommendedPointOfInterest> recommendContentBased(User user) {
		return recommendContentBased(user, new ArrayList<>());
	}

	/**
	 * Filters recommended points of interests that do not fit the specified tourist category
	 * @param recommendations
	 * @param categoryIndex index that indicates the tourist category
	 * @return recommended points of interest
	 */
	private List<RecommendedPointOfInterest> filterRecommendationsForCategory(
			List<RecommendedPointOfInterest> recommendations, TouristCategory category) {
		List<RecommendedPointOfInterest> toRemove = new ArrayList<>();
		for (RecommendedPointOfInterest recommendedPointOfInterest : recommendations) {
			POIProfile poi = recommendedPointOfInterest.getProfile();
			if (poi.getPreferenceForCategory(category) != Preference.TRUE) {
				toRemove.add(recommendedPointOfInterest);
			}
		}
		return toRemove;
	}

	/**
	 * Performs the content based recommendation
	 * @param user
	 * @param alreadyRecommendedPOIs POI's that were recommended in a previous recommendation process - cannot be recommended again
	 * @return recommended points of interest
	 */
	private List<RecommendedPointOfInterest> recommendContentBased(User user,
			List<RecommendedPointOfInterest> alreadyRecommendedPOIs) {

		List<RecommendedPointOfInterest> result = new ArrayList<>();

		if (alreadyRecommendedPOIs.size() < NUM_RECOMMENDATIONS) {
			Location location = user.getCurrentLocation();
			List<RecommendedPointOfInterest> pois = pointDB.getPOIInRadius(location.getLatitude(),
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
						NUM_RECOMMENDATIONS - alreadyRecommendedPOIs.size());
				userIds = LongStream.of(mostSimilarUserIDs).boxed().collect(Collectors.toList());
			} catch (TasteException e) {
				logger.error(e);
			}

			final List<Long> mostSimilarUserIDs = userIds;
			result.addAll(
					pois.stream().filter(t -> mostSimilarUserIDs.contains(t.getId())).collect(Collectors.toList()));
		}

		return result;
	}
	
}

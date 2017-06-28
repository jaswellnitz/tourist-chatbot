package chatbot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dataAccess.RatingDB;
import dataAccess.UserDB;
import domain.Location;
import domain.Rating;
import domain.RecommendedPointOfInterest;
import domain.User;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import recommender.POIProfile;
import recommender.Recommender;
import service.ImageRequester;
import service.agent.AgentHandler;
import service.agent.AgentResponse;
import service.agent.Context;
import service.agent.Parameter;

/**
 * The tourist chatbot controls the conversation flow and accesses the different
 * components of the application, such as NLU platform, recommender, database.
 * 
 * @author Jasmin Wellnitz
 *
 */
public class TouristChatbot {

	private final AgentHandler agentHandler;
	private UserDB userDB;
	private Recommender recommender;
	private ImageRequester imageRequester;
	private RatingDB ratingsDB;
	/**
	 * A map containing the currently active users. A user is considered as active if he conversed with the chatbot in the last 24 hours.
	 */
	private final Map<Long, User> activeUsers;

	/**
	 * Creates the tourist chatbot
	 * 
	 * @param agentHandler
	 *            the NLU platform agent handler
	 * @param imageRequester
	 *            the image handler, in this case Foursquare
	 * @param recommender
	 *            the recommender
	 * @param userDB
	 *            the user database
	 * @param ratingsDB
	 *            the ratings database
	 */
	public TouristChatbot(AgentHandler agentHandler, ImageRequester imageRequester, Recommender recommender,
			UserDB userDB, RatingDB ratingsDB) {
		this.agentHandler = agentHandler;
		this.activeUsers = ExpiringMap.builder().maxSize(100).expirationPolicy(ExpirationPolicy.ACCESSED)
				.expiration(1, TimeUnit.DAYS).build();
		this.userDB = userDB;
		this.recommender = recommender;
		this.imageRequester = imageRequester;
		this.ratingsDB = ratingsDB;
	}

	/**
	 * Processes the start message, stores the new user and overrides the old one if need be.
	 * @param userId
	 * @param userName
	 * @return a chatbot response
	 */
	public ChatbotResponse processStartMessage(long userId, String userName) {
		if (userDB.hasUser(userId)) {
			ratingsDB.deleteAllUserRatings(userId);
			userDB.deleteUser(userId);
		}

		User user = new User(userId, userName);
		userDB.storeUser(user);
		getActiveUsers().put(userId, user);
		AgentResponse agentResponse = agentHandler.sendEvent("WELCOME", user.getId(), true);
		return new ChatbotResponse(agentResponse.getReply());
	}

	/**
	 * Handles the user input: transfers the user input to the NLU platform, checks which action needs to be taken based on the agent response
	 * and creates one or multiple chatbot responses.
	 * @param userId
	 * @param userInput
	 * @return chatbot responses
	 */
	public List<ChatbotResponse> processInput(long userId, Object userInput) {
		assert userInput != null : "Precondition failed: userInput != null";

		User user = getUserFromId(userId);
		AgentResponse agentResponse = agentHandler.sendUserInput(userInput.toString(), user.getId());
		List<ChatbotResponse> chatbotResponses = new ArrayList<>();

		switch (agentResponse.getAction()) {
		case ABOUT:
			String answer = getAboutText(user.getPrefRecommendationRadius());
			chatbotResponses.add(new ChatbotResponse(answer));
			break;
		case SAVE_INTEREST:
			// TODO error handling
			saveInterests(user, agentResponse);
			chatbotResponses.add(new ChatbotResponse(agentResponse.getReply()));
			break;
		case SHOW_INFORMATION:
			answer = getPersonalInformation(user);
			chatbotResponses.add(new ChatbotResponse(answer));
			break;
		case RECOMMEND_LOCATION:
			chatbotResponses.add(new ChatbotResponse(agentResponse.getReply(), "Send Current Location"));
			break;
		case RECOMMEND:
			if (userInput instanceof Location) {
				user.setCurrentLocation((Location) userInput);
			} else {
				String[] coordinates = ((String) userInput).split(",");
				Location location = new Location(Double.valueOf(coordinates[0]), Double.valueOf(coordinates[1]));
				user.setCurrentLocation(location);
			}
			String interest = "";
			for (Context context : agentResponse.getContexts()) {
				if (context.getName().equals("recommendation")) {
					if (context.getParameters().containsKey(Parameter.INTEREST.name())) {
						interest = (String) context.getParameters().get(Parameter.INTEREST.name());
						break;
					}
				}
			}
			chatbotResponses.addAll(getRecommendation(user, interest));
			break;
		case RECOMMENDATION_POSITIVE:
			processFirstImpressionForPreviousPOI(user, true);
			if (user.getPendingRecommendations().isEmpty()) {
				chatbotResponses.add(new ChatbotResponse(agentResponse.getReply()));
				agentHandler.resetContext(user.getId());
			} else {
				chatbotResponses.add(presentPendingPOIs(user));
			}
			break;
		case RECOMMENDATION_NEGATIVE:
			processFirstImpressionForPreviousPOI(user, false);
			if (user.getPendingRecommendations().isEmpty()) {
				chatbotResponses.add(new ChatbotResponse(agentResponse.getReply()));
				agentHandler.resetContext(user.getId());
			} else {
				chatbotResponses.add(presentPendingPOIs(user));
			}
			break;
		case RECOMMENDATION_MORE:
			int ind = Integer.valueOf(((String) agentResponse.getParameters().get(Parameter.POI_INDEX.name()))) - 1;
			chatbotResponses.addAll(presentRecommendationResult(user, ind));
			break;
		case SHOW_PAST_RECOMMENDATIONS:
			if (!user.getPositiveRecommendations().isEmpty()) {
				chatbotResponses.addAll(showPastRecommendations(user));
			} else {
				chatbotResponses.add(new ChatbotResponse(agentResponse.getReply()));
			}
			break;
		case SAVE_RADIUS:
			trySaveRadius(user, agentResponse);
			// answer = "Please enter a valid input.";
			// TODO set context correctly
			chatbotResponses.add(new ChatbotResponse(agentResponse.getReply()));
			break;
		case RATE:
			int rateIndex = 0;
			int rating = Integer.valueOf((String) agentResponse.getParameters().get(Parameter.RATING.name()));
			processRating(user, rateIndex, rating);
			// TODO error handling
			chatbotResponses.add(new ChatbotResponse(agentResponse.getReply()));
			break;
		case GREETINGS:
			chatbotResponses.add(new ChatbotResponse(agentResponse.getReply()));
			if (agentResponse.getContexts().isEmpty() && !user.getUnratedPOIs().isEmpty()) {
				chatbotResponses.add(createRatePrompt(user,0));
				agentHandler.setContext("Rate", user.getId());
			}
			break;
		default:
			chatbotResponses.add(new ChatbotResponse(agentResponse.getReply()));
		}

		return chatbotResponses;

	}

	/**
	 * Filters the user interests from the agent response and saves them.
	 * @param user
	 * @param response
	 */
	private void saveInterests(User user, AgentResponse response) {
		for (Context context : response.getContexts()) {
			if (context.getName().equals("interview")) {
				@SuppressWarnings("unchecked")
				List<String> interests = (List<String>) context.getParameters().get(Parameter.INTEREST.name());
				POIProfile profile = POIProfile.getProfileForInterests(interests);
				user.setProfile(profile);
				userDB.changeProfileForUser(user.getId(), profile);
			}
		}
	}

	/**
	 * Returns the chatbot's presentation text.
	 * @param recommendationRadius
	 * @return presentation text 
	 */
	private String getAboutText(int recommendationRadius) {
		String aboutText = "Hey there, I am your friendly tourist chatbot! I will try my best to recommend you cool places on your trip. "
				+ "As I get to know you better, my recommendations are going to be more adjusted to your interests."
				+ "\n\n" + "Currently, I am looking for recommendations in a distance of " + recommendationRadius
				+ " m from you. " + "You can change that distance anytime." + "\n\n"
				+ "If you already asked for recommendations, I can show them to you if you like."
				+ " While doing so, you can also tell me how you liked them in order to improve my recommendations.";
		return aboutText;
	}
	

	/**
	 * Filters the agent response for the new recommendation radius and saves it.
	 * @param user
	 * @param response the agent response
	 * @return boolean that indicates whether the action was successful
	 */
	// TODO refactor
	private boolean trySaveRadius(User user, AgentResponse response) {
		boolean succesful = false;
		Object object = response.getParameters().get(Parameter.DISTANCE.name());
		if (object instanceof Integer) {
			int radius = (int) object;
			if (radius > 0) {
				user.setPrefRecommendationRadius(radius);
				userDB.changeRadiusForUser(user.getId(), radius);
				succesful = true;
			}
		}
		return succesful;
	}
	


	/**
	 * Shows the user's past recommendations. If the user has recommendations that are marked as unrated, he is asked to rate the oldest recommendation.
	 * @param user
	 * @return
	 */
	private List<ChatbotResponse> showPastRecommendations(User user) {
		List<ChatbotResponse> responses = new ArrayList<>();
		String reply = "Here are the past recommendations you were interested in: ";
		for (RecommendedPointOfInterest poi : user.getPositiveRecommendations()) {
			reply += "\n" + poi.getFormattedString(false);
		}
		responses.add(new ChatbotResponse(reply));
		if (!user.getUnratedPOIs().isEmpty()) {
			ChatbotResponse rate = createRatePrompt(user,0);
			responses.add(rate);
		}
		return responses;
	}

	/**
	 * The user was asked to rate his first impression. A positive first impression is saved; a provisional rating of 4 is given 
	 * and the rating is marked as unrated recommendation for the user, so he is going to be asked for his opinion later again.
	 * Negative impressions are discarded and rated with a value of 1.
	 * @param user
	 * @param positiveImpression
	 */
	private void processFirstImpressionForPreviousPOI(User user, boolean positiveImpression) {
		RecommendedPointOfInterest recPointOfInterest = user.getPendingRecommendations()
				.get(user.getLastRecommendedIndex());
		if (positiveImpression) {
			ratingsDB.saveRating(user.getId(), recPointOfInterest.getId(), Rating._4);
			user.addUnratedPOI(recPointOfInterest);
			user.addPositiveRecommendations(recPointOfInterest);
			userDB.addRecommendation(user.getId(), recPointOfInterest.getId());
		} else {
			ratingsDB.saveRating(user.getId(), recPointOfInterest.getId(), Rating._1);
		}
		user.getPendingRecommendations().remove(user.getLastRecommendedIndex());
	}

	/**
	 * Shows the user the pending recommendations that were calculated during the last recommendation process and not seen yet.
	 * @param user
	 * @return the chatbot response
	 */
	private ChatbotResponse presentPendingPOIs(User user) {
		String[] numbers = new String[user.getPendingRecommendations().size()];
		String answer = "Thank you! I have found these other POIs you might be interested in: ";
		for (int i = 0; i < user.getPendingRecommendations().size(); i++) {
			RecommendedPointOfInterest recommendedPointOfInterest = user.getPendingRecommendations().get(i);
			numbers[i] = String.valueOf(i + 1);
			answer += "\n" + (i + 1) + ": " + recommendedPointOfInterest.getName();
			if (recommendedPointOfInterest.getRecommendationValue() > 0.1) {
				answer += ", our computed recommendation value: " + recommendedPointOfInterest.getRecommendationValue();
			}
		}
		answer = answer + "\nDo you want to know more about any of them?";
		return new ChatbotResponse(answer, numbers);
	}

	/**
	 * Triggers the recommendation process. 
	 * @param user
	 * @param interest if an interest is set (the value is not empty), a category-specific recommendation is triggered
	 * @return the chatbot responses (the recommendation result itself and a picture and asking for the user's first impression)
	 */
	private List<ChatbotResponse> getRecommendation(User user, String interest) {
		List<RecommendedPointOfInterest> recommendations = new ArrayList<>();
		if (interest.isEmpty()) {
			recommendations = recommender.recommend(user);
		} else {
			recommendations = recommender.recommendForCategory(user, POIProfile.getCategoryIndex(interest));
		}
		
		// POIs that were already recommended and rated positively are not recommended again.
		List<RecommendedPointOfInterest> toRemove = new ArrayList<>();
		for (RecommendedPointOfInterest recommendation : recommendations) {
			if (user.getPositiveRecommendations().contains(recommendation)) {
				toRemove.add(recommendation);
			}
		}
		recommendations.removeAll(toRemove);

		List<ChatbotResponse> chatbotResponses = new ArrayList<>();
		String answer;
		if (recommendations.isEmpty()) {
			answer = "I didn't find any recommendations near your current location. Maybe consider extending the recommendation radius?";
			chatbotResponses.add(new ChatbotResponse(answer));
		} else {
			user.setPendingRecommendations(recommendations);
			chatbotResponses.addAll(presentRecommendationResult(user, 0));
		}
		return chatbotResponses;
	}

	/**
	 * Creates chatbot responses that contain the recommendation result and asking the user for his first impression
	 * @param user
	 * @param index pending recommendation index
	 * @return the chatbot responses (the recommendation result itself and a picture and asking for the user's first impression)
	 */
	private List<ChatbotResponse> presentRecommendationResult(User user, int index) {
		assert index >= 0
				&& index < user.getPendingRecommendations().size() : "Preconditon failed: index out of bounds";

		user.setLastRecommendedIndex(index);
		List<ChatbotResponse> chatbotResponses = new ArrayList<>();
		String reply = "Here we go:\n";
		RecommendedPointOfInterest recommendedPointOfInterest = user.getPendingRecommendations().get(index);
		String imageURL = imageRequester.getImageURL(recommendedPointOfInterest.getName(),
				recommendedPointOfInterest.getLocation());
		if (!imageURL.isEmpty()) {
			chatbotResponses.add(new ChatbotResponse(imageURL));
		}
		reply += recommendedPointOfInterest.getFormattedString(true) + "\n";
		reply += "What do you think about this place?";
		chatbotResponses.add(new ChatbotResponse(reply, "Sounds good!", "Don't like it..."));
		return chatbotResponses;
	}

	/**
	 * Processes the user rating:
	 * saves the rating in the data base and marks the recommendation as rated
	 * @param user
	 * @param rateIndex index of the recommended poi
	 * @param ratingValue
	 * @return
	 */
	private boolean processRating(User user, int rateIndex, int ratingValue) {
		assert !user.getUnratedPOIs().isEmpty() : "Precondition failed: !user.getUnratedPOIs().isEmpty()";
		Rating rating = Rating.valueOf(ratingValue);
		if (rating != Rating.INVALID) {
			RecommendedPointOfInterest recommendedPointOfInterest = user.getUnratedPOIs().get(rateIndex);
			ratingsDB.updateRating(user.getId(), recommendedPointOfInterest.getId(), Rating.valueOf(ratingValue));
			user.getUnratedPOIs().remove(rateIndex);
			userDB.deleteFirstUnratedRecommendation(user.getId());
			return true;
		}
		return false;
	}

	/**
	 * Creates a rate prompt which asks the user to rate a previously seen POI
	 * @param user
	 * @param rateIndex the user's unrated recommendation to be rated
	 * @return a chatbot response, asking for the user's opinion and modifying the keyboard
	 */
	private ChatbotResponse createRatePrompt(User user, int rateIndex) {
		RecommendedPointOfInterest pointOfInterest = user.getUnratedPOIs().get(rateIndex);
		String ret = "By the way, how many stars would you give " + pointOfInterest.getName() + "?";

		String[] ratings = { "1", "2", "3", "4", "5" };

		ChatbotResponse response = new ChatbotResponse(ret, ratings);
		return response;
	}
	
	/**
	 * Gets the personal information for the user and creates the chatbot reply
	 * @param user
	 * @return the chatbot reply
	 */
	private String getPersonalInformation(User user) {
		String answer = "So, here's what I know about you: Your current recommendation radius is "
				+ user.getPrefRecommendationRadius() + " m.";
		List<String> interests = user.getProfile().getInterestsFromProfile();

		if (!interests.isEmpty()) {
			String interestString = "";
			for (String interest : interests) {
				interestString += interest + ", ";
			}
			interestString = interestString.substring(0, interestString.length() - 2);
			answer += "\n\nYou are interested in: " + interestString + ".";
		}

		return answer;
	}

	/**
	 * Gets the user from the messenger id. If the user is already active, the database do not have to be called.
	 * If the user was not active before, the user is retrieved from the database and added to the activeUsers cache.
	 * @param userId the messenger id
	 * @return
	 */
	private User getUserFromId(long userId) {
		if (getActiveUsers().containsKey(userId)) {
			return getActiveUsers().get(userId);
		}
		User user;
		if (userDB.hasUser(userId)) {
			user = userDB.getUser(userId);
		} else {
			user = new User(userId, "");
		}
		getActiveUsers().put(user.getId(), user);
		return user;
	}
	

	// public for JUnit testing
	public Map<Long, User> getActiveUsers() {
		return activeUsers;
	}

}

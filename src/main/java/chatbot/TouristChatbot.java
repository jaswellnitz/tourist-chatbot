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
import service.agent.ParameterKey;

public class TouristChatbot {

	private final AgentHandler agentHandler;
	private final Map<Long, User> activeUsers;
	private UserDB userDB;
	private Recommender recommender;
	private ImageRequester imageRequester;
	private RatingDB ratingsDB;

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

	public List<ChatbotResponse> processInput(long userId, Object userInput) {
		assert userInput != null : "Precondition failed: userInput != null";

		User user = getUserFromId(userId);
		System.out.println(userInput + ", " + user);
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
				user.setCurrentLocation(Double.valueOf(coordinates[0]), Double.valueOf(coordinates[1]));
			}
			String interest = "";
			for(Context context: agentResponse.getContexts()){
				if(context.getName().equals("recommendation")){
					if (context.getParameters().containsKey(ParameterKey.INTEREST.name())) {
						interest = (String) context.getParameters().get(ParameterKey.INTEREST.name());
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
			int ind = Integer.valueOf(((String) agentResponse.getParameters().get(ParameterKey.POI_INDEX.name())))
					- 1;
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
			int rating = Integer.valueOf((String) agentResponse.getParameters().get(ParameterKey.RATING.name()));
			processRating(user, rateIndex, rating);
			// TODO error handling
			chatbotResponses.add(new ChatbotResponse(agentResponse.getReply()));
			break;
		case GREETINGS:
			chatbotResponses.add(new ChatbotResponse(agentResponse.getReply()));
			if (agentResponse.getContexts().isEmpty() && !user.getUnratedPOIs().isEmpty()) {
				chatbotResponses.add(rateFirstUnratedItem(user));
				agentHandler.setContext("Rate", user.getId());
			}
			break;
		default:
			chatbotResponses.add(new ChatbotResponse(agentResponse.getReply()));
		}

		return chatbotResponses;

	}

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

	private ChatbotResponse rateFirstUnratedItem(User user) {
		RecommendedPointOfInterest pointOfInterest = user.getUnratedPOIs().get(0);
		String ret = "By the way, how many stars would you give " + pointOfInterest.getName() + "?";

		String[] ratings = { "1", "2", "3", "4", "5" };

		ChatbotResponse response = new ChatbotResponse(ret, ratings);
		return response;
	}

	private List<ChatbotResponse> showPastRecommendations(User user) {
		List<ChatbotResponse> responses = new ArrayList<>();
		String reply = "Here are the past recommendations you were interested in: ";
		for (RecommendedPointOfInterest poi : user.getPositiveRecommendations()) {
			reply += "\n" + poi.getFormattedString(false);
		}
		responses.add(new ChatbotResponse(reply));
		if (!user.getUnratedPOIs().isEmpty()) {
			ChatbotResponse rate = rateFirstUnratedItem(user);
			responses.add(rate);
		}
		return responses;
	}

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

	public Map<Long, User> getActiveUsers() {
		return activeUsers;
	}

	private List<ChatbotResponse> getRecommendation(User user, String interest) {
		List<RecommendedPointOfInterest> recommendations = new ArrayList<>();
		if (interest.isEmpty()) {
			recommendations = recommender.recommend(user);
		} else {
			recommendations = recommender.recommendForCategory(user, POIProfile.getCategoryIndex(interest));
		}
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

	// TODO refactor
	private boolean trySaveRadius(User user, AgentResponse response) {
		boolean succesful = false;
		Object object = response.getParameters().get(ParameterKey.DISTANCE.name());
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

	private void saveInterests(User user, AgentResponse response) {
		for (Context context : response.getContexts()) {
			if (context.getName().equals("interview")) {
				List<String> interests = (List<String>) context.getParameters().get(ParameterKey.INTEREST.name());
				POIProfile profile = POIProfile.getProfileForInterests(interests);
				user.setProfile(profile);
				userDB.changeProfileForUser(user.getId(), profile);
			}
		}
	}

	private String getAboutText(int recommendationRadius) {
		String aboutText = "Hey there, I am your friendly tourist chatbot! I will try my best to recommend you cool places on your trip. "
				+ "As I get to know you better, my recommendations are going to be more adjusted to your interests."
				+ "\n\n" + "Currently, I am looking for recommendations in a distance of " + recommendationRadius
				+ " m from you. " + "You can change that distance anytime." + "\n\n"
				+ "If you already asked for recommendations, I can show them to you if you like."
				+ " While doing so, you can also tell me how you liked them in order to improve my recommendations.";
		return aboutText;
	}
}

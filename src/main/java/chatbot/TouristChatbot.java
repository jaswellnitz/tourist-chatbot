package chatbot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import data_access.UserDB;
import data_access.UserRatingHandler;
import model.Location;
import model.POIProfile;
import model.Rating;
import model.RecommendedPointOfInterest;
import model.User;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import recommender.Recommender;

public class TouristChatbot {

	private final AgentHandler agentHandler;
	private final Map<Long, User> activeUsers;
	private UserDB userDB;
	private Recommender recommender;
	private UserRatingHandler userRatingHandler;

	public TouristChatbot(AgentHandler agentHandler, Recommender recommender, UserDB userDB,
			UserRatingHandler ratingHandler) {
		this.agentHandler = agentHandler;
		this.activeUsers = ExpiringMap.builder().maxSize(100).expirationPolicy(ExpirationPolicy.ACCESSED)
				.expiration(1, TimeUnit.DAYS).build();
		this.userDB = userDB;
		this.recommender = recommender;
		this.userRatingHandler = ratingHandler;
	}
	
	public ChatbotResponse processStartMessage(long userId, String userName) {
		if (userDB.hasUser(userId)) {
			// TODO cleanup user ratings
			userDB.deleteUser(userId);
		}

		User user = new User(userId, userName);
		userDB.storeUser(user);
		getActiveUsers().put(userId, user);
		AgentResponse agentResponse = agentHandler.sendEvent("WELCOME", user.getId(), true);
		System.out.println(agentResponse.getSessionId());
		return new ChatbotResponse(agentResponse.getReply());
	}

	public ChatbotResponse processInput(long userId, Object userInput) {
		assert userInput != null : "Precondition failed: userInput != null";

		User user = getUserFromId(userId);
		System.out.println(userInput + ", " + user);
		AgentResponse agentResponse = agentHandler.sendUserInput(userInput.toString(), user.getId());
		ChatbotResponse chatbotResponse;

		switch (agentResponse.getAction()) {
		case ABOUT:
			String answer = getAboutText(user.getPrefRecommendationRadius());
			chatbotResponse = new ChatbotResponse(answer);
			break;
		case SAVE_INTEREST:
			saveInterests(user, agentResponse);
			chatbotResponse = new ChatbotResponse(agentResponse.getReply());
			break;
		case SHOW_INFORMATION:
			answer = getPersonalInformation(user);
			chatbotResponse = new ChatbotResponse(answer);
			break;
		case RECOMMEND_LOCATION:
			chatbotResponse = new ChatbotResponse(agentResponse.getReply(), "Send Location");
			break;
		case RECOMMEND:
			user.setCurrentLocation((Location) userInput);
			chatbotResponse = getRecommendation(user);
			break;
		case RECOMMENDATION_POSITIVE:
			answer = agentResponse.getReply();
			processFirstImpressionForPreviousPOI(user, true);
			if (user.getPendingRecommendations().isEmpty()) {
				chatbotResponse = new ChatbotResponse(agentResponse.getReply());
				agentHandler.resetContext(user.getId());
			} else {
				chatbotResponse = presentPendingPOIs(user);
			}
			break;
		case RECOMMENDATION_NEGATIVE:
			answer = agentResponse.getReply();
			processFirstImpressionForPreviousPOI(user, false);
			if (user.getPendingRecommendations().isEmpty()) {
				chatbotResponse = new ChatbotResponse(agentResponse.getReply());
				agentHandler.resetContext(user.getId());
			} else {
			chatbotResponse = presentPendingPOIs(user);
			}
			break;
		case RECOMMENDATION_MORE:
			int index = Integer.valueOf((String)agentResponse.getParameters().get("number"))-1;
			chatbotResponse = presentRecommendationResult(user, index);
			break;
		case SHOW_PAST_RECOMMENDATIONS:
			chatbotResponse = showPastRecommendations(user);
			break;
		case SAVE_RADIUS:
			answer = agentResponse.getReply();
			if (trySaveRadius(user, agentResponse)) {
				answer = agentResponse.getReply();
			} else {
//				answer = "Please enter a valid input.";
				// TODO set context correctly
			}
			chatbotResponse = new ChatbotResponse(answer);
			break;
		case NONE:
			chatbotResponse = new ChatbotResponse(agentResponse.getReply());
			break;
		default:
			chatbotResponse = new ChatbotResponse(agentResponse.getReply());
		}

		return chatbotResponse;

	}

	// TODO rating
	private ChatbotResponse showPastRecommendations(User user) {
		String ret = "Here are the past recommendations you were interested in: ";
		for(RecommendedPointOfInterest poi: user.getPositiveRecommendations()){
			ret += "\n" +poi.getFormattedString();
		}
		// rate
		ChatbotResponse response = new ChatbotResponse(ret);
		return response;
	}

	private void processFirstImpressionForPreviousPOI(User user, boolean positiveImpression) {
		RecommendedPointOfInterest recPointOfInterest = user.getPendingRecommendations().get(user.getLastRecommendedIndex());
		if (positiveImpression) {
//			userRatingHandler.saveRating(user.getId(), recPointOfInterest.getId(), Rating._4);
			user.addUnratedPOI(recPointOfInterest);
			user.addPositiveRecommendations(recPointOfInterest);
		} else {
//			userRatingHandler.saveRating(user.getId(), recPointOfInterest.getId(), Rating._1);
		}
		user.getPendingRecommendations().remove(user.getLastRecommendedIndex());
	}

	private ChatbotResponse presentPendingPOIs(User user){
		String[] numbers = new String[user.getPendingRecommendations().size()];
		String answer= "Thank you! I have found these other POIs you may are interested in: ";
		for(int i = 0; i < user.getPendingRecommendations().size(); i++){
			RecommendedPointOfInterest recommendedPointOfInterest = user.getPendingRecommendations().get(i);
			numbers[i] = String.valueOf(i+1);
			answer += "\n" + (i+1) +": "+ recommendedPointOfInterest.getName();
			if(recommendedPointOfInterest.getRecommendationValue() != 0){
				answer+=", our computed recommendation value: "+ recommendedPointOfInterest.getRecommendationValue();
			}
		}
		answer = answer + "\nDo you want to know more about any of them?";
		return new ChatbotResponse(answer, numbers);
	}

	public Map<Long, User> getActiveUsers() {
		return activeUsers;
	}

	private ChatbotResponse getRecommendation(User user) {
		List<RecommendedPointOfInterest> recommendations = recommender.recommend(user);
		ChatbotResponse chatbotResponse;
		String answer;
		user.setPendingRecommendations(recommendations);
		if (recommendations.isEmpty()) {
			answer = "I didn't find any recommendations near your current location. Maybe consider extending the recommendation radius?";
			chatbotResponse = new ChatbotResponse(answer);
		} else {
			chatbotResponse = presentRecommendationResult(user,0);
		}
		return chatbotResponse;
	}
	
	private ChatbotResponse presentRecommendationResult(User user, int index){
		assert index > 0 && index < user.getPendingRecommendations().size(): "Preconditon failed: index out of bounds";
		
		user.setLastRecommendedIndex(index);
		String reply = "Here we go:\n";
		reply+= user.getPendingRecommendations().get(index).getFormattedString() + "\n";
		reply += "What do you think about this place?";
		return new ChatbotResponse(reply, "Sounds good!", "Don't like it...");
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

	// TODO check if telegram bot responds when /start was not triggered
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
		Object object = response.getParameters().get("distance");
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
				List<String> interests = (List<String>) context.getParameters().get("interest");
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

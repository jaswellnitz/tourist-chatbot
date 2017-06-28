package chatbot;

/**
 * An action to be triggered, specified by the NLU agent.
 * @author Jasmin Wellnitz
 *
 */
public enum Action {

	ABOUT, SAVE_INTEREST, SAVE_RADIUS, SHOW_INFORMATION,
	RECOMMEND_LOCATION, RECOMMEND,RECOMMENDATION_POSITIVE, RECOMMENDATION_NEGATIVE, RECOMMENDATION_MORE, 
	RATE, SHOW_PAST_RECOMMENDATIONS, GREETINGS,
	NONE;
	
	/**
	 * Converts the string representation into the enum value.
	 * @param actionString
	 * @return Action
	 */
	public static Action getEnum(String actionString){
		for(Action a: values()){
			if(actionString.toUpperCase().equals(a.name())){
				return a;
			}
		}
		return Action.NONE;
	}
}

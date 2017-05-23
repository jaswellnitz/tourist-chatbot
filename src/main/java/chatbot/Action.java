package chatbot;

public enum Action {

	ABOUT, SAVE_INTEREST, SAVE_RADIUS, SHOW_INFORMATION,
	RECOMMEND_LOCATION, RECOMMEND,RECOMMENDATION_POSITIVE, RECOMMENDATION_NEGATIVE, RECOMMENDATION_MORE, 
	RATE, SHOW_PAST_RECOMMENDATIONS, GREETINGS,
	NONE;
	
	public static Action getEnum(String s){
		for(Action a: values()){
			if(s.toUpperCase().equals(a.name())){
				return a;
			}
		}
		return Action.NONE;
	}
}

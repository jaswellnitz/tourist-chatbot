package chatbot;

public enum Action {

	ABOUT, SAVE_INTEREST, SAVE_RADIUS, RECOMMEND_LOCATION, RECOMMEND,RECOMMENDATION_POSITIVE, RECOMMENDATION_NEGATIVE, RECOMMENDATION_MORE, SHOW_PAST_RECOMMENDATIONS, NONE, SHOW_INFORMATION;
	
	public static Action getEnum(String s){
		for(Action a: values()){
			if(s.toUpperCase().equals(a.name())){
				return a;
			}
		}
		return Action.NONE;
	}
}

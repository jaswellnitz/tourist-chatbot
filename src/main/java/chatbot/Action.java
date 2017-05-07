package chatbot;

public enum Action {

	ABOUT, SAVE_INTEREST, SAVE_RADIUS, RECOMMEND, SHOW_RECOMMENDATIONS, NONE;
	
	public static Action getEnum(String s){
		for(Action a: values()){
			if(s.toUpperCase().equals(a.name())){
				return a;
			}
		}
		return Action.NONE;
	}
}

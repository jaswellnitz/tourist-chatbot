package chatbot;

public enum Action {

<<<<<<< HEAD
	ABOUT, SAVE_INTEREST, SAVE_RADIUS, RECOMMEND, SHOW_RECOMMENDATIONS, NONE, SHOW_INFORMATION;
=======
	ABOUT, SAVE_INTEREST, SAVE_RADIUS, RECOMMEND, SHOW_RECOMMENDATIONS, NONE;
>>>>>>> 305535a612534a56e7f2c460a883c17155f98478
	
	public static Action getEnum(String s){
		for(Action a: values()){
			if(s.toUpperCase().equals(a.name())){
				return a;
			}
		}
		return Action.NONE;
	}
}

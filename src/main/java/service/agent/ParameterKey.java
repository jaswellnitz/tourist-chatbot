package service.agent;

public enum ParameterKey {
	DISTANCE, RATING, POI_INDEX, INTEREST, NONE;
	
	public static ParameterKey getEnum(String s){
		for(ParameterKey p: values()){
			if(s.toUpperCase().equals(p.name())){
				return p;
			}
		}
		return ParameterKey.NONE;
	}

}

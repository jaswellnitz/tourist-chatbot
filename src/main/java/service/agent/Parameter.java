package service.agent;

/**
 * An enum defining the different parameters that can be filtered by the agent from the user input.
 * @author Jasmin Wellnitz
 *
 */
public enum Parameter {
	DISTANCE, RATING, POI_INDEX, INTEREST, NONE;
	
	/**
	 * Gets the corresponding enum from the string representation. If no equivalent enum was found, the value NONE is returned.
	 * @param s
	 * @return
	 */
	public static Parameter getEnum(String s){
		for(Parameter p: values()){
			if(s.toUpperCase().equals(p.name())){
				return p;
			}
		}
		return Parameter.NONE;
	}

}

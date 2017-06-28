package domain;

/**
 * The rating of a point of interest (from 1 to 5)
 * @author Jasmin Wellnitz
 *
 */
public enum Rating {
	_1(1),_2(2),_3(3),_4(4),_5(5), INVALID(-1);
	
	private int value;

	Rating(int value){
		this.value = value;
	}
	
	/**
	 * Gets the numeric representation of the enum
	 * @return the numeric representation of the enum
	 */
	public int getValue(){
		return value;
	}
	
	/**
	 * Converts a numeric value into the rating. An invalid value is returned if the entered integer has no corresponding enum value.
	 * @param value the numeric representation of the enum
	 * @return the corresponding Rating
	 */
	public static Rating valueOf(int value){
		if(value == 1){
			return Rating._1;
		}else if(value == 2){
			return Rating._2;
		}else if(value == 3){
			return Rating._3;
		}else if(value == 4){
			return Rating._4;
		}else if(value ==5){
			return Rating._5;
		}
		else
			return Rating.INVALID;
		}
}

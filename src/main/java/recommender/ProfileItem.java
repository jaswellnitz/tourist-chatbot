package recommender;

import java.io.Serializable;

/**
 * Interface that marks items that possess a POIProfile
 * @author Jasmin Wellnitz
 *
 */
public interface ProfileItem extends Serializable {
	
	/**
	 * Returns the identifier
	 * @return identifier
	 */
	long getId();
	
	/**
	 * Returns the POIProfile
	 * @return  POIProfile
	 */
	POIProfile getProfile();

}

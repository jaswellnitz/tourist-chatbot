package recommender;

import java.io.Serializable;

public interface ProfileItem extends Serializable {
	
	long getId();
	
	POIProfile getProfile();

}

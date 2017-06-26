package recommender;

import java.util.Collection;
import java.util.Iterator;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.similarity.PreferenceInferrer;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class ProfileSimilarity implements UserSimilarity {

	private DataModel dataModel;

	/**
	 * Creates new {@link ProfileSimilarity}
	 * 
	 * @param dataModel
	 */
	public ProfileSimilarity(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	@Override
	public double userSimilarity(long userID1, long userID2) throws TasteException {
		PreferenceArray preferencesFromUser1 = dataModel.getPreferencesFromUser(userID1);
		PreferenceArray preferencesFromUser2 = dataModel.getPreferencesFromUser(userID2);
		assert preferencesFromUser1.length() == preferencesFromUser2.length();
		double similarity = 0.0;
		Iterator<Preference> iterator = preferencesFromUser1.iterator();
		Iterator<Preference> iterator2 = preferencesFromUser2.iterator();
		while (iterator.hasNext()) {
			Preference pref1 = iterator.next();
			Preference pref2 = iterator2.next();
			int pref1Value = (int)pref1.getValue();
			int pref2Value = (int)pref2.getValue();
			if (pref1Value != recommender.Preference.NOT_RATED.getValue()
					&& pref2Value != recommender.Preference.NOT_RATED.getValue()) {
				if (pref1Value== recommender.Preference.TRUE.getValue() && pref1Value == pref2Value) {
					similarity += 0.3;
				} else if (pref1Value == recommender.Preference.FALSE.getValue()
						&& pref1Value== pref2Value) {
					similarity += 0.05;
				}
			}
		}
		return similarity;
	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPreferenceInferrer(PreferenceInferrer inferrer) {
		throw new UnsupportedOperationException();
	}
}
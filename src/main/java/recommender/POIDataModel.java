package recommender;

import java.util.Collection;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.AbstractDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import model.POIProfile;
import model.ProfileItem;

// TODO: implement all methods and constructors
public class POIDataModel extends AbstractDataModel implements DataModel {


	/**
	 * 
	 */
	private static final long serialVersionUID = -8542663924097410194L;
	private final List<ProfileItem> profileItems;
	private DataModel delegate;

	public POIDataModel(List<ProfileItem> profItems){
		this.profileItems = profItems;

		processFile(profileItems);

	}

	private void processFile(List<ProfileItem> profileItems) {

		FastByIDMap<PreferenceArray> data = new FastByIDMap<>();
		for (ProfileItem profileItem : profileItems) {
			POIProfile profile = profileItem.getProfile();
			long id = profileItem.getId();
			PreferenceArray newPrefs = new GenericUserPreferenceArray(POIProfile.CATEGORY_COUNT);
			List<model.Preference> categories = profile.getAllCategories();
			for (int i = 0; i < categories.size(); i++) {
					int value = categories.get(i).getValue();
					newPrefs.setValue(i, value);
					Preference pref = new GenericPreference(id, i, value);
					newPrefs.set(i, pref);
			}
			data.put(id, newPrefs);
		}
		delegate = new GenericDataModel(data);
	}

	@Override
	public PreferenceArray getPreferencesFromUser(long userID) throws TasteException {
		return delegate.getPreferencesFromUser(userID);
	}

	@Override
	public FastIDSet getItemIDsFromUser(long userID) throws TasteException {
		return delegate.getItemIDsFromUser(userID);
	}

	@Override
	public LongPrimitiveIterator getUserIDs() throws TasteException {
		return delegate.getUserIDs();
	}

	@Override
	public LongPrimitiveIterator getItemIDs() throws TasteException {
		return delegate.getItemIDs();
	}

	@Override
	public PreferenceArray getPreferencesForItem(long itemID) throws TasteException {
		return delegate.getPreferencesForItem(itemID);
	}

	@Override
	public Float getPreferenceValue(long userID, long itemID) throws TasteException {
		return delegate.getPreferenceValue(userID, itemID);
	}

	@Override
	public Long getPreferenceTime(long userID, long itemID) throws TasteException {
		return delegate.getPreferenceTime(userID, itemID);
	}

	@Override
	public int getNumItems() throws TasteException {
		return delegate.getNumItems();
	}

	@Override
	public int getNumUsers() throws TasteException {
		return delegate.getNumUsers();
	}

	@Override
	public int getNumUsersWithPreferenceFor(long itemID) throws TasteException {
		return delegate.getNumUsersWithPreferenceFor(itemID);
	}

	@Override
	public int getNumUsersWithPreferenceFor(long itemID1, long itemID2) throws TasteException {
		return delegate.getNumUsersWithPreferenceFor(itemID1, itemID2);
	}

	@Override
	public void setPreference(long userID, long itemID, float value) throws TasteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removePreference(long userID, long itemID) throws TasteException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasPreferenceValues() {
		return true;
	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		// TODO Auto-generated method stub
	}

}

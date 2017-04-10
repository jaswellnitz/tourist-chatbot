package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.AbstractDataModel;
import org.apache.mahout.cf.taste.impl.model.BooleanPreference;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericItemPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.common.iterator.FileLineIterator;

// MOCKUP: Approach 1 - convert csv file to data model

// TODO: check if subclass of FileDataModel
// TODO: implement all methods and constructors
@ Deprecated
public class POICSVDataModel extends AbstractDataModel implements DataModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final File dataFile;
	private static final char COMMENT_CHAR = '#';
	private static final char[] DELIMITERS = { ',', '\t' };
	private DataModel delegate;

	public POICSVDataModel(File file) throws IOException {
		if (file.getAbsoluteFile() == null || !file.exists() || file.isDirectory()) {
			throw new FileNotFoundException(file.toString());
		}
		this.dataFile = file;

		processFile(file);

	}

	private void processFile(File file) throws IOException {

		FileLineIterator iterator = new FileLineIterator(dataFile, false);
		String line;
		FastByIDMap<PreferenceArray> data = new FastByIDMap<>();
		do {
			line = iterator.next();
			if (line.isEmpty() || line.charAt(0) == COMMENT_CHAR) {
				continue;
			}
			String[] split = line.split(",");
			int id = Integer.valueOf(split[0]);
			PreferenceArray newPrefs = new GenericUserPreferenceArray(split.length - 1);
			for (int i = 1; i < split.length; i++) {
				Integer categoryValue = Integer.valueOf(split[i]);
				newPrefs.setValue(i - 1, categoryValue);
				Preference pref = new GenericPreference(id, i-1, categoryValue);
				newPrefs.set(i-1, pref);

			}
			data.put(id, newPrefs);
		} while (iterator.hasNext());
		iterator.close();
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

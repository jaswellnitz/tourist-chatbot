package model;

import java.util.Collection;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

public class POISimilarity implements ItemSimilarity{
	
	private DataModel dm;
	
	public POISimilarity(DataModel dm) {
		this.dm = dm;
	}
	@Override
	public void refresh(Collection<Refreshable> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long[] allSimilarItemIDs(long arg0) throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] itemSimilarities(long arg0, long[] arg1) throws TasteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double itemSimilarity(long arg0, long arg1) throws TasteException {
		// TODO Auto-generated method stub
		return 0;
	}

}

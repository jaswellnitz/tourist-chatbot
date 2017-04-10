package recommender;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import data_acces.PointConverter;
import model.POIDataModel;
import model.PointOfInterest;

public class Recommender {

	public static void main(String... args) throws Exception {
		PointConverter pointConverter = new PointConverter();
		List<PointOfInterest> pois =  pointConverter.getPOIInRadius("9.991636", "53.550090");
		DataModel dm = new POIDataModel(pois);
		long id = dm.getUserIDs().next();
		UserSimilarity similarity = new MySimilarity(dm);
		UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.5, similarity, dm);
		UserBasedRecommender rec = new GenericUserBasedRecommender(dm,neighborhood,similarity);
		List<Long> mostSimilarUserIDs = LongStream.of(rec.mostSimilarUserIDs(id, 5)).boxed().collect(Collectors.toList());
		System.out.println("Ids similar to " + id + " :" + mostSimilarUserIDs);
		
		List<PointOfInterest> result = pois.stream().filter(t-> mostSimilarUserIDs.contains(t.getId())).collect(Collectors.toList());
		System.out.println(result);
	}
}


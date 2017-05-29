package poiRecommendation;

import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderBuilder;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.SpearmanCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.apache.mahout.common.RandomUtils;

import dataAccess.RatingDB;

public class RecommenderEvaluation {

	private static int EUCLIDEAN_SIM = 0;
	private static int PEARSON_SIM = 1;
	private static int LOGLIKELIHOOD_SIM = 2;
	private static int SPEARMAN_SIM = 3;

	private static int THRESHOLD_NEIGHBORHOOD = 0;
	private static int NEAREST_2_NEIGHBORHOOD = 1;
	private static int NEAREST_5_NEIGHBORHOOD = 2;
	private static int NEAREST_10_NEIGHBORHOOD = 3;
	private static int COUNT = 4;

	public static void main(String... args) throws TasteException {
		evaluateCollaborative();
	}

	private static void evaluateCollaborative() throws TasteException {
		RandomUtils.useTestSeed();
		RatingDB ratingDB = new RatingDB(System.getenv("DATABASE_URL"));
		DataModel model = new PostgreSQLJDBCDataModel(ratingDB.getDataSource(), "ratings", "userId", "pointId",
				"ratings", null);
		double bestFMeasure = 0.0;
		double prec = 0.0;
		double rec = 0.0;
		String combo = "";
		for(int i = 0; i < COUNT; i++){
			for(int j = 0; j < COUNT; j++){
				RecommenderIRStatsEvaluator evaluator = new GenericRecommenderIRStatsEvaluator();
				RecommenderBuilder recommenderBuilder = getRecommenderBuilder(i, j);
				IRStatistics irStatistics = evaluator.evaluate(recommenderBuilder, null, model, null, 1,
						GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD, 1.0);
				double fMeasure = irStatistics.getFNMeasure(0.75);
				if(fMeasure  > bestFMeasure) {
					bestFMeasure = fMeasure;
					prec = irStatistics.getPrecision();
					rec = irStatistics.getRecall();
					combo = i+","+j;
				}
			}
				
		}
		System.out.println("BEST COMBINATION:" + combo);
		System.out.println("Precision:"+prec);
		System.out.println("Recall:"+rec);
		System.out.println("F-Measure:"+bestFMeasure);
	}

	private static RecommenderBuilder getRecommenderBuilder(final int sim, final int nhood) {
		return new RecommenderBuilder() {

			@Override
			public Recommender buildRecommender(DataModel dataModel) throws TasteException {
				UserSimilarity similarity;
				if (sim == EUCLIDEAN_SIM) {
					similarity = new EuclideanDistanceSimilarity(dataModel);
				} else if (sim == PEARSON_SIM) {
					similarity = new PearsonCorrelationSimilarity(dataModel);
				} else if (sim == LOGLIKELIHOOD_SIM) {
					similarity = new LogLikelihoodSimilarity(dataModel);
				}else{
					similarity = new SpearmanCorrelationSimilarity(dataModel);
				}
				UserNeighborhood neighborhood;
				if(nhood == THRESHOLD_NEIGHBORHOOD){
					neighborhood = new ThresholdUserNeighborhood(0.1, similarity, dataModel);
				}else if(nhood == NEAREST_2_NEIGHBORHOOD){
					neighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);
				}else if(nhood == NEAREST_5_NEIGHBORHOOD){
					neighborhood = new NearestNUserNeighborhood(5, similarity, dataModel);
				}else{
					neighborhood = new NearestNUserNeighborhood(10, similarity, dataModel);
				}

				return new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
			}
		};
	}
}

import java.math.BigDecimal;
import java.util.HashSet;

public class SimilarityUtil {

	public static double euclideanDistance(double[] vectorA, double[] vectorB) {
		double sum = 0.0;

		for (int i = 0; i < vectorA.length; i++) {

			sum += Math.pow(vectorA[i]-vectorB[i], 2);

		}   

		return Math.sqrt(sum);
	} 

	public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
		//		System.out.println(Arrays.toString(vectorB));
		//	System.out.println("VECTOR B");
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;

		for (int i = 0; i < vectorA.length; i++) {


			dotProduct += vectorA[i] * vectorB[i];

			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);

		}   
		if(normA==0 || normB==0 ||(Math.sqrt(normA) * Math.sqrt(normB)==0)){
			return 0;
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	} 


	public static double jaccardSimilarity(double[] vectorA, double[] vectorB, int roundingVal) {
		for(int i=0;i<vectorA.length;i++){
			try{vectorA[i]=round(vectorA[i],roundingVal);
			vectorB[i]=round(vectorB[i],roundingVal);
			}catch(Exception e){

			}
		}



		if (unionArrays(vectorA,vectorB).length==1 && intersection(vectorA,vectorB).length==1 ) return 0;
		double val = (double) (double) intersection(vectorA,vectorB).length/(double) unionArrays(vectorA,vectorB).length;
		/*if(val==1){
			System.out.println(val + "JACCARD IS 1");
			System.out.println(unionArrays(vectorA,vectorB).length + " UNION LENGTH");
			System.out.println(intersection(vectorA,vectorB).length + " interesection LENGTH");
		} */

		return (double) (double) intersection(vectorA,vectorB).length/(double) unionArrays(vectorA,vectorB).length;
		/*Set<Double> setA = new HashSet<Double>();
		Set<Double> setB = new HashSet<Double>();
		Double[] newA= new Double[vectorA.length];
		Double[] newB = new Double[vectorB.length];
		for(int i=0;i<vectorA.length;i++){
			newA[i]=(Double) vectorA[i];
			newB[i]=(Double) vectorB[i];

		}
		Collections.addAll(setA, newA);
		Collections.addAll(setB, newB);

		Set<Double> intesersection_cardinality = new HashSet<Double>(setA);
		intesersection_cardinality.retainAll(setB);

		Double intesersection_cardinalityLength = (double) intesersection_cardinality.size();

		Set<Double> union_cardinality = new HashSet<Double>(setA);
		union_cardinality.addAll(setB);

		Double union_cardinalityLength = (double) union_cardinality.size();
		if(union_cardinalityLength==0) return 0;
		return intesersection_cardinalityLength/union_cardinalityLength; */

	} 

	public static double[] unionArrays(double[]... arrays)
	{
		int maxSize = 0;
		int counter = 0;

		for(double[] array : arrays) maxSize += array.length;
		double[] accumulator = new double[maxSize];

		for(double[] array : arrays)
			for(double i : array)
				if(!isDuplicated(accumulator, counter, i))
					accumulator[counter++] = i;

		double[] result = new double[counter];
		for(int i = 0; i < counter; i++) result[i] = accumulator[i];

		return result;
	}

	public static boolean isDuplicated(double[] array, double counter, double value)
	{
		for(int i = 0; i < counter; i++) if(array[i] == value) return true;
		return false;
	}

	public static double[] intersection(double[] nums1, double[] nums2) {
		HashSet<Double> set1 = new HashSet<Double>();
		for(double i: nums1){
			set1.add(i);
		}

		HashSet<Double> set2 = new HashSet<Double>();
		for(double i: nums2){
			if(set1.contains(i)){
				set2.add(i);
			}
		}

		double[] result = new double[set2.size()];
		int i=0;
		for(double n: set2){
			result[i++] = n;
		}

		return result;
	}
	public static double manhattanSimilarity(double[] vectorA, double[] vectorB) {
		double sum = 0.0;
		for (int i = 0; i < vectorA.length; i++) {

			sum += Math.abs(vectorA[i]-vectorB[i]);

		}   

		return sum;
	} 

	public static double minkowskiSimilarity(double[] vectorA, double[] vectorB, double p_value) {
		//		System.out.println(Arrays.toString(vectorB));
		//	System.out.println("VECTOR B");

		double sum=0.0;
		for (int i = 0; i < vectorA.length; i++) {
			sum+=Math.pow(Math.abs(vectorA[i]-vectorB[i]), p_value);
		}   
		return nthRoot(sum, p_value);

	} 


	private static double nthRoot(double value, double n_root){
		float root_Value= 1/(float) n_root;
		return round(value*root_Value, 3);
		//root_value = 1/float(n_root);
		//return round (Decimal(value) ** Decimal(root_value),3);
	}
	private static double round(double value, int numDigs){
		BigDecimal bigDecimal = new BigDecimal(value);
		bigDecimal = bigDecimal.setScale(numDigs,
				BigDecimal.ROUND_HALF_UP);
		return bigDecimal.doubleValue();
	}}

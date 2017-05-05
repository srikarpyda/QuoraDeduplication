import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.plaf.synth.SynthSeparatorUI;

public class FeatureExtractionUtil{

	// BEGIN PLAN
	/* Take question
	 * Map each word to cluster
	 * For each sentence, take top three words for each cluster
	 * Comparing similar clusters for Q1 and Q2, do 3x3 cosine similarity matching and make each into a feature
	 * Do for all clusters
	 * final features:
	 * 
	 * cluster 1            cluster 2           cluster 3
	 * f1.....f9			f1.....f9			f1.....f9
	 */
	// END PLAN

	// BEGIN TODO
	/*
	 * 1. Exception handling: 
	 * 	a. word is not in cluster.txt (is not assigned cluster)--currently auto-assigned to cluster 0
	 *		solution: auto-assign to 0, randomly assign to one of 10 clusters, or designate it for waste
	 *  b. word is not in the wv.txt (its not assigned vector--currently auto-assigned <0...0>
	 *  	solution: word is automatically assigned vector: <0...0>
	 *  c. indexing of kw is messed up (there are not 10 words for some questions but only question 1s) 
	 *  	solution: check for id of q1+1 in the question array once it is split by commas
	 *  	problem: what if the keyword is the same as the id
	 *  
	 * 2. Need to check whether cosine similarity is being calculated properly--taken from stack overflow 
	 * 
	 * 3. Hard-coded values:
	 *	a. Directories
	 *	b. number of Clusters
	 *	c. figure out why 00.00 double array not being initialized in the while loop which checks the size of the array list?? this took a while 
	 */
	// END TODO

	public static void main(String[] args) throws IOException {
		/*
		 * Similarity Measure String Values:
		 * 	"cosinesimilarity"
		 * 	"euclideandistance"
		 * 	"jaccardsimilarity"
		 * 	"minkowskidistance"
		 * 	"manhattandistance"
		 * 	
		 * Feature Format String Values:
		 * 	"allfeatures"
		 * 	"averagefeatures"
		 * 	"topfeature"
		 * 	"top3features"
		 * 	"averagetop3features"
		 * 
		 * 
		 */
		String similarityMeasure="cosinesimilarity2";
		String featureFormat="allfeatures";
		int numClusters=10;

		String clusterDir="/Users/srikarpyda/Documents/Quora/cluster.txt";
		Map<String,Integer> clusterMap=wordToCluster(clusterDir);

		String vectorDir="/Users/srikarpyda/Documents/Quora/wv.txt";
		Map<String,double[]> vectorMap = wordToVector(vectorDir);

		FileWriter pw = new FileWriter(new File("/Users/srikarpyda/Documents/Quora/featureExtraction" + similarityMeasure +featureFormat+".csv"),true);

		StringBuilder sb= new StringBuilder();
		pw.write(sb.toString());
		//pw.close();

		int pairIdx=0;

		Scanner scanner;

		try {
			scanner = new Scanner(new File("/Users/srikarpyda/Documents/Quora/quora_duplicate_questions.tsv"));
			String nextLine=scanner.nextLine();

			while(scanner.hasNextLine() ){	
				sb= new StringBuilder();


				System.out.println("PAIR IDX : " +pairIdx);

				pairIdx++;
				nextLine = scanner.nextLine();
				//System.out.println(nextLine);
				String[] splitLine = nextLine.split("	");
				//	System.out.println(splitLine.length);
				if(splitLine.length<6) continue;

				String ques1 = splitLine[3];
				String ques2 = splitLine[4];
				ques1 = ques1.toLowerCase();
				ques2 = ques2.toLowerCase();

				ques1= ques1.replaceAll("[^A-Za-z0-9\\s\\(\\)\\-]", "");

				ques2= ques2.replaceAll("[^A-Za-z0-9\\s\\(\\)\\-]", "");


				String[] words1 = ques1.split(" ");

				String endword= words1[words1.length-1];


				String[] words2 = ques2.split(" ");
				double[] q1Vec = new double[100];
				double[] q2Vec = new double[100];
				for(int i=0;i<100;i++){
					double agg1=0;
					double agg2=0;
					int skip1=0;
					int skip2=0;
					for(int j=0; j<words1.length;j++){
						try{
							agg1+=vectorMap.get(words1[j])[i];
						}catch(Exception e){
							try{
								agg1+=vectorMap.get(words1[j].toLowerCase())[i];
							}catch(Exception f){
								//	 System.out.println("VECTOR NOT FOUND: " + question1[j]);
								skip1++;
							}

						}
					}
					for(int k=0;k<words2.length;k++){
						try{
							agg2+=vectorMap.get(words2[k])[i];
						}catch(Exception e){
							try{
								agg2+=vectorMap.get(words2[k].toLowerCase())[i];
							}catch(Exception f){
								//	 System.out.println("VECTOR NOT FOUND: " + question2[k]);
								skip2++;
							}

						}
					}

					if(words1.length==0 || words1.length-skip1<=0){
						agg1=0;
					}else{
						agg1=agg1/(words1.length-skip1);

					}
					if(words2.length==0 || words2.length-skip1<=0){
						agg2=0;
					}else{
						agg2=agg2/(words2.length-skip2);

					}


					q1Vec[i]=agg1;
					q2Vec[i]=agg2;

				}


				double sim= 0;
				if(words1.length==0 && words1.length==0){
					sim=1;
				}else{
					sim = cosineSimilarity(q1Vec, q2Vec);
				}				
				sb.append(sim);
				sb.append(",");

				ArrayList<String> intList= Intersection.intersection(ques1, ques2);
				ArrayList<String> list1 = new ArrayList<>(Arrays.asList(words1));
				ArrayList<String> list2 = new ArrayList<>(Arrays.asList(words2));

				for(String s:intList){
					if(list1.contains(s)) list1.remove(s);
					if(list2.contains(s)) list2.remove(s);
				}

				String[] question1= list1.toArray(new String[list1.size()]);
				String[] question2=list2.toArray(new String[list2.size()]);


				int testIdx;
				try {
					testIdx = Integer.parseInt(splitLine[1]);
				} catch (NumberFormatException e) {
					//System.out.println("NOT AN INTEGER INDEX");
					continue;
				}				
				int q1=Integer.parseInt(splitLine[1]);

				int q2=q1+1;

				int duplicateBool=-1;
				//System.out.println(splitLine[splitLine.length-1] + " VAL");
				try{
					duplicateBool=Integer.parseInt(splitLine[splitLine.length-1]);		
				}catch(Exception e){
					continue;
				}

				Integer idx2=-1;

				for(int i=1;i<splitLine.length;i++){
					try{ 
						if(Integer.parseInt(splitLine[i])==q2){
							idx2=i+1;
							break;
						}

					}catch(Exception e){

					}
				}

				//problem with indices
				if(idx2==-1 || duplicateBool==-1){
					continue;
				}

				//String[] question1 = Arrays.copyOfRange(splitLine,1,idx2-1);
				//String[] question2 = Arrays.copyOfRange(splitLine,idx2,splitLine.length-1);

				//System.out.println("QESTION 2 " + question2.length);
				//	Map<Integer,double[]> featureMap= featureList(question1, question2,clusterMap,vectorMap, numClusters, similarityMeasure);

				sb=averageEuclidean(question1, question2, vectorMap,sb);



				sb.append(duplicateBool);
				sb.append("\n");
				pw.write(sb.toString());



			}


			//pw.write(sb.toString());	
			pw.close();
			scanner.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	public static StringBuilder averageEuclidean(String[] question1, String[] question2, Map<String,double[]> vectorMap, StringBuilder sb){
		double[] q1Vec = new double[100];
		double[] q2Vec = new double[100];
		for(int i=0;i<100;i++){
			double agg1=0;
			double agg2=0;
			int skip1=0;
			int skip2=0;
			for(int j=0; j<question1.length;j++){
				try{
					agg1+=vectorMap.get(question1[j])[i];
				}catch(Exception e){
					try{
						agg1+=vectorMap.get(question1[j].toLowerCase())[i];
					}catch(Exception f){
						//	 System.out.println("VECTOR NOT FOUND: " + question1[j]);
						skip1++;
					}

				}
			}
			for(int k=0;k<question2.length;k++){
				try{
					agg2+=vectorMap.get(question2[k])[i];
				}catch(Exception e){
					try{
						agg2+=vectorMap.get(question2[k].toLowerCase())[i];
					}catch(Exception f){
						//	 System.out.println("VECTOR NOT FOUND: " + question2[k]);
						skip2++;
					}

				}
			}

			if(question1.length==0 || question1.length-skip1<=0){
				agg1=0;
			}else{
				agg1=agg1/(question1.length-skip1);

			}
			if(question2.length==0 || question2.length-skip1<=0){
				agg2=0;
			}else{
				agg2=agg2/(question2.length-skip2);

			}


			q1Vec[i]=agg1;
			q2Vec[i]=agg2;

		}


		double sim= 0;
		if(question1.length==0 && question2.length==0){
			sim=1;
		}else{
			sim = cosineSimilarity(q1Vec, q2Vec);
		}
		double jaccard = jaccardSimilarity(q1Vec, q2Vec, 1);

		sb.append(sim);
		sb.append(",");
		sb.append(jaccard);
		sb.append(",");
		return sb;

	}


	public static Map<Integer,double[]> featureList(String[] question1, String[] question2, Map<String,Integer> clusterMap, Map<String,double[]>vectorMap, int numClusters, String similarityMeasure) throws IOException{

		Map<Integer,double[]> featureMap = new HashMap<Integer,double[]>();
		//Map<String,Integer> clusterMap= wordToCluster(clusterDir); //
		//Map<String,double[]>vectorMap = wordToVector(vectorDir); //

		int[] q1Cluster = questionToCluster(question1, clusterMap); // 
		int[] q2Cluster = questionToCluster(question2, clusterMap); //

		Map<Integer,ArrayList<String>> topWords1 = topWordsForCluster(q1Cluster, question1, numClusters);
		Map<Integer,ArrayList<String>> topWords2 = topWordsForCluster(q2Cluster, question2, numClusters);

		Map<Integer,ArrayList<double[]>> topVectors1 = topVectorForCluster(topWords1, clusterMap, vectorMap);
		Map<Integer,ArrayList<double[]>> topVectors2 = topVectorForCluster(topWords2, clusterMap, vectorMap);
		for(int i=0; i<numClusters;i++){
			//System.out.println(" CLUSTER NUM " +i);
			ArrayList<double[]> vectors1= new ArrayList<double[]>();
			ArrayList<double[]> vectors2= new ArrayList<double[]>();

			if(topVectors1.get(i)!=null){
				vectors1 = topVectors1.get(i);
			}

			while(vectors1.size()<3){
				double[] zeroArr = new double[100];
				Arrays.fill(zeroArr, 0.0);

				vectors1.add(zeroArr);
			}

			if(topVectors2.get(i)!=null){
				vectors2 = topVectors2.get(i);
			}

			while(vectors2.size()<3){
				double[] zeroArr = new double[100];
				Arrays.fill(zeroArr, 0.0);

				vectors2.add(zeroArr);
			}
			double[] features= featureArr(vectors1, vectors2, similarityMeasure);
			featureMap.put(i, features);
		}

		return featureMap;
	}

	public static double[] featureArr(ArrayList<double[]> vectors1, ArrayList<double[]> vectors2, String similarityMeasure){
		int featureIndx=0;
		int featureSize = vectors1.size()*vectors2.size();
		//System.out.println(featureSize +"feature" +vectors1.size() +"vecsizE" +vectors2.size());
		double[] featureArr= new double[featureSize];
		//double[] vector1 = new double[featureSize];
		//double[] vector2 = new double[featureSize];


		for(double[] vector1: vectors1){
			for(double[] vector2: vectors2){
				if(vector1==null){
					vector1 = new double[100];
					for(int i=0;i<vector1.length;i++){
						vector1[i]=0.0;
					}
				}

				if(vector2==null){
					vector2= new double[100];
					for(int i=0;i<vector2.length;i++){
						vector2[i]=0.0;
					}
				}

				//	System.out.println("NEW FUNCT 1"+Arrays.toString(vector1));
				//System.out.println(" NEW FUNCT 2 " + Arrays.toString(vector2));
				double similarity=0.0;
				if(similarityMeasure.equals("euclideandistance")) similarity = euclideanDistance(vector1, vector2);
				else if(similarityMeasure.equals("cosinesimilarity")) similarity =  cosineSimilarity(vector1, vector2);
				else if(similarityMeasure.equals("jaccardsimilarity")) similarity = jaccardSimilarity(vector1, vector2, 1);
				else if(similarityMeasure.equals("minkowskidistance")) similarity = minkowskiSimilarity(vector1, vector2, 1);
				else if(similarityMeasure.equals("manhattandistance")) similarity = manhattanSimilarity(vector1, vector2);
				else similarity = cosineSimilarity(vector1, vector2);

				featureArr[featureIndx] = similarity;

				featureIndx++;
			}
		}

		/*	//System.out.println(vectors1.size() + " " + vectors2.size());
		for(int i=0; i<vectors1.size(); i++){
			for(int j=0;j<vectors2.size();j++){
				System.out.println(vectors1.get(i).length +"Og");
				System.out.println(vectors2.get(i).length);
				if(vectors1.get(i)!=null){
					vector1= vectors1.get(i);
				}
				else{
					Arrays.fill(vector1, 00.00);
				}

				if(vectors2.get(j)!=null){
					vector2= vectors2.get(j);
					System.out.println(vectors2.get(j) + "vec2map");
				}
				else{
					Arrays.fill(vector2, 00.00);
				}
				System.out.println(vector1.length + "VEC1Size " + vector2.length);

				double similarity = cosineSimilarity(vector1, vector2);
				featureArr[featureIndx] = similarity;

				featureIndx++;
			}
		}*/

		return featureArr;
	}


	public static int[] questionToCluster(String[] question, Map<String,Integer> clusterMap){
		int[] clusterArr = new int[question.length];

		for(int i =0; i< question.length;i++){
			//System.out.println("WORDDD " + question[i]);

			if(clusterMap.containsKey(question[i])){
				clusterArr[i] = clusterMap.get(question[i]);
			}
			else{
				System.out.println(question[i] + " not in cluster file");

				clusterArr[i]=-1;

			}
		}

		return clusterArr;

	}

	public static Map<Integer,ArrayList<String>> topWordsForCluster(int[] clusterArr, String[] questionArr, int numClusters){
		Map<Integer,ArrayList<String>> topWordsMap = new HashMap<Integer,ArrayList<String>>();
		for(int i=0;i<numClusters;i++){
			if(!topWordsMap.containsKey(i)){
				topWordsMap.put(i, new ArrayList<String>());
			}
			ArrayList<String> topWords = topWordsMap.get(i);
			for(int j=0;j<questionArr.length;j++){
				if(topWords.size()>=3) break;
				if(clusterArr[j]==i && topWords.size()<3){
					topWords.add(questionArr[j]);
				}
			}

			//System.out.println("TOP WORDS SIZE : " + topWords.size());
			topWordsMap.put(i, topWords);
		}
		return topWordsMap;
		/* Map<Integer,ArrayList<String>> topWordsMap = new HashMap<Integer,ArrayList<String>>();
		for(int i =0 ; i <questionArr.length;i++){
			System.out.println(clusterArr.length + "cluster " +questionArr.length + " question");
			if(topWordsMap.containsKey(clusterArr[i])){
				ArrayList<String> val =topWordsMap.get(clusterArr[i]);
				if(val.size()<3){  
					val.add(questionArr[i]);
					topWordsMap.put(clusterArr[i], val);
				}
			}
			else{
				ArrayList<String> val = new ArrayList<String>();
				topWordsMap.put(clusterArr[i], val);
				System.out.println(topWordsMap.get(clusterArr[i]).size());

			}
		}
		return topWordsMap; */
	}

	public static Map<Integer,ArrayList<double[]>> topVectorForCluster(Map<Integer,ArrayList<String>> topWords, Map<String,Integer> clusterMap, Map<String,double[]> vectorMap){
		Map<Integer,ArrayList<double[]>> topVectors = new HashMap<Integer, ArrayList<double[]>>();
		for(Integer key:topWords.keySet()){
			ArrayList<String> words = topWords.get(key);

			ArrayList<double[]> vectors = new ArrayList<double[]>();
			if(topVectors.containsKey(key)){
				vectors=topVectors.get(key);
			}
			for(String word:words){

				//	System.out.println(word);
				if(vectors.size()<3)
					vectors.add(vectorMap.get(word));
			}
			topVectors.put(key,vectors);
		}
		//System.out.println("TOP SIZE " + topVectors.keySet().size());
		return topVectors;
	}


	public static Map<String,Integer> wordToCluster(String clusterDir){
		HashMap<String, Integer> clusterMap = new HashMap<String,Integer>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(clusterDir));
			String str;
			String[] clusterSplit;
			while ((str = in.readLine()) != null){
				clusterSplit= str.split(" ");
				String word = clusterSplit[0];
				Integer cluster = Integer.parseInt(clusterSplit[1]);
				//System.out.println("WORD : " + word + "CLUSTER : " + cluster);
				clusterMap.put(word, cluster);

			}	

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return clusterMap;
	}



	public static Map<String,double[]> wordToVector( String vectorDir) throws IOException{

		HashMap<String, double[]> vectorMap = new HashMap<String,double[]>();
		BufferedReader in;

		try {

			in = new BufferedReader(new FileReader(vectorDir));
			String str=in.readLine();
			//int i=0;


			while((str = in.readLine()) != null){
				String nextLine = str;
				//System.out.println("line index: "+ i + " \n " + nextLine +"\n");
				//i++;

				String[] splitLine = nextLine.split(" ");
				String word = splitLine[0];

				String[] vectorStringArr = Arrays.copyOfRange(splitLine,1,splitLine.length);
				double[] vectorDoubleArr = strToDoubleArr(vectorStringArr);

				vectorMap.put(word, vectorDoubleArr);
			}

			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return vectorMap;
	}

	public static double[] strToDoubleArr(String[] arr){
		double[] doubleArr = new double[arr.length];

		for(int i=0; i<arr.length;i++){

			doubleArr[i] = Double.parseDouble(arr[i]);
		}

		return doubleArr;
	}

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
	}

}

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.plaf.synth.SynthSeparatorUI;

public class ClusterRankingAverageTop3Features{

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
		int numClusters=10;

		String clusterDir="/Users/srikarpyda/Documents/Quora/cluster.txt";
		Map<String,Integer> clusterMap=wordToCluster(clusterDir);

		String vectorDir="/Users/srikarpyda/Documents/Quora/wv.csv";
		Map<String,double[]> vectorMap = wordToVector(vectorDir);

		FileWriter pw = new FileWriter(new File("/Users/srikarpyda/Documents/Quora/featureExtractionEuclideanSimAverageTop3Features.csv"),true);

		StringBuilder sb= new StringBuilder();
		sb.append("PairIndex,Question1,Question2,Cluster0,Cluster1,Cluster2,Cluster3,Cluster4,Cluster5,Cluster6,Cluster7,Cluster8,Cluster9,DuplicateBoolean\n");
		pw.write(sb.toString());
		//pw.close();

		int pairIdx=0;

		Scanner scanner;

		try {
			scanner = new Scanner(new File("/Users/srikarpyda/Documents/Quora/kw.csv"));

			while(scanner.hasNextLine()){	

				sb= new StringBuilder();
				sb.append(pairIdx);
				sb.append(",");

				System.out.println("PAIR : " +pairIdx);

				pairIdx++;
				String nextLine = scanner.nextLine();


				String[] splitLine = nextLine.split(",");
				int testIdx;
				try {
					testIdx = Integer.parseInt(splitLine[0]);
				} catch (NumberFormatException e) {
					//System.out.println("NOT AN INTEGER INDEX");
					continue;
				}				
				int q1=Integer.parseInt(splitLine[0]);
				sb.append(q1);
				sb.append(",");

				int q2=q1+1;
				sb.append(q2);
				sb.append(",");


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

				String[] question1 = Arrays.copyOfRange(splitLine,1,idx2-1);
				String[] question2 = Arrays.copyOfRange(splitLine,idx2,splitLine.length-1);
				//System.out.println("QESTION 2 " + question2.length);
				Map<Integer,double[]> featureMap= featureList(question1, question2,clusterMap,vectorMap, numClusters);

				//System.out.println("KEYSET " + featureMap.keySet().size());
				for(Integer cluster: featureMap.keySet()){
					double[] featureArr = featureMap.get(cluster);
					Arrays.sort(featureArr);
					double top3Average = (featureArr[featureArr.length-1] + featureArr[featureArr.length-2]+ featureArr[featureArr.length-3])/3;
					sb.append(top3Average);
					sb.append(",");

					/*for(int i=0;i<featureArr.length;i++){
						//System.out.println(" CLUSTER : " + cluster + " size of featureArr: " + featureArr.length);
						sb.append(featureArr[i]);
						sb.append(",");
						//pw.write(Double.toString(featureArr[i]));
						//pw.write(",");
					}*/

				}
				sb.append(duplicateBool);
				sb.append("\n");
				pw.write(sb.toString());
				//pw.write(duplicateBool);
				//System.out.println("SIZE " + sb.toString().length() + " STRING " + sb.toString());
				//pw.write("\n");


				//pw = new FileWriter("/Users/srikarpyda/Documents/Quora/featureExtraction.csv",true);

				//System.out.println("******************************************************* \n");	
				//System.out.println("done writing feature extraction for question pair!!!");
				//System.out.println("******************************************************* \n");


			}


			//pw.write(sb.toString());	
			pw.close();
			scanner.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}


	public static Map<Integer,double[]> featureList(String[] question1, String[] question2, Map<String,Integer> clusterMap, Map<String,double[]>vectorMap, int numClusters) throws IOException{

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
			double[] features= featureArr(vectors1, vectors2);
			featureMap.put(i, features);
		}

		return featureMap;
	}

	public static double[] featureArr(ArrayList<double[]> vectors1, ArrayList<double[]> vectors2){
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
				double similarity = euclideanDistance(vector1, vector2);
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
		System.out.println("QUESTIOn " + Arrays.toString(question));
		int[] clusterArr = new int[question.length];

		for(int i =0; i< question.length;i++){
			//System.out.println("WORDDD " + question[i]);

			if(clusterMap.containsKey(question[i])){
				clusterArr[i] = clusterMap.get(question[i]);
			}
			else{
				System.out.println(question[i] + " not in cluster file");

				clusterArr[i]=0;
				//unmappedWords++;
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
			String str;
			//int i=0;


			while((str = in.readLine()) != null){
				String nextLine = str;
				//System.out.println("line index: "+ i + " \n " + nextLine +"\n");
				//i++;

				String[] splitLine = nextLine.split(",");
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

}

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

public class ClusterRanking {

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
		FileWriter pw = new FileWriter(new File("/Users/srikarpyda/Documents/Quora/featureExtraction.csv"),true);
		//pw.write("PairIndex,Question1,Question2\n");
		StringBuilder sb= new StringBuilder();
		sb.append("PairIndex,Question1,Question2\n");

		//System.out.println(sb);
		pw.write(sb.toString());
		//pw.close();

		String clusterDir="/Users/srikarpyda/Documents/Quora/cluster.txt";
		String vectorDir="/Users/srikarpyda/Documents/Quora/wv.csv";

		int numClusters=10;
		int pairIdx=0;

		Scanner scanner;

		try {
			scanner = new Scanner(new File("/Users/srikarpyda/Documents/Quora/kw.csv"));

			while(scanner.hasNextLine()){		
				sb= new StringBuilder();
				sb.append(pairIdx);
				sb.append(",");
				pairIdx++;
				System.out.println("PAIR NUMBER : " + pairIdx);
				String nextLine = scanner.nextLine();

			
				String[] splitLine = nextLine.split(",");

				int q1=Integer.parseInt(splitLine[0]);
				sb.append(q1);
				sb.append(",");

				int q2=q1+1;
				sb.append(q2);
				sb.append(",");

				int duplicateBool=-1;
				duplicateBool=Integer.parseInt(splitLine[splitLine.length-1]);		

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

				
				Map<Integer,double[]> featureMap= featureList(question1, question2,wordToCluster(clusterDir), wordToVector(vectorDir), numClusters);
				
				//System.out.println("KEYSET " + featureMap.keySet().size());
				for(Integer cluster: featureMap.keySet()){
					double[] featureArr = featureMap.get(cluster);
					for(int i=0;i<featureArr.length;i++){
						//	System.out.println(" CLUSTER : " + cluster + " size of featureArr: " + featureArr.length);
						sb.append(featureArr[i]);
						sb.append(",");
						//pw.write(Double.toString(featureArr[i]));
						//pw.write(",");
					}

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

		Map<Integer,ArrayList<String>> topWords1 = topWordsForCluster(q1Cluster, question1);
		Map<Integer,ArrayList<String>> topWords2 = topWordsForCluster(q2Cluster, question2);

		Map<Integer,ArrayList<double[]>> topVectors1 = topVectorForCluster(topWords1, vectorMap);
		Map<Integer,ArrayList<double[]>> topVectors2 = topVectorForCluster(topWords2, vectorMap);

		for(int i=0; i<numClusters;i++){

			ArrayList<double[]> vectors1= new ArrayList<double[]>();
			ArrayList<double[]> vectors2= new ArrayList<double[]>();

			if(topVectors1.get(i)!=null){
				vectors1 = topVectors1.get(i);
			}

			while(vectors1.size()<3){
				double[] zeroArr = new double[100];
				Arrays.fill(zeroArr, 00.00);

				vectors1.add(zeroArr);
			}

			if(topVectors2.get(i)!=null){
				vectors2 = topVectors2.get(i);
			}

			while(vectors2.size()<3){
				double[] zeroArr = new double[100];
				Arrays.fill(zeroArr, 00.00);

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

		double[] featureArr= new double[featureSize];
		double[] vector1 = new double[featureSize];
		double[] vector2 = new double[featureSize];

		//System.out.println(vectors1.size() + " " + vectors2.size());
		for(int i=0; i<vectors1.size(); i++){
			for(int j=0;j<vectors2.size();j++){

				if(vectors1.get(i)!=null){
					vector1= vectors1.get(i);
				}
				else{
					Arrays.fill(vector1, 00.00);
				}

				if(vectors2.get(j)!=null){
					vector2= vectors2.get(j);
				}
				else{
					Arrays.fill(vector2, 00.00);
				}

				//System.out.println(vector1.length + " " + vector2.length);
				//System.out.println(Arrays.toString(vector1));
				//System.out.println(Arrays.toString(vector2));

				double similarity = cosineSimilarity(vector1, vector2);
				featureArr[featureIndx] = similarity;

				featureIndx++;
			}
		}

		return featureArr;
	}


	public static int[] questionToCluster(String[] question, Map<String,Integer> clusterMap){
		int[] clusterArr = new int[question.length];
		//int unmappedWords=0;
		for(int i =0; i< question.length;i++){
			//System.out.println("WORDDD " + question[i]);

			if(clusterMap.containsKey(question[i])){
				clusterArr[i] = clusterMap.get(question[i]);
			}
			else{
				// need to resolve policy

				/*	System.out.println("******************************************************* \n");
				System.out.println("This Word Has Been Automatically Mapped to Cluster 0 Because It is Not a Key Within the Cluster.txt File");
				System.out.println("******************************************************* \n");
				 */
				clusterArr[i]=0;
				//unmappedWords++;
			}
		}

		/*System.out.println("******************************************************* \n");
		System.out.println("Question: " + Arrays.toString(question) +  "Cluster: " + Arrays.toString(clusterArr)+ " Number of Unmapped KeyWords: " + unmappedWords);
		System.out.println("******************************************************* \n");
		 */
		return clusterArr;

	}

	public static Map<Integer,ArrayList<String>> topWordsForCluster(int[] clusterArr, String[] questionArr){
		Map<Integer,ArrayList<String>> topWordsMap = new HashMap<Integer,ArrayList<String>>();
		for(int i =0 ; i <questionArr.length;i++){
			if(topWordsMap.containsKey(clusterArr[i])){
				ArrayList<String> val =topWordsMap.get(clusterArr[i]);
				if(val.size()<3){  
					val.add(questionArr[i]);
					topWordsMap.put(clusterArr[i], val);
				}
			}
			else{
				ArrayList<String> val = new ArrayList<String>();
				val.add(questionArr[i]);
				topWordsMap.put(clusterArr[i], val);
			}
		}
		return topWordsMap;
	}

	public static Map<Integer,ArrayList<double[]>> topVectorForCluster(Map<Integer,ArrayList<String>> topWords, Map<String,double[]> vectorMap){
		Map<Integer,ArrayList<double[]>> topVectors = new HashMap<Integer,ArrayList<double[]>>();
		for(Integer key:topWords.keySet()){
			ArrayList<String> words = topWords.get(key);
			ArrayList<double[]> vectors = new ArrayList<double[]>();
			for(String word:words){
				vectors.add(vectorMap.get(word));
			}
			topVectors.put(key,vectors);
		}
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

	public static double cosineSimilarity(double[] vectorA, double[] vectorB) {

		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;

		for (int i = 0; i < vectorA.length; i++) {

			try{

				dotProduct += vectorA[i] * vectorB[i];

				normA += Math.pow(vectorA[i], 2);
				normB += Math.pow(vectorB[i], 2);
			}catch(Exception e){
				dotProduct += vectorA[i] * 0.0;

				normA += Math.pow(vectorA[i], 2);
				normB += Math.pow(0.0, 2);
			}
		}   

		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	} 

}

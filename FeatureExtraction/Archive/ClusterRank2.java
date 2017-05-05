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

public class ClusterRank2 {



	public static void main(String[] args) throws IOException {


		String clusterDir="/Users/srikarpyda/Documents/Quora/cluster.txt";
		String vectorDir="/Users/srikarpyda/Documents/Quora/wv.csv";

		int numClusters=10;
		int pairIdx=0;
		
		
		StringBuilder sb= new StringBuilder();
		sb.append("PairIndex,Question1,Question2\n");

		Scanner scanner;

		try {
			scanner = new Scanner(new File("/Users/srikarpyda/Documents/Quora/kw.csv"));

			while(scanner.hasNextLine()){				
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
				
				sb = featureList(question1, question2, clusterDir, vectorDir, numClusters, sb);
			
				sb.append(duplicateBool);
				//System.out.println(sb);
				sb.append("\n");
			}


			FileWriter pw = new FileWriter(new File("/Users/srikarpyda/Documents/Quora/featureExtraction.csv"),true);
			pw.write(sb.toString());	
			pw.close();
			scanner.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}


	public static StringBuilder featureList(String[] question1, String[] question2, String clusterDir, String vectorDir, int numClusters, StringBuilder sb) throws IOException{

		Map<String,Integer> clusterMap= wordToCluster(clusterDir); //
		Map<String,double[]>vectorMap = wordToVector(vectorDir); //

		int[] q1Cluster = questionToCluster(question1, clusterMap); // 
		int[] q2Cluster = questionToCluster(question2, clusterMap); //

		for(int i=0;i<numClusters;i++){
			ArrayList<double[]> vectorList1= new ArrayList<double[]>();
			ArrayList<double[]> vectorList2= new ArrayList<double[]>();

			for(int j=0;j<q1Cluster.length; j++){

				if(q1Cluster[j]==i&& vectorList1.size()<3){
					String word = question1[j];
					double[] vec = vectorMap.get(word);
					vectorList1.add(vec);
				}
			}

			while(vectorList1.size()<3){
				double[] zeroArr = new double[100];
				Arrays.fill(zeroArr, 00.00);
				vectorList1.add(zeroArr);
			}

			for(int k=0;k<q2Cluster.length; k++){

				if(q2Cluster[k]==i && vectorList2.size()<3){
					String word = question2[k];
					double[] vec = vectorMap.get(word);
					vectorList2.add(vec);
				}
			}

			while(vectorList2.size()<3){
				double[] zeroArr = new double[100];
				Arrays.fill(zeroArr, 00.00);
				vectorList2.add(zeroArr);

			}
			sb = featureArr(vectorList1,vectorList2, sb);

		}
		return sb;

	}


public static StringBuilder featureArr(ArrayList<double[]> vectors1, ArrayList<double[]> vectors2, StringBuilder sb){

	//int featureIndx=0;
	int featureSize = vectors1.size()*vectors2.size();

	//double[] featureArr= new double[featureSize];
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

			double similarity = cosineSimilarity(vector1, vector2);
			sb.append(similarity + ",");
			
		}
	}

	return sb;
}


public static int[] questionToCluster(String[] question, Map<String,Integer> clusterMap){
	int[] clusterArr = new int[question.length];
	for(int i =0; i< question.length;i++){

		if(clusterMap.containsKey(question[i])){
			clusterArr[i] = clusterMap.get(question[i]);
		}
		else{
			clusterArr[i]=0;
		}
	}

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

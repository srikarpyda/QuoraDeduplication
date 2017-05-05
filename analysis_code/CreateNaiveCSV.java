/* Goal: read in the files and test the naive solution. That is, take each 
   vector associated with the word and just add them all up. 

   Since the TF-ID is picking the top 10 words we'll sum up 10 vectors producing
   one vector for each variables.

   The final output wil be
   similarity,dup

   eg.
   .8, 1
   .2, 0
*/

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

public class CreateNaiveCSV {


	/* Below are helper functions created by Srikar. */
	public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;

		for (int i = 0; i < vectorA.length; i++) {


			dotProduct += vectorA[i] * vectorB[i];

			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);

		}   
		if(normA==0 || normB==0){
			return 0;
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	} 

	public static double[] strToDoubleArr(String[] arr){
		double[] doubleArr = new double[arr.length];

		for(int i=0; i<arr.length;i++){

			doubleArr[i] = Double.parseDouble(arr[i]);
		}

		return doubleArr;
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

				String[] splitLine = nextLine.split("\\s+");
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
	/* End of helper functions */


	/* Start of main method */
	public static void main(String[] args) throws IOException {

		int vectorLen = 100;

		//note: cluster file not needed for naive solutioon
		System.out.println("Hashmap started...");
		String vectorDir="/Users/andrewlee/Documents/spring17/cs590/prj/words_and_vectors(withExtraCorpus).txt";
		Map<String,double[]> vectorMap = wordToVector(vectorDir);
		System.out.println("Hashmap created");

		FileWriter pw = new FileWriter(new File("naive.csv"),true);

		StringBuilder sb= new StringBuilder();
		sb.append("similarity,dup\n");
		pw.write(sb.toString());
		//pw.close();

		int pairIdx=0;

		Scanner scanner;

		try {
			/* head kw_v2.csv
				1,guide,step,share,invest,market,by,india,is,in,to,2,guide,step,share,invest,market,by,is,in,to,what,0
			*/
			
			scanner = new Scanner(new File("/Users/andrewlee/Documents/spring17/cs590/prj/kw_v3.csv"));
			sb= new StringBuilder();

			while(scanner.hasNextLine()){	
				pairIdx++;
				String nextLine = scanner.nextLine();


				String[] splitLine = nextLine.split(",");
				
				int numWords = (splitLine.length-1)/2 - 1; //should be 10
				//System.out.println(numWords);
				//System.out.println(splitLine[22]);



				double[] finalVectorQ1 = new double[vectorLen]; // should be size 100

				//for all words
				for (int i = 1; i < numWords+1; i++){
					//System.out.println(splitLine[i]);
					try{
						double[] tempVec = vectorMap.get(splitLine[i]);

						for (int j=0; j<tempVec.length;j++){
							finalVectorQ1[j] = finalVectorQ1[j] + tempVec[j]; //add up each element in vector
						}
					}
					catch(Exception e){
						//System.out.println( splitLine[i]+ " not found in HashMap (Q1)");
					}


				}

				double[] finalVectorQ2 = new double[vectorLen]; // should be size 100
				for (int i = numWords+2; i < splitLine.length-1; i++){
					//System.out.println(splitLine[i]);
					try {
						double[] tempVec = vectorMap.get(splitLine[i]);
						
						for (int j=0; j<tempVec.length;j++){
							finalVectorQ2[j] = finalVectorQ2[j] + tempVec[j]; //add up each element in vector
						}
					}
					catch(Exception e){
						//System.out.println( splitLine[i]+ " not found in HashMap (Q2)");
					}

					

				}



				int duplicateBool=-1;
				//System.out.println(splitLine[splitLine.length-1] + " VAL");
				try{
					duplicateBool=Integer.parseInt(splitLine[splitLine.length-1]);		
				}catch(Exception e){
					System.out.println("No duplicate value exists.");
				}

				double cosSim = cosineSimilarity(finalVectorQ1,finalVectorQ2);

				sb.append(cosSim);
				sb.append(",");
				if (duplicateBool!=-1){
					sb.append(duplicateBool);
				}
				sb.append("\n");
				if (pairIdx%1000 ==0){
					System.out.println(pairIdx + " ..... writing to file.");
					pw.write(sb.toString());

					sb= new StringBuilder();//reset
				}
				

			}
			pw.write(sb.toString());
			//pw.write(sb.toString());	
			pw.close();
			scanner.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}



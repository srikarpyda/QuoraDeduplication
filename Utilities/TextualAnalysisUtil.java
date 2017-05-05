import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TextualAnalysisUtil {

	public static double[] strToDoubleArr(String[] arr){
		double[] doubleArr = new double[arr.length];

		for(int i=0; i<arr.length;i++){

			doubleArr[i] = Double.parseDouble(arr[i]);
		}

		return doubleArr;
	}
	public static Map<String,double[]> wordToVector(String vectorDir) throws IOException{

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
}

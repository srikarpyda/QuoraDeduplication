import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class Intersection {

	public static void main(String[] args) throws FileNotFoundException {
		Scanner scanner = new Scanner(new File("/Users/srikarpyda/Documents/Quora/quora_duplicate_questions.tsv"));
		String nextLine = scanner.nextLine();

		int i=0;
		while(scanner.hasNextLine() && i<100){	
			nextLine=scanner.nextLine();
			String[] nextArr = nextLine.split("	");
			String q1 = nextArr[3];
			String q2 = nextArr[4];
			String[] intersection = intersection(q1,q2).toArray(new String[intersection(q1,q2).size()]);
			System.out.println(Arrays.toString(intersection));
			i++;
		}
	}
	
    public static ArrayList<String> intersection(String s1, String s2) {
    	String[] q1 = s1.split(" ");
    	String[] q2 = s2.split(" ");

        HashSet<String> set1 = new HashSet<String>();
        for(String i: q1){
            set1.add(i);
        }
     
        HashSet<String> set2 = new HashSet<String>();
        for(String i: q2){
            if(set1.contains(i)){
                set2.add(i);
            }
        }
     
        ArrayList<String> result = new ArrayList<String>();
        for(String n: set2){
        	result.add(n);
        }
     
        return result;
    }

}

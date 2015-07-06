import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import org.json.simple.parser.JSONParser;


public class test {

	public static void main(String[] args) {
		
//		while(true) {
//			Scanner scannerOption = new Scanner(System.in);
//			Scanner scannerUsername = new Scanner(System.in);
//			Scanner scannerPassword = new Scanner(System.in);
//			
//			System.out.println("1.Resgister");
//			System.out.println("2.Login");
//			System.out.println("Choose your option:");
//			int option = scannerOption.nextInt();
//			if(option == 1) {
//				System.out.println("username:");
//				String username = scannerUsername.nextLine();
//				System.out.println("password:");
//				String password = scannerPassword.nextLine();
//				break;
//			} else if(option == 2){
//				System.out.println("username:");
//				String username = scannerUsername.nextLine();
//				System.out.println("password:");
//				String password = scannerPassword.nextLine();
//				break;
//			} else {
//				System.out.println("Please enter the correct option number");
//			}
//		}
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("A", 0);
		map.put("B", 0);
		map.put("C", 2);
		map.put("D", 2);

		String body = map.values().toString();
		//System.out.println(map.values().toString());
	}
}

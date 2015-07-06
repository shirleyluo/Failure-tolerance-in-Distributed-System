import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Client extends Thread{

	public static void main(String[] args) {
		
		Client client = new Client();
		client.start();
	}
	
	public void run() {
		String[] hostport = doGetFromDis().split("\\:");
		String host = hostport[0];
		int port = Integer.parseInt(hostport[1].toString());
		while(true) {
			Scanner scannerOption = new Scanner(System.in);
			Scanner scannerUsername = new Scanner(System.in);
			Scanner scannerPassword = new Scanner(System.in);
			//Scanner scannerLogout = new Scanner(System.in);
			
			System.out.println("1.Resgister");
			System.out.println("2.Login");
			System.out.println("Choose your option:");
			int option = scannerOption.nextInt();
			if(option == 1) {
				System.out.println("username:");
				String username = scannerUsername.nextLine();
				System.out.println("password:");
				String password = scannerPassword.nextLine();
				if(doRegisterPost(host, port, username, password) == true) {
					System.out.println("*************************");
					System.out.println("Register sucessfully!!");
					System.out.println("*************************");
				} else {
					System.out.println("*************************");
					System.out.println("The username has already existed");
					System.out.println("*************************");
				}
			} else if(option == 2){
				System.out.println("username:");
				String username = scannerUsername.nextLine();
				System.out.println("password:");
				String password = scannerPassword.nextLine();
				if(doLoginPost(host, port, username, password) == true) {
					System.out.println("*************************");
					System.out.println("Login sucessfully!!");
					System.out.println("*************************");
					while(true) {
						System.out.println("Please enter your operation:");
						Scanner scannerOper = new Scanner(System.in);
						String line = scannerOper.nextLine();
						if(line.equals("logout")) {
							break;
						} else if(line.equals("DELETEALL")) {
							DeleteAll(host, port, username);
						}
						else if(line.contains(" ")) {
							String[] lines = line.split(" ", 2);
							String method = lines[0];
							String text = lines[1];
							if(method.equals("POST")) {
								doPost(host, port, text);
							} else if(method.equals("GET")) {
								System.out.println("Result: " + doGet(host, port, text));
							} else if(method.equals("DELETE")) {
								doDelete(host, port, text);
							}
							else {
								System.out.println("*************************");
								System.out.println("Please enter the valid method");
								System.out.println("*************************");
							}
							
						} else {
							System.out.println("*************************");
							System.out.println("Please enter the valid operation");
							System.out.println("*************************");
						}
						
					}
				} else {
					System.out.println("*************************");
					System.out.println("Invalid username or password");
					System.out.println("*************************");
					
				}
			} else {
				System.out.println("*************************");
				System.out.println("Please enter the correct option number");
				System.out.println("*************************");
			}
		}
	}
	
	/*
	 * get the request body for POST method of Client to AccountServer
	 */
	public String getRequestBody(String username, String password) {
		String requestbody = null;
		JSONObject bodyObj = new JSONObject();
		bodyObj.put("username", username);
		bodyObj.put("password", password);
		requestbody = bodyObj.toString();
		return requestbody;
	}
	
	/*
	 * Register
	 * post username and password to assigned FE(Client to FE)
	 * 
	 * uri: POST /client/register
	 * body={"username":A, "password":123}
	 */
	public Boolean doRegisterPost(String host, int port, String username, String password) {
		Boolean flag = true;
		try {
			String body = getRequestBody(username, password);
			
			String path = "/client/register";
			String request = "POST " + path + " HTTP/1.1\r\n"
					+ "Content-Length:" + body.toString().length() + "\r\n"
					+ "Content-Type: application/json\r\n"
					+ "\r\n";
			
			Socket socket = new Socket(host, port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(request);
			out.write(body.toString());
			out.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String line = "";
			String response = in.readLine();
			String[] httpResponse = response.split(" ");
			if(httpResponse[1].equals("201")) {
				flag = true;
			} else if(httpResponse[1].equals("400")) {
				flag = false;
			}
			while(!(line = in.readLine().trim()).equals(""));
			socket.close();
			
			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
		
	}
	
	/*
	 * Login
	 * post username and password to assigned FE to check if it is valid
	 * (Client to FE)
	 * 
	 * uri: POST /client/login
	 * body={"username":A, "password":123}
	 */
	public Boolean doLoginPost(String host, int port,String username, String password) {
		Boolean flag = true;
		try {
			String body = getRequestBody(username, password);
			
			String path = "/client/login";
			String request = "POST " + path + " HTTP/1.1\r\n"
					+ "Content-Length:" + body.toString().length() + "\r\n"
					+ "Content-Type: application/json\r\n"
					+ "\r\n";
			
			Socket socket = new Socket(host, port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(request);
			out.write(body.toString());
			out.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String line = "";
			String response = in.readLine();
			String[] httpResponse = response.split(" ");
			if(httpResponse[1].equals("200")) {
				flag = true;
			} else if(httpResponse[1].equals("400")) {
				flag = false;
				socket.close();
				return flag;
			}
			while(!(line = in.readLine().trim()).equals(""));
//			socket.close();
			
			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
		
	}
	
	/*
	 * Doing loadBalancer
	 * get the frontend ip which client can connect with it(client to discovery)
	 * 
	 * uri: GET /discovery/frontend
	 */
	public String doGetFromDis() {
		String responsebody = null;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("host.json"));
			JSONObject jsonObject = (JSONObject) obj;
			String host = (String)jsonObject.get("host");
			int port = Integer.parseInt(jsonObject.get("port").toString());
			String path = "/discovery/frontend";
			String request = "GET " + path + " HTTP/1.1\r\n"
					+ "Content-Type: application/json\r\n" + "\r\n";
			Socket socket;
			socket = new Socket(host, port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			out.write(request);
			out.flush();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String line = "";
			char[] cbuf = new char[4096];
			StringBuilder stringBuilder = new StringBuilder();
			while (!(line = in.readLine().trim()).equals(""));
			if(in.ready()) {
				stringBuilder.append(cbuf, 0, in.read(cbuf));
				responsebody = stringBuilder.toString();
			}
			socket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responsebody;

	}
	
	/*
	 * POST
	 * send text to FE if login sucessfully(Client to FE)
	 * 
	 * uri: POST /tweets
	 */
	public void doPost(String host, int port, String text) {
		try {
			
			String path = "/tweets";
			String request = "POST " + path + " HTTP/1.1\r\n"
					+ "Content-Length:" + text.toString().length() + "\r\n"
					+ "Content-Type: application/json\r\n"
					+ "\r\n";
			
			Socket socket = new Socket(host, port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(request);
			out.write(text.toString());
			out.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream())); 
			String line = "";
			String response = in.readLine();
			System.out.println(response);
//			String[] httpResponse = response.split(" ");
//			if(httpResponse[1].equals("201")) {
//				flag = true;
//			} else if(httpResponse[1].equals("400")) {
//				flag = false;
//			}
			while(!(line = in.readLine().trim()).equals(""));
//			socket.close();
			} catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	/*
	 * GET
	 * send query parameter to FE(Client to FE)
	 * 
	 * uri:GET /tweets?q=
	 */
	public String doGet(String host, int port, String searchterm) {
		String responsebody = null;
		try {
			
			String path = "/tweets?q=" + searchterm;
			String request = "GET " + path + " HTTP/1.1\r\n"
					+ "Content-Type: application/json\r\n"
					+ "\r\n";
			StringBuilder stringBuilder = new StringBuilder();
			Socket socket = new Socket(host, port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(request);
			out.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream())); 
			String line = "";
			char[] cbuf = new char[4096];
			
			while(!(line = in.readLine().trim()).equals(""));
			if(in.ready()) {
				stringBuilder.append(cbuf, 0, in.read(cbuf));
				responsebody = stringBuilder.toString();
		        
			}
//			socket.close();
			} catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return responsebody;
	}
	
	/*
	 * delete all tweets of the login user(Client to FE)
	 * 
	 * uri: DELETE /deleteall
	 */
	public void DeleteAll(String host, int port, String username) {
		try {
			
			String path = "/deleteall";
			String request = "DELETE " + path + " HTTP/1.1\r\n"
					+ "Content-Type: application/json\r\n"
					+ "\r\n";
			StringBuilder stringBuilder = new StringBuilder();
			Socket socket = new Socket(host, port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(request);
			out.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream())); 
			String line = "";
			
			while(!(line = in.readLine().trim()).equals(""));
//			socket.close();
			} catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	/*
	 * delete by hashtag(Client to FE)
	 * 
	 * uri: DELETE /delete
	 */
	public void doDelete(String host, int port, String q) {
		try {
			
			String path = "/delete";
			String request = "DELETE " + path + " HTTP/1.1\r\n"
					+ "Content-Type: application/json\r\n"
					+ "\r\n";
			//StringBuilder stringBuilder = new StringBuilder();
			Socket socket = new Socket(host, port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(request);
			out.write(q);
			out.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream())); 
			String line = "";
			String response = in.readLine();
			System.out.println(response);
//			while(!(line = in.readLine().trim()).equals(""));
//			socket.close();
			} catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
}

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.logging.log4j.*;


public class FrontEndProcessor implements Runnable {
	
	private Logger logger;
	private Socket sock;
	private HTTPRequestLine httpRequestLine;
	private HTTPRequestLineParser httpParser;
	private Cache cache;
	private ResponseType type;
	private static String username;
//	private static String backendip;
	private String host;
	private int port;
	
	public FrontEndProcessor(Socket sock, Cache cache, String host, int port, Logger logger) {
		this.logger = logger;
		this.cache = cache;
		this.sock = sock;
		this.host = host;
		this.port = port;
		httpRequestLine = new HTTPRequestLine();
		httpParser = new HTTPRequestLineParser();
		type = new ResponseType();
		
	}
	

	@SuppressWarnings({ "unchecked", "static-access" })
	@Override
	public void run() {
		// TODO Auto-generated method stub
		logger.info("socket connection");
//		String[] hostport = doBackendGet().split("\\:");
//		String host = hostport[0];
//		int port = Integer.parseInt(hostport[1].toString());
		try {
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					sock.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					sock.getOutputStream()));
			
			String httpline = in.readLine();
			String[] lines = httpline.split(" ");
			
			String responsebody = null;
			String responseheaders = null;
			ArrayList<String> responseArray = new ArrayList<String>();
			
			String line = null;
			StringBuilder stringBuilder = new StringBuilder();
			
			
			if(httpParser.parse(httpline) == null) {
				logger.error("Invalid HTTPRequest");
				responseArray = type.BadRequest();
				responsebody = responseArray.get(0);
				responseheaders = responseArray.get(1);
				
				out.write(responseheaders);
				out.write(responsebody);
				out.flush();
				out.close();
				sock.close();
				return;
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/client/register")) {
				line = null;
				char[] cbuf = new char[4096];
				
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					
				}
				String body = stringBuilder.toString();
				System.out.println("Get the username and password:" + body);
				if(register(host, port, body) == true) {
					responseArray = type.Created();
					responsebody = responseArray.get(0);
					responseheaders = responseArray.get(1);
				} else if(register(host, port, body) == false){
					responseArray = type.BadRequest();
					responsebody = responseArray.get(0);
					responseheaders = responseArray.get(1);
				} 
				
				out.write(responseheaders);
				out.write(responsebody);
				out.flush();
				out.close();
				sock.close();

			}
			
			if(httpParser.parse(httpline).getUripath().equals("/client/login")) {
				
				char[] cbuf = new char[4096];
				
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
				}
				String body = stringBuilder.toString();
				System.out.println("Login" + body);
				JSONParser parser = new JSONParser();
				JSONObject bodyObj = (JSONObject) parser.parse(body);
				username = (String) bodyObj.get("username");
				Boolean result = login(host, port, body);
				if( result== true) {
					responseArray = type.Valid();
					responsebody = responseArray.get(0);
					responseheaders = responseArray.get(1);
				} else if(result == false){
					responseArray = type.BadRequest();
					responsebody = responseArray.get(0);
					responseheaders = responseArray.get(1);
				}
				out.write(responseheaders);
				out.write(responsebody);
				out.flush();
				out.close();
				sock.close();

			}
			
			if(httpParser.parse(httpline).getUripath().equals("/deleteall")) {
				
				responseArray = type.Valid();
				responsebody = "<html><body> Delete sucesful! </body></html>\r\n";
				responseheaders = responseArray.get(1);
				doDeleteAll(host, port, username);
				out.write(responseheaders);
				out.write(responsebody);
				out.flush();
				out.close();
				sock.close();
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/delete")) {
				char[] cbuf = new char[4096];
				
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
				}
				String body = stringBuilder.toString();
				
				responseArray = type.Valid();
				responsebody = "<html><body> Delete sucesful! </body></html>\r\n";
				responseheaders = responseArray.get(1);
				doDelete(host, port, username, body);
				
				out.write(responsebody);
				out.write(responseheaders);
				out.flush();
				out.close();
				sock.close();
			}
			if(httpParser.parse(httpline).getUripath().equals("/tweets")) {
				if(lines[0].equals(HTTPConstants.HTTPMethod.valueOf("POST").toString())) {
					char[] cbuf = new char[4096];
					while(!(line = in.readLine().trim()).equals(""));
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					
					String body = stringBuilder.toString();
					//System.out.println(body);
					if(doPost(host, port, getBody(body))) {
						responsebody = "<html><body> Created!! </body></html>\r\n";
						
						responseheaders = "HTTP/1.1 201 Created\n" +
										"Content-Length: " + responsebody.getBytes().length + "\n\n";
					} 
					
					out.write(responsebody);
					out.write(responseheaders);
					out.flush();
					out.close();
					sock.close();
					
				} else if(lines[0].equals(HTTPConstants.HTTPMethod.valueOf("GET").toString())) {
					HashMap<String, String> parameters = httpParser.parse(httpline).getHashMap();
					String searchterm = parameters.get("q");
					if(searchterm == null) {
						responseArray = type.BadRequest();
						responsebody = responseArray.get(0);
						responseheaders = responseArray.get(1);
						
						out.write(responseheaders);
						out.write(responsebody);
					} else if(doGet(host, port, searchterm)) {

						responsebody = cache.get(searchterm, username) + "\r\n";
						responseheaders = "HTTP/1.1 200 OK\n" +
								"Content-Length: " + responsebody.getBytes().length + "\n\n";
//						System.out.println("GET!!! " + responsebody);
						out.write(responseheaders);
						out.write(responsebody);
						out.write("\r\n");
					} 
						out.flush();
						out.close();
						sock.close();
						
					
					
				}
			}

			
			
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Register
	 * send username and password to assigned Backend
	 * (FE to BE)
	 * 
	 * uri: POST /account/register
	 * requestbody: {"username":a, "password":1}
	 */
	private Boolean register(String host, int port, String body) {
		Boolean flag = true;
		try {
			
			String path = "/register";
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
			} catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return flag;
	}
	
	/*
	 * Login
	 * send username and password to assigned BE to check if it is valid
	 * (FE to BE)
	 * 
	 * uri: POST /login
	 * requestbody: {"username":a, "password":1}
	 */
	private Boolean login(String host, int port, String body) {
		Boolean flag = true;
		try {
			
			String path = "/login";
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
			}
			while(!(line = in.readLine().trim()).equals(""));
//			socket.close();
			} catch(IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return flag;
	}
	/*
	 * POST: get request body
	 * @return
	 * @throws ParseException
	 */
	
	private JSONObject getBody(String body) throws ParseException {
		
		String tweet = httpRequestLine.getText(body);
		JSONObject tweetJson = httpRequestLine.textAnalysis(tweet);
		tweetJson.put("user", username);
		return tweetJson;
		
	}
	
	/*
	 * get the assigned backend server ip from discovery(FE to Discovery)
	 * 
	 * uri: GET /discovery
	 */
	public String getBackEndIP() {
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
			obj = parser.parse(new FileReader("host.json"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONObject jsonObject = (JSONObject) obj;
		String host = (String) jsonObject.get("host");
		int discoveryPort = Integer.parseInt(jsonObject.get("port").toString());
		
		String path = "/discovery/backend";
		String request = "GET " + path + " HTTP/1.1\r\n"
				+ "Content-Type: application/json\r\n"
				+ "\r\n";
		
		Socket socket;
		StringBuilder stringBuilder = new StringBuilder();

		String ip = null;
		try {
			socket = new Socket(host, discoveryPort);
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
				ip = stringBuilder.toString();
		        
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ip;
	}
	
	/*
	 * POST(FE to BE)
	 * 
	 * uri: POST /tweets
	 * 
	 * @param body
	 * @throws IOException
	 */
	private Boolean doPost(String host, int port, JSONObject body) {
		
		boolean flag = true;
		try {
			
			String path = "/tweets";
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
			while(!(line = in.readLine().trim()).equals(""));
//			socket.close();
			
			
		}
		catch (IOException e) {

			String[] hostport = getBackEndIP().split("\\:");
			host = hostport[0];
			port = Integer.parseInt(hostport[1]);
			doPost(host, port, body);
		}
		return flag;
	}
	

	
	/*
	 * POST timestamp to BE to compare(FE to BE)
	 * @param queries
	 * @throws IOException
	 * @throws ParseException 
	 * 
	 * uri: POST /timestamps
	 */
	private Boolean doGet(String host, int port, String searchterm) {
//		System.out.println("**************" + ip);
		boolean flag = true;
		ArrayList<String> responseArray = new ArrayList<String>();
		try {
			
//			System.out.println("Version number: " + versionnum);
//			System.out.println(cache.getCache().get(ip));
			
			int versionnum = cache.getVersion(host + ":" + port, username, searchterm);
			String path = "/timestamps";
			
			String request = "POST " + path + " HTTP/1.1\r\n"
					+ "Content-Type: application/json\r\n"
					+ "\r\n";
			Socket socket = new Socket(host, port);
			String body = cache.getTimestampBody();
//			System.out.println("*******" + body);
			
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(request);
			if(body != null) {
				out.write(body);
			}
			out.flush();
			
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String line = "";
			char[] cbuf = new char[4096];
			StringBuilder stringBuilder = new StringBuilder();
			String header = in.readLine();
			System.out.println(header);
			while(!(line = in.readLine().trim()).equals(""));
			
			if(header.contains("200")) {
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					String responsebody1 = stringBuilder.toString();
			        cache.storeInfo(responsebody1);
				} 
			} 
			
		}
		catch (IOException e) {
//			while(true) {
//				String[] hostport = getBackEndIP().split("\\:");
//				host = hostport[0];
//				port = Integer.parseInt(hostport[1]);
//				String body = cache.getTimestampBody();
//				Boolean timestampFlag = doTimestampPost(host, port, body);
//				if(timestampFlag == true) {
//					doGet(host, port, searchterm);
//					break;
//				} else {
//					doGet(host, port, searchterm);
//				}
//				
//			}
			String[] hostport = getBackEndIP().split("\\:");
			host = hostport[0];
			port = Integer.parseInt(hostport[1]);
			doGet(host, port, searchterm);
			
		} 
		return flag;
		
	}
	
//	/*
//	 * post timestamp to BE to compare two timestamps(FE to BE)
//	 * 
//	 * uri: POST /timestamp
//	 */
//	public Boolean doTimestampPost(String host, int port, String body) {
//		boolean flag = true;
//		try {
//			
//			String path = "/timestamp";
//			String request = "POST " + path + " HTTP/1.1\r\n"
//					+ "Content-Length:" + body.toString().length() + "\r\n"
//					+ "Content-Type: application/json\r\n"
//					+ "\r\n";
//			
//			Socket socket = new Socket(host, port);
//			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//			out.write(request);
//			out.write(body.toString());
//			out.flush();
//			
//			BufferedReader in = new BufferedReader(new InputStreamReader(
//					socket.getInputStream()));
//			String line = "";
//			String header = in.readLine();
//			while(!(line = in.readLine().trim()).equals(""));
//			if(header.contains("200")) {
//				flag = true;
//			} else {
//				flag = false;
//			}
////			socket.close();
//		}
//		catch (IOException e) {
//			e.getStackTrace();
//		}
//		return flag;
//	}
	
	/*
	 * Delete All(FE to BE)
	 * 
	 * uri: DELETE /deleteall
	 */
	public void doDeleteAll(String host, int port, String user) {
		try {
			String path = "/deleteall";
			String request = "DELETE " + path + " HTTP/1.1\r\n"
					+ "Content-Type: application/json\r\n"
					+ "\r\n";
			StringBuilder stringBuilder = new StringBuilder();
			Socket socket = new Socket(host, port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(request);
			out.write(user);
			out.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream())); 
			String line = "";
			
			while(!(line = in.readLine().trim()).equals(""));
			
//		socket.close();
		} catch(IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			String[] hostport = getBackEndIP().split("\\:");
			host = hostport[0];
			port = Integer.parseInt(hostport[1]);
			doDeleteAll(host, port, user);
		}
	}
	
	/*
	 * delete by hashtag(FE to BE)
	 * 
	 * uri: DELETE /delete
	 */
	public void doDelete(String host, int port, String user, String deleteterm) {
		try {
			String path = "/delete";
			String request = "DELETE " + path + " HTTP/1.1\r\n"
					+ "Content-Type: application/json\r\n"
					+ "\r\n";
//			StringBuilder stringBuilder = new StringBuilder();
			Socket socket = new Socket(host, port);
			JSONObject body = new JSONObject();
			body.put("username", user);
			body.put("deleteterm", deleteterm);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(request);
			out.write(body.toString());
			out.flush();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream())); 
			String line = "";
			
			while(!(line = in.readLine().trim()).equals(""));
			
//		socket.close();
		} catch(IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			String[] hostport = getBackEndIP().split("\\:");
			host = hostport[0];
			port = Integer.parseInt(hostport[1]);
			doDelete(host, port, user, deleteterm);
		}
	}
}

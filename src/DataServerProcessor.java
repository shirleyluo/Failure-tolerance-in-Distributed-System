import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class DataServerProcessor implements Runnable{
	
	private Logger logger;
	private Socket sock;
	private Data data;
	private HTTPRequestLineParser httpParser;
	private ResponseType type;
	private String ip;
	
	
	public DataServerProcessor(Socket sock, Data data, String ip, Logger logger) {
		this.sock = sock;
        this.data = data;
		this.logger = logger;
		this.ip = ip;
		httpParser = new HTTPRequestLineParser();
		type = new ResponseType();
		
	}


	@SuppressWarnings({ "unchecked", "static-access"})
	@Override
	public void run() {
		// TODO Auto-generated method stub
		logger.info("socket connection");
		BufferedReader in = null;
		String line = null;
		BufferedWriter out = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					sock.getInputStream()));
		
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			StringBuilder stringBuilder = new StringBuilder();
			String responsebody = null;
			String responseheaders = null;
			String httpline = in.readLine();
			String[] lines = httpline.split(" ");
			ArrayList<String> responseArray = new ArrayList<String>();
			
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
			
			if(httpParser.parse(httpline).getUripath().equals("/register")) {
				if(lines[0].equals(HTTPConstants.HTTPMethod.valueOf("POST").toString())) {
					String username = null;
					String password = null;
					char[] cbuf = new char[4096];
					
					while(!(line = in.readLine().trim()).equals(""));
					if(in.ready()) {
						stringBuilder.append(cbuf, 0, in.read(cbuf));
						String body = stringBuilder.toString();
						//System.out.println(body);
						JSONParser parser = new JSONParser();
						JSONObject bodyjs = (JSONObject) parser.parse(body);
						username = (String) bodyjs.get("username");
						password = (String) bodyjs.get("password");
						
					}
					if(data.Register(username, password) == true) {
						responseArray = type.Created();
						responsebody = responseArray.get(0);
						responseheaders = responseArray.get(1);
					} else {
						responseArray = type.BadRequest();
						responsebody = responseArray.get(0);
						responseheaders = responseArray.get(1);
					}
					accountBroadcast(stringBuilder.toString(), data.getBackendInfo());
					out.write(responseheaders);
					out.write(responsebody);
					out.flush();
					out.close();
					sock.close();
				}

			}
			
			if(httpParser.parse(httpline).getUripath().equals("/login")) {
				if(lines[0].equals(HTTPConstants.HTTPMethod.valueOf("POST").toString())) {
					String username = null;
					String password = null;
					char[] cbuf = new char[4096];
					
					while(!(line = in.readLine().trim()).equals(""));
					if(in.ready()) {
						stringBuilder.append(cbuf, 0, in.read(cbuf));
						String body = stringBuilder.toString();
//						System.out.println(body);
						JSONParser parser = new JSONParser();
						JSONObject bodyjs = (JSONObject) parser.parse(body);
						username = (String) bodyjs.get("username");
						password = (String) bodyjs.get("password");
						
					}
					Boolean result = data.login(username, password);
					if(result == true) {
						responseArray = type.Valid();
						responsebody = responseArray.get(0);
						responseheaders = responseArray.get(1);
					} else {
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

			}
			
			if(httpParser.parse(httpline).getUripath().equals("/check")) {
				responseArray = type.Valid();
				responsebody = responseArray.get(0);
				responseheaders = responseArray.get(1);
				out.write(responseheaders);
				out.write(responsebody);
				out.flush();
				out.close();
				sock.close();
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/information")) {
				char[] cbuf = new char[4096];
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					String body = stringBuilder.toString();
					data.updateBackendInfo(body);
					
					responseArray = type.Valid();
					responsebody = responseArray.get(0);
					responseheaders = responseArray.get(1);
					out.write(responseheaders);
					out.write(responsebody);
					out.flush();
					out.close();
					sock.close();
					
				}
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/broadcast")) {
				char[] cbuf = new char[4096];
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					String body = stringBuilder.toString();
					System.out.println("BROADCAST body: " + body);
					data.storeTweet(ip, body);
					data.updateBroadcast(body, ip);
					responseArray = type.Valid();
					responsebody = responseArray.get(0);
					responseheaders = responseArray.get(1);
					out.write(responseheaders);
					out.write(responsebody);
					out.flush();
					out.close();
					sock.close();
					
				}
			}
			if(httpParser.parse(httpline).getUripath().equals("/broadcast/delete")) {
				char[] cbuf = new char[4096];
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					String body = stringBuilder.toString();
					System.out.println("BROADCAST delete body: " + body);
					JSONParser parser = new JSONParser();
					JSONObject object = (JSONObject) parser.parse(body);
					String username = (String) object.get("username");
					String deleteterm = (String) object.get("deleteterm");
//					System.out.println(username);
//					System.out.println(deleteterm);
					data.delete(username, deleteterm);
					responseArray = type.Valid();
					responsebody = responseArray.get(0);
					responseheaders = responseArray.get(1);
					out.write(responseheaders);
					out.write(responsebody);
					out.flush();
					out.close();
					sock.close();
					
				}
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/broadcast/deleteall")) {
				char[] cbuf = new char[4096];
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					String body = stringBuilder.toString();
					System.out.println("BROADCAST delete body: " + body);
					
//					System.out.println(username);
//					System.out.println(deleteterm);
					data.deleteAll(body);
					responseArray = type.Valid();
					responsebody = responseArray.get(0);
					responseheaders = responseArray.get(1);
					out.write(responseheaders);
					out.write(responsebody);
					out.flush();
					out.close();
					sock.close();
					
				}
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/account")) {
				char[] cbuf = new char[4096];
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					String body = stringBuilder.toString();
					JSONParser parser = new JSONParser();
					JSONObject bodyjs = (JSONObject) parser.parse(body);
					String username = (String) bodyjs.get("username");
					String password = (String) bodyjs.get("password");
					data.Register(username, password);
					responseArray = type.Valid();
					responsebody = responseArray.get(0);
					responseheaders = responseArray.get(1);
					out.write(responseheaders);
					out.write(responsebody);
					out.flush();
					out.close();
					sock.close();
					
				}
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/timestamps")) {
				char[] cbuf = new char[4096];
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					String body = stringBuilder.toString();
					
					if(data.compareEqual(body) == true)  {
						responseheaders = "HTTP/1.1 304 Not Modified\n" + 
								"Content-Length: " + 0 + "\n\n";;
						out.write(responseheaders);
					} else {
						int count = 1;
						while(count < 10) {
							if(data.compare(body) == true) {
								break;
							}
							count++;
						}
						responseArray = type.Valid();
						responsebody = data.getInformation();
						responseheaders = responseArray.get(1);
						out.write(responseheaders);
						out.write(responsebody);
					}
				} else {
					
					responseArray = type.Valid();
					responsebody = data.getInformation();
					responseheaders = responseArray.get(1);
					out.write(responseheaders);
					out.write(responsebody);
				}
				out.flush();
				out.close();
				sock.close();
		
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/randomInfo")) {
				responsebody = data.getInformation();
				//System.out.println(responsebody);
				responseheaders = "HTTP/1.1 200 OK\n" +
						"Content-Length: " + responsebody.getBytes().length + "\n\n";
				
				out.write(responseheaders);
				out.write(responsebody);
				out.flush();
				out.close();
				sock.close();
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/whoisfailed")) {
				char[] cbuf = new char[4096];
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					String body = stringBuilder.toString();
					ArrayList<String> list = data.getBackendInfo();
					list.remove(body);
					doEventual(list, body);	
				}
				responseArray = type.Valid();
				responsebody = responseArray.get(0);
				responseheaders = responseArray.get(1);
				
				out.write(responseheaders);
				out.write(responsebody);
				out.flush();
				out.close();
				sock.close();
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/eventual")) {
				char[] cbuf = new char[4096];
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					
					String body = stringBuilder.toString();
					data.storeEventual(body);
					
				}
				
				responseArray = type.Valid();
				responsebody = responseArray.get(0);
				responseheaders = responseArray.get(1);
				out.write(responseheaders);
				out.write(responsebody);
				out.flush();
				out.close();
				sock.close();
				
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/deleteall")) {
				char[] cbuf = new char[4096];
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
				}
				
				String body = stringBuilder.toString();
				data.deleteAll(body);
				responseArray = type.Valid();
				responsebody = responseArray.get(0);
				responseheaders = responseArray.get(1);
				out.write(responseheaders);
				out.write(responsebody);
				out.flush();
				out.close();
				sock.close();
				broadcastDeleteAll(body, data.getBackendInfo());
				
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/delete")) {
				char[] cbuf = new char[4096];
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
				}
				
				String body = stringBuilder.toString();
				JSONParser parser = new JSONParser();
				JSONObject object = (JSONObject) parser.parse(body);
				String username = (String) object.get("username");
				String deleteterm = (String) object.get("deleteterm");
//				System.out.println(username);
//				System.out.println(deleteterm);
				data.delete(username, deleteterm);
				responseArray = type.Valid();
				responsebody = responseArray.get(0);
				responseheaders = responseArray.get(1);
				out.write(responseheaders);
				out.write(responsebody);
				out.flush();
				out.close();
				sock.close();
				
				broadcastDelete(body, data.getBackendInfo());
				
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/tweets")) {
				if(lines[0].equals(HTTPConstants.HTTPMethod.valueOf("POST").toString())) {
					
					char[] cbuf = new char[4096];
					while(!(line = in.readLine().trim()).equals(""));
					if(in.ready()) {
						stringBuilder.append(cbuf, 0, in.read(cbuf));
					}
					
					String body = stringBuilder.toString();
					//System.out.println("***********" + body);
					data.storeTweet(ip, body);
					data.updateTimestamp(ip);
					data.storeLog(ip);
					responsebody = "<html><body> Created!! </body></html>\r\n";
					
					responseheaders = "HTTP/1.1 201 Created\n" +
									"Content-Length: " + responsebody.getBytes().length + "\n\n";
					
					out.write(responseheaders);
					out.write(responsebody);
					out.flush();
					out.close();
					sock.close();
					
					broadcast(data.getBroadcastBody(body, ip), data.getBackendInfo());
					
				} 
//				else if(lines[0].equals(HTTPConstants.HTTPMethod.valueOf("GET").toString())) {
//					HashMap<String, String> parameters = httpParser.parse(httpline).getHashMap();
//					
//					String searchterm = parameters.get("q");
//					int versionnum = Integer.parseInt(parameters.get("v"));
//					String username = parameters.get("u");
//					
//					responseheaders = null;
//					if(data.getVersion(ip, username, searchterm) >= versionnum) {
//						responsebody = data.getValue(searchterm, username);
////						System.out.println("GET***" + responsebody);
//						responseheaders = "HTTP/1.1 200 OK\n" +
//								"Content-Length: " + responsebody.getBytes().length + "\n\n";
//						out.write(responseheaders);
//						out.write(responsebody);
//						
//						
//					} else {
//						logger.info("304 Not Modified the cache datas need not to change");
//						responseheaders = "HTTP/1.1 304 Not Modified\n" + 
//								"Content-Length: " + 0 + "\n\n";;
//						out.write(responseheaders);
//					}
//					out.flush();
//					out.close();
//					sock.close();
//				}
				
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.getMessage();
		
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/*
	 * Broadcast new data to other data servers(BE to BE)
	 * 
	 * uri: POST /broadcast
	 */
	public void broadcast(String requestbody, ArrayList<String> list) {
		list.remove(ip);
//		System.out.println("The backend ip which need to broadcast: " + list);
		
		String path = "/broadcast";
		String request = "POST " + path + " HTTP/1.1\r\n"
				+ "Content-Type: application/json\r\n"
				+ "\r\n";
		for(int i = 0; i < list.size(); i++) {
			
			String[] hostport = list.get(i).toString().split("\\:");
			String host = hostport[0];
			int port = Integer.parseInt(hostport[1]);
			try {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException interruptedException) {

					System.out
							.println("Thread is interrupted when it is sleeping"
									+ interruptedException);
				}
				Socket socket = new Socket(host, port);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				
				out.write(request);
				out.write(requestbody.toString());
				out.flush();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String line = "";
				//System.out.println(in.readLine());
				while(!(line = in.readLine().trim()).equals(""));

				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
//				System.out.println("this secondary has down");
			}
		}
	}

	/*
	 * broadcast after doing delete(BE to BE)
	 * 
	 * uri: POST /broadcast/delete
	 */
	public void broadcastDelete(String requestbody, ArrayList<String> list) {
		list.remove(ip);
//		System.out.println("The backend ip which need to broadcast: " + list);
		
		String path = "/broadcast/delete";
		String request = "POST " + path + " HTTP/1.1\r\n"
				+ "Content-Type: application/json\r\n"
				+ "\r\n";
		for(int i = 0; i < list.size(); i++) {
			
			String[] hostport = list.get(i).toString().split("\\:");
			String host = hostport[0];
			int port = Integer.parseInt(hostport[1]);
			try {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException interruptedException) {

					System.out
							.println("Thread is interrupted when it is sleeping"
									+ interruptedException);
				}
				Socket socket = new Socket(host, port);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				
				out.write(request);
				out.write(requestbody.toString());
				out.flush();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String line = "";
				//System.out.println(in.readLine());
				while(!(line = in.readLine().trim()).equals(""));

				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
//				System.out.println("this secondary has down");
			}
		}
	}
	
	/*
	 * broadcast after doing delete(BE to BE)
	 * 
	 * uri: POST /broadcast/deleteall
	 */
	public void broadcastDeleteAll(String requestbody, ArrayList<String> list) {
		list.remove(ip);
//		System.out.println("The backend ip which need to broadcast: " + list);
		
		String path = "/broadcast/deleteall";
		String request = "POST " + path + " HTTP/1.1\r\n"
				+ "Content-Type: application/json\r\n"
				+ "\r\n";
		for(int i = 0; i < list.size(); i++) {
			
			String[] hostport = list.get(i).toString().split("\\:");
			String host = hostport[0];
			int port = Integer.parseInt(hostport[1]);
			try {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException interruptedException) {

					System.out
							.println("Thread is interrupted when it is sleeping"
									+ interruptedException);
				}
				Socket socket = new Socket(host, port);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				
				out.write(request);
				out.write(requestbody.toString());
				out.flush();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String line = "";
				//System.out.println(in.readLine());
				while(!(line = in.readLine().trim()).equals(""));

				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
//				System.out.println("this secondary has down");
			}
		}
	}
	
	/*
	 * broadcast account information to other BE(BE to BE)
	 * 
	 * uri POST /account
	 */
	public void accountBroadcast(String body, ArrayList<String> list) {
		list.remove(ip);
		String path = "/account";
		String request = "POST " + path + " HTTP/1.1\r\n"
				+ "Content-Type: application/json\r\n"
				+ "\r\n";
		for(int i = 0; i < list.size(); i++) {
			String[] hostport = list.get(i).toString().split("\\:");
			String host = hostport[0];
			int port = Integer.parseInt(hostport[1]);
			try {
				Socket socket = new Socket(host, port);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				
				out.write(request);
				out.write(body);
				out.flush();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String line = "";
				//System.out.println(in.readLine());
				while(!(line = in.readLine().trim()).equals(""));

				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
//				System.out.println("this secondary has down");
			}
		}
	}
	
	/*
	 * do eventual consistency when one server failed(BE to BE)
	 * 
	 * uri: POST /eventual
	 */
	public void doEventual(ArrayList<String> list, String failip) {
		list.remove(ip);
		String path = "/eventual";
		String request = "POST " + path + " HTTP/1.1\r\n"
				+ "Content-Type: application/json\r\n"
				+ "\r\n";
		for(int i = 0; i < list.size(); i++) {
			String address =list.get(i);
			System.out.println("$$$" + address);
			String[] hostport = address.split("\\:");
			String host = hostport[0];
			int port = Integer.parseInt(hostport[1]);
			try {
				Socket socket = new Socket(host, port);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				String body = data.getLogInformation(failip);
				System.out.println("**********" + body);
				out.write(request);
				if(body != null) {
					out.write(body);
				}
				
				out.flush();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String line = "";
				//System.out.println(in.readLine());
				while(!(line = in.readLine().trim()).equals(""));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
		}
	}
}

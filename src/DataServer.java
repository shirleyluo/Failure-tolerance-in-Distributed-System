import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DataServer extends Thread {

	private static Logger logger = LogManager.getLogger(DataServer.class);
	private static String ip;
	private int id;
	Data d;

	DataServer(String ip) {
		this.ip = ip;
		d = new Data();
	}

	public static void main(String[] args) throws UnknownHostException {

//		int PORT = 3099;
//		ip = "localhost:3099";
		ip = InetAddress.getLocalHost().getHostAddress() + ":" + args[0];
		DataServer dataServer = new DataServer(ip);

		ServerSocket serversock;
		try {
			//serversock = new ServerSocket(PORT);
			serversock = new ServerSocket(Integer.parseInt(args[0]));

			logger.info("DataServer starts");
			ExecutorService executor = Executors.newFixedThreadPool(10);

			dataServer.start();
			while (true) {
				Socket sock = serversock.accept();
				executor.execute(new DataServerProcessor(sock, dataServer.d,
						ip, logger));
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void run() {
		logger.info("thread starts");
		d.setResultsMap(ip);
		d.setTimestamp(ip);
		doPostToDiscovery(ip);
//		System.out.println("New BE ip: " + getNewIP());
		String newIP = getNewIP();
//		System.out.println("******" + newIP);
		if(newIP != null) {
			getNewInfo(newIP);
		}
	}

	/*
	 * send HTTPRequest to discovery server in order to post its own ip
	 * 
	 * uri: POST /discovery/backend
	 */
	private void doPostToDiscovery(String localip) {

		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("host.json"));
			JSONObject jsonObject = (JSONObject) obj;
			String host = (String) jsonObject.get("host");
			int discoveryPort = Integer.parseInt(jsonObject.get("port").toString());
			// String ip = "localhost:3098";
			String path = "/discovery/backend";
			String request = "POST " + path + " HTTP/1.1\r\n"
					+ "Content-Type: application/json\r\n" + "\r\n";
			Socket socket;
			socket = new Socket(host, discoveryPort);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			String body = localip;
			out.write(request);
			out.write(body);
			out.flush();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String line = "";
			while (!(line = in.readLine().trim()).equals(""));
//			socket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/*
	 * select a random BE ip(BE to Discovery)
	 * 
	 * uri: POST /discovery/newBE
	 */
	public String getNewIP() {
		String newIP = null;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("host.json"));
			JSONObject jsonObject = (JSONObject) obj;
			String host = (String) jsonObject.get("host");
			int discoveryPort = Integer.parseInt(jsonObject.get("port").toString());
			// String ip = "localhost:3098";
			String path = "/discovery/newBE";
			String request = "POST " + path + " HTTP/1.1\r\n"
					+ "Content-Type: application/json\r\n" + "\r\n";
			Socket socket;
			socket = new Socket(host, discoveryPort);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream()));
			out.write(request);
			out.write(ip);
			out.flush();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();
			String line = "";
			char[] cbuf = new char[4096];
			String response = in.readLine();
			while(!(line = in.readLine().trim()).equals(""));
			if(response.contains("400")) {
				newIP = null;
			} else {
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					newIP = stringBuilder.toString();
			        
				}
			}
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newIP;
	}
	
	/*
	 * get the information from random BE(BE to BE)
	 * 
	 * uri: GET /randomInfo
	 */
	public void getNewInfo(String randomip) {
		try {
			String[] hostport = randomip.split("\\:");
			String host = hostport[0];
			int port = Integer.parseInt(hostport[1]);
			String path = "/randomInfo";
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
				String responsebody = stringBuilder.toString();
//				System.out.println("**********" + responsebody);
				if(!responsebody.equals("{}")) {
					d.storeInfo(responsebody);
					d.storeLog(randomip);
					d.storeLog(ip);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}

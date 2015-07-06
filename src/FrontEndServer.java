import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FrontEndServer extends Thread{

	private static Logger logger = LogManager.getLogger(FrontEndServer.class);
	private static String ip;
	private static String host;
	private static int port;
	private Cache cache;
	
	public FrontEndServer(String ip) {
		this.ip = ip;
		cache = new Cache();
	}
	public static void main(String[] args) throws NumberFormatException, IOException {

//		Cache cache = new Cache();
		//int PORT = 3050;
		//ip = "localhost:3050";
		ip = InetAddress.getLocalHost().getHostAddress() + ":" + args[0];
		FrontEndServer frontendServer = new FrontEndServer(ip);
		ServerSocket serversock = new ServerSocket(Integer.parseInt(args[0]));
		//ServerSocket serversock;
		try {
//			serversock = new ServerSocket(PORT);
			logger.info("FrontEndServer starts");
			
			ExecutorService executor = Executors.newFixedThreadPool(10);
			frontendServer.start();
			
			while (true) {
				Socket sock = serversock.accept();
				executor.execute(new FrontEndProcessor(sock, frontendServer.cache, 
						host, port, logger));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void run() {
		logger.info("thread starts");
		doPostToDiscovery(ip);
		String[] hostport = doBackendGet().split("\\:");
		host = hostport[0];
		port = Integer.parseInt(hostport[1].toString());
		//cache.setCache(host, port);
		
	}
	
	/*
	 * send its own ip address to discovery(FE to discovery)
	 * 
	 * uri: POST /discovery/frontend
	 */
	private void doPostToDiscovery(String localip) {
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("host.json"));
			JSONObject jsonObject = (JSONObject) obj;
			String host = (String) jsonObject.get("host");
			int discoveryPort = Integer.parseInt(jsonObject.get("port").toString());
			// String ip = "localhost:3098";
			String path = "/discovery/frontend";
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
			socket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Doing loadBalancer
	 * get the backend ip which FE can connect with it(FE to discovery)
	 * 
	 * uri: GET /discovery/backend
	 */
	public String doBackendGet() {
		String responsebody = null;
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader("host.json"));
			JSONObject jsonObject = (JSONObject) obj;
			String host = (String)jsonObject.get("host");
			int port = Integer.parseInt(jsonObject.get("port").toString());
			String path = "/discovery/backend";
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

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return responsebody;

	}

}
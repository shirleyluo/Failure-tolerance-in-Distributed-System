import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class DiscoveryServer extends Thread{

	private static Logger logger = LogManager.getLogger(DiscoveryServer.class);
	ServersInfo info = new ServersInfo();
	
	public static void main(String[] args) throws Exception {
		
		DiscoveryServer discoveryServer = new DiscoveryServer();
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(new FileReader("host.json"));
		JSONObject jsonObject = (JSONObject) obj;
		int port = Integer.parseInt(jsonObject.get("port").toString());
		ServerSocket serversock = new ServerSocket(port);
		
		ExecutorService executor = Executors.newFixedThreadPool(10);
		discoveryServer.start();
		while (true) {
			
			Socket sock = serversock.accept();
			executor.execute(new DiscoveryServerProcessor(sock, discoveryServer.info, logger));
		}
	}
	
	public void run() {
		
		while(true) {
			String backendList = info.getBackendInfo();
			System.out.println(backendList);
			if(info.getBackendList(backendList) != null) {
				
				sendPing(info.getBackendList(backendList));
				try {
					Thread.sleep(3000);
				} catch (InterruptedException interruptedException) {
	
					System.out
							.println("Thread is interrupted when it is sleeping"
									+ interruptedException);
				}
			}
		}
		
		
	}
	
	/*
	 * send ping to all BEs in order to check it is alive or not(Discovery to BE)
	 * 
	 * uri GET /check
	 */
	public void sendPing(ArrayList<String> list) {
		String path = "/check";
		String request = "GET " + path + " HTTP/1.1\r\n"
				+ "Content-Type: application/json\r\n"
				+ "\r\n";
		for(int i = 0; i < list.size(); i++) {
			String address =list.get(i);
			String[] hostport = address.split("\\:");
			String host = hostport[0];
			int port = Integer.parseInt(hostport[1]);
			try {
				Socket socket = new Socket(host, port);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				
				out.write(request);
				out.flush();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String line = "";
				//System.out.println(in.readLine());
				while(!(line = in.readLine().trim()).equals(""));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println(address + " this server has down");
				list.remove(address);
				info.remove(address);
				System.out.println("update backend list: " + list);
				sendFail(list, address);
			}
		}
	}
	
	/*
	 * tell all BE servers which server is failed(Discovery to BE)
	 * 
	 * uri: POST /whoisfailed
	 */
	public void sendFail(ArrayList<String> list, String ip) {
		String path = "/whoisfailed";
		String request = "POST " + path + " HTTP/1.1\r\n"
				+ "Content-Type: application/json\r\n"
				+ "\r\n";
		for(int i = 0; i < list.size(); i++) {
			String address =list.get(i);
//			System.out.println("********" + address);
			String[] hostport = address.split("\\:");
			String host = hostport[0];
			int port = Integer.parseInt(hostport[1]);
			try {
				Socket socket = new Socket(host, port);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				
				out.write(request);
				out.write(ip);
				out.flush();
				
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String line = "";
				//System.out.println(in.readLine());
				while(!(line = in.readLine().trim()).equals(""));

			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
//				System.out.println(address + " this server has down");
//				list.remove(address);
//				System.out.println("update backend list: " + list);
			}
		}
	}
}

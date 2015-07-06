import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.text.html.HTMLEditorKit.Parser;

import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class DiscoveryServerProcessor implements Runnable {

	private Socket sock;
	private Logger logger;
	private HTTPRequestLineParser httpParser;
	private ResponseType type;
	private ServersInfo info;
	
	public DiscoveryServerProcessor(Socket sock, ServersInfo info, Logger logger) {
		// TODO Auto-generated constructor stub
		this.sock = sock;
		this.logger = logger;
		httpParser = new HTTPRequestLineParser();
		type = new ResponseType();
		this.info = info;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		BufferedReader in = null;
		String line = null;
//		String url = null;
		BufferedWriter out = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					sock.getInputStream()));
		
			out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			
			String httpline = in.readLine();
			String[] lines = httpline.split(" ");
			
			String responsebody = null;
			String responseheaders = null;
			ArrayList<String> responseArray = new ArrayList<String>();
			
			//catch the invalid HTTPRequest
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

			if(httpParser.parse(httpline).getUripath().equals("/discovery/frontend")) {
				if(lines[0].equals(HTTPConstants.HTTPMethod.valueOf("POST").toString())) {
					StringBuilder stringBuilder = new StringBuilder();
					char[] cbuf = new char[4096];
					
					while(!(line = in.readLine().trim()).equals(""));
					if(in.ready()) {
						stringBuilder.append(cbuf, 0, in.read(cbuf));
						String body = stringBuilder.toString();
						info.setFrontendMap(body);
					}
					responsebody = "<html><body> Created!! </body></html>\r\n";
					responseheaders = "HTTP/1.1 201 Created\n" +
							"Content-Length: " + responsebody.getBytes().length + "\n\n";
					out.write(responseheaders);
					out.write(responsebody);
					out.flush();
					out.close();
					sock.close();
				} 
				else if(lines[0].equals(HTTPConstants.HTTPMethod.valueOf("GET").toString())) {
					String ip = info.getFrontendIP();
					responsebody = ip;
					info.updateFrontendMap(ip);
					
					responseheaders = "HTTP/1.1 200 OK\n" +
							"Content-Length: " + responsebody.getBytes().length + "\n\n";
					out.write(responseheaders);
					out.write(responsebody);
					out.flush();
					out.close();
					sock.close();
				}
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/discovery/newBE")) {
//				String backendList = info.getBackendInfo();
//				ArrayList<String> list = info.getBackendList(backendList);
				StringBuilder stringBuilder = new StringBuilder();
				char[] cbuf = new char[4096];
				String ip = null;
				
				while(!(line = in.readLine().trim()).equals(""));
				if(in.ready()) {
					stringBuilder.append(cbuf, 0, in.read(cbuf));
					ip = stringBuilder.toString();
					//System.out.println(body);
					
				}
				String backendList = info.getBackendInfo();
				ArrayList<String> list = info.getBackendList(backendList);
				list.remove(ip);
				String body = info.getRandom(list);
//				System.out.println(responsebody);
				if(body == null) {
					responseArray = type.BadRequest();
					responsebody = responseArray.get(0);
					responseheaders = responseArray.get(1);
				} else {
					responsebody = body;
					responseheaders = "HTTP/1.1 200 OK\n" +
							"Content-Length: " + responsebody.getBytes().length + "\n\n";
				}
				
				out.write(responseheaders);
				out.write(responsebody);
				out.flush();
				out.close();
				sock.close();
			}
			
			if(httpParser.parse(httpline).getUripath().equals("/discovery/backend")) {
				if(lines[0].equals(HTTPConstants.HTTPMethod.valueOf("POST").toString())) {
					StringBuilder stringBuilder = new StringBuilder();
					char[] cbuf = new char[4096];
					
					while(!(line = in.readLine().trim()).equals(""));
					if(in.ready()) {
						stringBuilder.append(cbuf, 0, in.read(cbuf));
						String body = stringBuilder.toString();
						//System.out.println(body);
						info.setBackendMap(body);
						
					}
					responsebody = "<html><body> Created!! </body></html>\r\n";
					responseheaders = "HTTP/1.1 201 Created\n" +
							"Content-Length: " + responsebody.getBytes().length + "\n\n";
					out.write(responseheaders);
					out.write(responsebody);
					out.flush();
					out.close();
					sock.close();
					sendBackendInfo();
					
				}
				else if(lines[0].equals(HTTPConstants.HTTPMethod.valueOf("GET").toString())){
					String ip = info.getBackendIP();
					responsebody = ip;
					info.updateBackendMap(ip);
					
					responseheaders = "HTTP/1.1 200 OK\n" +
							"Content-Length: " + responsebody.getBytes().length + "\n\n";
					out.write(responseheaders);
					out.write(responsebody);
					out.flush();
					out.close();
					sock.close();
				}
			}
			
				
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.getMessage();
		
		} 
	}

	/*
	 * send the bacend information to all servers if a new server starts(Discovery to BE)
	 * 
	 * uri: POST /information
	 */
	public void sendBackendInfo() {
		try {
			String list = info.getBackendInfo();
			String path = "/information";
			String request = "POST " + path + " HTTP/1.1\r\n"
					+ "Content-Length:" + list.toString().length() + "\r\n"
					+ "Content-Type: application/json\r\n"
					+ "\r\n";
			Socket socket;
			
			JSONParser parser = new JSONParser();
			JSONObject listObj = (JSONObject) parser.parse(list);
			JSONArray listArray = (JSONArray) listObj.get("backendList");
			for(int i = 0; i < listArray.size(); i++) {
				String[] hostport = listArray.get(i).toString().split("\\:");
				String host = hostport[0];
				int port = Integer.parseInt(hostport[1]);

				socket = new Socket(host, port);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream()));
				out.write(request);
				out.write(list);
				out.flush();

				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				String line = "";
//				StringBuilder stringBuilder = new StringBuilder();
//				char[] cbuf = new char[4096];
				while (!(line = in.readLine().trim()).equals(""));
				socket.close();
			}
			
//			socket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

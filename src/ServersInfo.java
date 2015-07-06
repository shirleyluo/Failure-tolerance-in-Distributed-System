import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ServersInfo {
	
	private HashMap<String, Integer> backendInfo;
	private HashMap<String, Integer> frontendInfo;
	MultiReaderLock lock;

	public ServersInfo() {
		backendInfo = new HashMap<String, Integer>();
		frontendInfo = new HashMap<String, Integer>();
		lock = new MultiReaderLock();
		//primary = new HashMap<String, String>();
	}
	
	 /*
	  * Doing loadBalancer for FE
	  * HashMap: IP=0 when starting a new dataserver
	  */
	public void setBackendMap(String IP) {
		lock.lockWrite();
		backendInfo.put(IP, 0);
		lock.unlockWrite();
		System.out.println("Backend: " + backendInfo);
	}
	
	/*
	 * Doing loadBalancer for FE
	 * Get minimum value in information map
	 */
	public String getBackendIP() {
		lock.lockRead();
		String ip = null;
		int min = Collections.min(backendInfo.values());
		//System.out.println(min);
		
		for(String key: backendInfo.keySet()) {
			if(backendInfo.get(key) == min) {
				ip = key;
				break;
			}
		}
		lock.unlockRead();
		return ip;
	}
	
	/*
	 * Update the backendInfo map value
	 * The value plus 1 if this ip assigned to be connected by FE
	 */
	public void updateBackendMap(String ip) {
		lock.lockRead();
		int value = backendInfo.get(ip) + 1;
		lock.unlockRead();
		lock.lockWrite();
		backendInfo.put(ip, value);
		lock.unlockWrite();
		System.out.println("Backend update: " + backendInfo);
	}
	
	/*
	  * Doing loadBalancer for FE
	  * HashMap: IP=0 when starting a new dataserver
	  */
	public void setFrontendMap(String IP) {
		lock.lockWrite();
		frontendInfo.put(IP, 0);
		lock.unlockWrite();
		System.out.println("Frontend: " + frontendInfo);
	}
	
	/*
	 * Doing loadBalancer for FE
	 * Get minimum value in information map
	 */
	public String getFrontendIP() {
		lock.lockRead();
		String ip = null;
		int min = Collections.min(frontendInfo.values());
		//System.out.println(min);
		
		for(String key: frontendInfo.keySet()) {
			if(frontendInfo.get(key) == min) {
				ip = key;
				break;
			}
		}
		lock.unlockRead();
		//System.out.println(ip);
		return ip;
	}
	
	/*
	 * Update the backendInfo map value
	 * The value plus 1 if this ip assigned to be connected by FE
	 */
	public void updateFrontendMap(String ip) {
		lock.lockRead();
		int value = frontendInfo.get(ip) + 1;
		lock.unlockRead();
		lock.lockWrite();
		frontendInfo.put(ip, value);
		lock.unlockWrite();
		System.out.println("Frontend updte: " + frontendInfo);
	}
	
	/*
	 * get backend infomation(During broadcast: covert to string)
	 */
	public String getBackendInfo() {
		JSONObject backendList = new JSONObject();
		JSONArray ips = new JSONArray();
		for(String key : backendInfo.keySet()) {
			ips.add(key);
		}
		lock.lockWrite();
		backendList.put("backendList", ips);
		lock.unlockWrite();
		return backendList.toString();
	}
	
	/*
	 * get backend ArrayList
	 */
	public ArrayList<String> getBackendList(String list) {
		ArrayList<String> backendList = new ArrayList<String>();
		try {
			
			JSONParser parser = new JSONParser();
			JSONObject listObj;
			
				listObj = (JSONObject) parser.parse(list);
			
			JSONArray listArray = (JSONArray) listObj.get("backendList");
			lock.lockWrite();
			for(int i = 0; i < listArray.size(); i++) {
				if(!backendList.contains(listArray.get(i))) {
					backendList.add(listArray.get(i).toString());
				}
				
			}
			lock.unlockWrite();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return backendList;
	}
	
	/*
	 * remove one element in ArrayList(backendList)
	 */
	public void remove(String ip) {
		backendInfo.remove(ip);
		
	}
	
	/*
	 * get the random ip address
	 */
	public String getRandom(ArrayList<String> list) {
		String ip;
		if(list.size() == 0) {
			ip = null;
		} else {
			Random randomGenerator = new Random();
			int index = randomGenerator.nextInt(list.size());
			ip = list.get(index);
		}
		return ip;
	}
	
}

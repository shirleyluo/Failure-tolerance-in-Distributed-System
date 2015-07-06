import java.util.HashMap;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Cache {
	private HashMap<String, TreeMap<String, Object>> cache = 
			new HashMap<String, TreeMap<String, Object>>();
	private HashMap<String, Integer> timestamp = new HashMap<String, Integer>();
	MultiReaderLock lock = new MultiReaderLock();
	
	@SuppressWarnings("unchecked")
	public void updateCache(String responsebody) 
			throws ParseException {
		//HashMap<String, TreeMap<String, Object>> users = cache.get(ip);
//		System.out.println(users);
		TreeMap<String, Object> tweets = new TreeMap<String, Object>();
		
		JSONParser parser = new JSONParser();
		Object bodyObj = parser.parse(responsebody);
		JSONObject bodyjs = (JSONObject) bodyObj;
		
		String searchterm = (String) bodyjs.get("q");
		String user = (String) bodyjs.get("user");
		int version = Integer.parseInt(bodyjs.get("version").toString());
		
		if(cache.containsKey(user)) {
			tweets = cache.get(user);
		}

		JSONArray tweetsArray = new JSONArray();
		tweetsArray = (JSONArray) bodyjs.get("tweets");
		
		JSONArray timestampArray = (JSONArray) bodyjs.get("timestamp");

		JSONObject js = new JSONObject();
		js.put("tweets", tweetsArray);
		js.put("version", version);
		
		lock.lockWrite();
		tweets.put(searchterm, js);
		cache.put(user, tweets);
		System.out.println("Update Cache: " + cache);
		lock.unlockWrite();	
		lock.lockWrite();
		for(int i = 0; i < timestampArray.size(); i++) {
			String[] list = timestampArray.get(i).toString().split("\\=");
			timestamp.put(list[0], Integer.parseInt(list[1]));
		}
		
		
		lock.unlockWrite();
		System.out.println("Update Timestamp: " + timestamp);
	}
	
	/*
	 * get search result to client 
	 */
	
	public String get(String searchterm, String user) {
		JSONObject resultObj = new JSONObject();
		JSONArray value = new JSONArray();
		lock.lockRead();
		//HashMap<String, TreeMap<String, Object>> users = cache.get(ip);
		if(cache.containsKey(user)) {
			if(cache.get(user).containsKey(searchterm)) {
				JSONObject object = (JSONObject) cache.get(user).get(searchterm);
				value =  (JSONArray) object.get("tweet");
				resultObj.put("q", searchterm);
				resultObj.put("tweets", value);
			} else {
				resultObj.put("q", searchterm);
				resultObj.put("tweets", value);
			}
			
		} else {
			resultObj.put("q", searchterm);
			resultObj.put("tweets", value);
		}
		lock.unlockRead();
		return resultObj.toString();
	}
	
	
	/*
	 * get the versionnum in cache data
	 * @param cacheData
	 * @param searchterm
	 * @return
	 * @throws ParseException
	 */
	public int getCacheVersion(String host, int port, String searchterm) throws ParseException {
		int version;
		String ip = host + ":" + port;
		lock.lockRead();
		if(!timestamp.containsKey(ip)) {
			version = 0;
		} else {
			
			version = timestamp.get(ip);
		}
		lock.unlockRead();
		return version;
	}
	
//	/*
//	 * set cache map when a new frontend starts
//	 */
//	public void setCache(String host, int port) {
//		String ip = host + ":" + port;
//		HashMap<String, TreeMap<String, Object>> users = new
//				HashMap<String, TreeMap<String, Object>>();
//		cache.put(ip, users);
//
//	}
	
	public HashMap<String, TreeMap<String, Object>> getCache() {
		
		return cache;
	}
	
	/*
	 * get versionnum(send this version to BE to compare)
	 */
	public int getVersion(String ip, String username, String searchterm) {
		int version;
		//HashMap<String, TreeMap<String, Object>> users = cache.get(ip);
		if(cache.size() == 0) {
			version = 0;
		}
		else if(cache.get(username).containsKey(searchterm)) {
			Object object = cache.get(username).get(searchterm);
			JSONObject jsonobject = (JSONObject) object;
			version = Integer.parseInt(jsonobject.get("version").toString());
		} else {
			version = 0;
		}
		
		return version;
	}
	
	/*
	 * get timestamp body(During compare FE and BE's timestamps)
	 */
	public String getTimestampBody() {
		JSONObject object = new JSONObject();
		String body = null;
		if(timestamp.size() != 0) {
			for(String key : timestamp.keySet()) {
				object.put(key, timestamp.get(key));
				body = object.toString();
			}
		}
		
		return body;
	}
	
	/*
	 * store the information when a new BE server starts
	 */
	public void storeInfo(String body) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject object = (JSONObject) parser.parse(body);
			JSONArray userArray = (JSONArray) object.get("users");
			JSONArray timestampsArray = (JSONArray) object.get("timestamp");
			for(int i = 0; i < timestampsArray.size(); i++) {
				String[] list = timestampsArray.get(i).toString().split("\\=");
				timestamp.put(list[0], Integer.parseInt(list[1]));
			}
//			JSONArray accountArray = (JSONArray) object.get("account");
//			for(int i = 0; i < accountArray.size(); i++) {
//				String[] list = accountArray.get(i).toString().split("\\=");
//				account.put(list[0], list[1]);
//			}
			for(int i = 0; i < userArray.size(); i++) {
				JSONObject userObject = (JSONObject) parser.parse(
						userArray.get(i).toString());
				String user = (String) userObject.get("user");
				JSONArray treemapArray = (JSONArray) userObject.get("treemap");
				TreeMap<String, Object> tweets = new TreeMap<String, Object>();
				for(int j = 0; j < treemapArray.size(); j++) {
					JSONObject HashtagObject = (JSONObject) parser.parse(
							treemapArray.get(j).toString());
					String hashtag = (String) HashtagObject.get("hashtag");
					JSONArray tweetsArray = (JSONArray) HashtagObject.get("tweets");
					for(int z = 0; z < tweetsArray.size(); z++) {
						JSONObject tweetObject = (JSONObject) parser.parse(
								tweetsArray.get(z).toString());
						
						tweets.put(hashtag, tweetObject);
					}
					
				}
				cache.put(user, tweets);
			}
			System.out.println("CACHE: " + cache);
			System.out.println("TIMESTAMP:" + timestamp);
//			System.out.println("ACCOUNT: " + account);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

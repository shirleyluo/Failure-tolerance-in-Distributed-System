import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class Data {
	
	MultiReaderLock lock;
	private HashMap<String, String> account = new HashMap<String, String>();
//	private HashMap<String, HashMap<String, TreeMap<String, Object>>> results = 
//			new HashMap<String, HashMap<String, TreeMap<String, Object>>>();
	private HashMap<String, HashMap<String, TreeMap<String, Object>>> log = 
			new HashMap<String, HashMap<String, TreeMap<String, Object>>>();
	private HashMap<String, TreeMap<String, Object>> results = new 
			HashMap<String, TreeMap<String, Object>>();
//	HashMap<String, Object> tweets = new HashMap<String, Object>();
	private HashMap<String, Integer> timestamp = new HashMap<String, Integer>();
	ArrayList<String> backendList = new ArrayList<String>();
	
	public Data() {
		lock = new MultiReaderLock();
	}
	
	/*
	 * During login
	 * check if the username registers or not
	 * then check whether the password is right or not
	 */
	public Boolean login(String username, String password) {
		Boolean flag = true;
		lock.lockRead();
		if(!account.containsKey(username)) {
			flag = false;
			return flag;
		}
		if(!account.get(username).equals(password)) {
			flag = false;
			return flag;
		}
		lock.unlockRead();
		return flag;
	}
	
	public Boolean Register(String username, String password) {
		Boolean flag = true;
		//lock.lockWrite();
		if(!account.containsKey(username)) {
			account.put(username, password);
		} else {
			flag = false;
		}
		//lock.unlockWrite();
		System.out.println("AccountInformation map: " + account);
		//System.out.print(data);
		return flag;
	}
	
	/*
	 * set results map when a new dataserver starts
	 */
	public void setResultsMap(String ip) {
		HashMap<String, TreeMap<String, Object>> users = new
				HashMap<String, TreeMap<String, Object>>();
		log.put(ip, users);
	}
	
	/*
	 * Analysis body and store them in an hashmap
	 * the format is: user = ["hashtags", {"tweets", "version"}]
	 * @throws ParseException 
	 */
	@SuppressWarnings("unchecked")
	public void storeTweet(String ip, String body) throws ParseException{
//		HashMap<String, TreeMap<String, Object>> users = results.get(ip);
		TreeMap<String, Object> tweets = new TreeMap<String, Object>();
		JSONParser parser = new JSONParser();
		Object bodyObj = parser.parse(body);
		JSONObject jObject = (JSONObject) bodyObj;
		String tweet = (String)jObject.get("tweet");
		String user = (String) jObject.get("user");
		
		JSONArray tweetValue = new JSONArray();
		tweetValue.add(tweet);
		
		JSONObject js = new JSONObject();
		js.put("tweet", tweetValue);
		js.put("version", 1);
		
		JSONArray hashtagsArray = new JSONArray();
		hashtagsArray = (JSONArray) jObject.get("hashtags");
		
//		lock.lockWrite();
		for(int i = 0; i < hashtagsArray.size(); i++) {
						
			tweets.put(hashtagsArray.get(i).toString(), js);
		}
		
		if(results.containsKey(user)) {
			addTweet(results.get(user), body);
		} else {
			results.put(user, tweets);
//			System.out.println(users);
		}
//		results.put(user, results);
//		lock.unlockWrite();
		System.out.println("Results: "+ results);
		
	}
	
	/*
	 * Do some store if add a new tweet(whether there is already structure in memory)
	 * @param data
	 * @param body
	 * @throws ParseException 
	 */
	
	@SuppressWarnings("unchecked")
	public void addTweet(TreeMap<String, Object> treeMap, String body) throws ParseException {
		
		JSONParser parser = new JSONParser();
		JSONObject object = (JSONObject) parser.parse(body);
		JSONArray hashtags = new JSONArray();
		hashtags = (JSONArray)object.get("hashtags");
		String tweet = (String) object.get("tweet");
		
		for(int i = 0; i < hashtags.size(); i++) {
			
			if(treeMap.containsKey(hashtags.get(i))) {
				
				JSONObject mapvalue = (JSONObject) parser.parse(treeMap.get(hashtags.get(i)).toString());
				JSONArray maptweets = (JSONArray) mapvalue.get("tweet");
				int version = Integer.parseInt(mapvalue.get("version").toString());
				
				maptweets.add(tweet);
				mapvalue.put("tweet", maptweets);
				mapvalue.put("version", version + 1);
				
				treeMap.put(hashtags.get(i).toString(), mapvalue);
			} else {
				
				JSONObject newjs = new JSONObject();
				JSONArray newtwvalue = new JSONArray();
				newtwvalue.add(tweet);
				
				newjs.put("tweet", newtwvalue);
				newjs.put("version", 1);
				
				treeMap.put(hashtags.get(i).toString(), newjs);
				
			}
			
		}
	
	}
	
	/*
	 * set timestamp when dataserver started
	 */
	public void setTimestamp(String ip) {
		timestamp.put(ip, 0);
	}
	
	/*
	 * version plus 1 if backend received POST or DELETE method
	 */
	public void updateTimestamp(String ip) {
		timestamp.put(ip, timestamp.get(ip) + 1);
		System.out.println("Timestamp map: " + timestamp);
	}
	
	/*
	 * update the timestamp and log after broadcast
	 */
	public void updateBroadcast(String body, String localip) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject object = (JSONObject) parser.parse(body);
		JSONArray timestamps = (JSONArray) object.get("timestamp");
		String ip = (String) object.get("ip");
		log.put(ip, results);
		log.put(localip, results);
		
		for(int i = 0; i < timestamps.size(); i++) {
			String[] lines = timestamps.get(i).toString().split("\\=");
			String host = lines[0];
			int version = Integer.parseInt(lines[1]);
			if(timestamp.containsKey(host)) {
				if(version > timestamp.get(host)) {
					timestamp.put(host, version);
				}
			} else {
				timestamp.put(host, version);
			}
		}
		System.out.println("Update timestamp after broadcast: " + timestamp);
		System.out.println("Update log after broadcast: " + log);
	}
	
	/*
	 * get the version number in backend
	 */
	public int getVersion(String ip, String user, String searchterm) {
		int version;
		System.out.println(results);
		if(results.containsKey(user)) {
			if(results.get(user).containsKey(searchterm)) {
				Object object = results.get(user).get(searchterm);
				JSONObject jsonobject = (JSONObject) object;
				version = Integer.parseInt(jsonobject.get("version").toString());
			} else {
				version = 0;
			}
		} else {
			TreeMap<String, Object> tweets = new TreeMap<String, Object>();
			results.get(ip).put(user, tweets);
			version = 0;
		}
		
		
		return version;
	}
	
	/*
	 * get value if the two version numbers are not same from BE
	 * 
	 * {"user": username, "q": "searchterm", 
	 * "version": "versionnum","tweets": ["tw1", "tw2"], "timestamp": ["ip1", "ip2"]}
	 */
	public String getValue(String searchterm, String user) throws ParseException {
		String responsebody = null;
		JSONObject responsebodyjs = new JSONObject();
		JSONArray tweetsArray = new JSONArray();
		//HashMap<String, TreeMap<String, Object>> users = results.get(ip);
		
		if(results.containsKey(user)) {
			JSONArray timestampArray = new JSONArray();
			if(results.get(user).containsKey(searchterm)) {
				JSONParser jsonParser = new JSONParser();
				JSONObject object = (JSONObject) jsonParser.parse(
						results.get(user).get(searchterm).toString());
				tweetsArray = (JSONArray)object.get("tweet");
				int version = Integer.parseInt(object.get("version").toString());
				
				for(String key : timestamp.keySet()) {
					timestampArray.add(key + "=" + timestamp.get(key));
				}
//				int oldversion = getDataServerVersion(ip);
				responsebodyjs.put("user", user);
				responsebodyjs.put("q", searchterm);
				responsebodyjs.put("version", version);
				responsebodyjs.put("tweets", tweetsArray);
				responsebodyjs.put("timestamp", timestampArray);
				responsebody = responsebodyjs.toString();
			} else {
				responsebodyjs.put("user", user);
				responsebodyjs.put("q", searchterm);
				responsebodyjs.put("version", 0);
				responsebodyjs.put("tweets", tweetsArray);
				responsebodyjs.put("timestamp", timestampArray);
				responsebody = responsebodyjs.toString();
			}
			
		} else {
			responsebodyjs.put("user", user);
			responsebodyjs.put("q", searchterm);
			responsebodyjs.put("tweets", tweetsArray);
			responsebody = responsebodyjs.toString();
		}
		return responsebody;
	}
	
	/*
	 * get information body when a new server starts
	 */
	/*
	 * get value if the two version numbers are not same from BE
	 * 
	 * {"user": username, "tweetsMap":
	 * "version": "versionnum","tweets": ["tw1", "tw2"], "timestamp": ["ip1", "ip2"]}
	 */
//	public String getInfoBody(String ip) throws ParseException {
//		String responsebody = null;
//		JSONObject responsebodyjs = new JSONObject();
//		JSONArray tweetsArray = new JSONArray();
//		HashMap<String, TreeMap<String, Object>> users = results.get(ip);
//		
//		for(String key : users.keySet()) {
//			TreeMap<String, Object> treemap = users.get(key);
//			for(String key1 : treemap.keySet()) {
//				responsebodyjs
//			}
//		}
//		
//		if(users.containsKey(user)) {
//			JSONArray timestampArray = new JSONArray();
//			if(users.get(user).containsKey(searchterm)) {
//				JSONParser jsonParser = new JSONParser();
//				JSONObject object = (JSONObject) jsonParser.parse(
//						users.get(user).get(searchterm).toString());
//				tweetsArray = (JSONArray)object.get("tweet");
//				int version = Integer.parseInt(object.get("version").toString());
//				
//				for(String key : timestamp.keySet()) {
//					timestampArray.add(key + "=" + timestamp.get(key));
//				}
////				int oldversion = getDataServerVersion(ip);
//				responsebodyjs.put("user", user);
//				responsebodyjs.put("q", searchterm);
//				responsebodyjs.put("version", version);
//				responsebodyjs.put("tweets", tweetsArray);
//				responsebodyjs.put("timestamp", timestampArray);
//				responsebody = responsebodyjs.toString();
//			} else {
//				responsebodyjs.put("user", user);
//				responsebodyjs.put("q", searchterm);
//				responsebodyjs.put("version", 0);
//				responsebodyjs.put("tweets", tweetsArray);
//				responsebodyjs.put("timestamp", timestampArray);
//				responsebody = responsebodyjs.toString();
//			}
//			
//		} else {
//			responsebodyjs.put("user", user);
//			responsebodyjs.put("q", searchterm);
//			responsebodyjs.put("tweets", tweetsArray);
//			responsebody = responsebodyjs.toString();
//		}
//		return responsebody;
//	}
	
	/*
	 * set and update backend informations
	 */
	public void updateBackendInfo(String body) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject listObj = (JSONObject) parser.parse(body);
		JSONArray listArray = (JSONArray) listObj.get("backendList");
		
		for(int i = 0; i < listArray.size(); i++) {
			if(!backendList.contains(listArray.get(i))) {
				backendList.add(listArray.get(i).toString());
			}
			
		}
		System.out.println("Update BackEndInfo: " + backendList);
	}
	
	/*
	 * get the backend informations
	 */
	public ArrayList<String> getBackendInfo() {
		return backendList;
	}
	
//	/*
//	 * get timestamp
//	 */
//	public HashMap<String, Integer> getTimestamp(String body) {
//		return timestamp;
//	}
	
	
	/*
	 * get broadcast body
	 */
	public String getBroadcastBody(String body, String ip) throws ParseException {
		JSONParser parser = new JSONParser();
		JSONObject object = (JSONObject) parser.parse(body);
		String tweet = (String) object.get("tweet");
		String user = (String) object.get("user");
		JSONArray hashtags = (JSONArray) object.get("hashtags");
		
		JSONArray timestamps = new JSONArray();
		for(String key : timestamp.keySet()) {
			timestamps.add(key + "=" + timestamp.get(key));
		}
		
		JSONObject newObj = new JSONObject();
		newObj.put("ip", ip);
		newObj.put("tweet", tweet);
		newObj.put("hashtags", hashtags);
		newObj.put("user", user);
		newObj.put("timestamp", timestamps);
		
		return newObj.toString();
	}
	
	/*
	 * get timestamp body(During compare FE and BE's timestamps)
	 */
	public String getTimestampBody() {
		JSONObject object = new JSONObject();
		for(String key : timestamp.keySet()) {
			object.put(key, timestamp.get(key));
		}
		return object.toString();
	}
	
	/*
	 * compare two timestamps
	 */
	public Boolean compare(String body) {
		Boolean flag = false;
		try {
			JSONParser parser = new JSONParser();
			JSONObject object = (JSONObject) parser.parse(body);
//			if(timestamp.size() == 0) {
//				return flag;
//			}
			for(String key : timestamp.keySet()) {
				if(object.containsKey(key)) {
					if(Integer.parseInt(object.get(key).toString()) <=
					timestamp.get(key)) {
						flag = true;
						return flag;
					}
				} 
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	
	public Boolean compareEqual(String body) {
		Boolean flag = true;
		try {
			JSONParser parser = new JSONParser();
			JSONObject object = (JSONObject) parser.parse(body);
//			if(timestamp.size() == 0) {
//				return flag;
//			}
			for(String key : timestamp.keySet()) {
				if(object.containsKey(key)) {
					if(Integer.parseInt(object.get(key).toString()) !=
					timestamp.get(key)) {
						flag = false;
						return flag;
					}
				} 
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
	
	public Boolean eventualCompare(HashMap<String, Integer> map) {
		Boolean flag = false;
		for(String key : map.keySet()) {

			if(timestamp.containsKey(key)) {
				if(map.get(key) < timestamp.get(key)) {
					flag = true;
					return flag;
				} 
			}
			
		}
		return flag;
	}
	
	/*
	 * get the users map(the the value of ip)
	 */
	public String getInformation() {
		JSONObject object = new JSONObject();
		
		JSONArray userArray = new JSONArray();
		for(String user : results.keySet()) {
			
			JSONObject userObject = new JSONObject();
			TreeMap<String, Object> tweets = results.get(user);
			JSONArray treemapArray = new JSONArray();
			for(String hashtag : tweets.keySet()) {
				JSONArray tweetsArray = new JSONArray();
				JSONObject HashtagObject = new JSONObject();
				
				tweetsArray.add(tweets.get(hashtag));
				HashtagObject.put("hashtag", hashtag);
				HashtagObject.put("tweets", tweetsArray);
				treemapArray.add(HashtagObject);
			}
			userObject.put("user", user);
			userObject.put("treemap", treemapArray);
			userArray.add(userObject);
			object.put("users", userArray);
			JSONArray timestampList = new JSONArray();
			for(String key : timestamp.keySet()) {
				timestampList.add(key + "=" + timestamp.get(key));
			}
//			JSONArray accountList = new JSONArray();
//			for(String key : account.keySet()) {
//				accountList.add(key + "=" + account.get(key));
//			}
			object.put("timestamp", timestampList);
//			object.put("treemap", treemapArray);
		}
		return object.toString();
	}
	
	/*
	 * get the information from Log map
	 */
	public String getLogInformation(String ip) {
//		System.out.println(log.containsKey(ip));
		if(!log.containsKey(ip)) {
			return null;
		}
		HashMap<String, TreeMap<String, Object>> map = log.get(ip);
		JSONObject object = new JSONObject();
		
		JSONArray userArray = new JSONArray();
		for(String user : map.keySet()) {
			
			JSONObject userObject = new JSONObject();
			TreeMap<String, Object> tweets = map.get(user);
			JSONArray treemapArray = new JSONArray();
			for(String hashtag : tweets.keySet()) {
				JSONArray tweetsArray = new JSONArray();
				JSONObject HashtagObject = new JSONObject();
				
				tweetsArray.add(tweets.get(hashtag));
				HashtagObject.put("hashtag", hashtag);
				HashtagObject.put("tweets", tweetsArray);
				treemapArray.add(HashtagObject);
			}
			userObject.put("user", user);
			userObject.put("treemap", treemapArray);
			userArray.add(userObject);
			object.put("users", userArray);
			JSONArray timestampList = new JSONArray();
			for(String key : timestamp.keySet()) {
				timestampList.add(key + "=" + timestamp.get(key));
			}
//			JSONArray accountList = new JSONArray();
			object.put("timestamp", timestampList);
//			object.put("treemap", treemapArray);
		}
		return object.toString();
	}
	
	/*
	 * store information to complete eventual consistency
	 */
	public void storeEventual(String body) {
		JSONParser parser = new JSONParser();
		HashMap<String, Integer> Newtimestamp = new HashMap<String, Integer>();
		try {
			JSONObject object = (JSONObject) parser.parse(body);
			JSONArray userArray = (JSONArray) object.get("users");
			JSONArray timestampsArray = (JSONArray) object.get("timestamp");
			for(int i = 0; i < timestampsArray.size(); i++) {
				String[] list = timestampsArray.get(i).toString().split("\\=");
				Newtimestamp.put(list[0], Integer.parseInt(list[1]));
			}
			System.out.println("!!NEWTIMESTAMP: " + Newtimestamp);
			
			
			if(eventualCompare(Newtimestamp)) {
				return;
			}
			for(String key : Newtimestamp.keySet()) {
				if(timestamp.containsKey(key)) {
					if(Newtimestamp.get(key) > timestamp.get(key)) {
						timestamp.put(key, Newtimestamp.get(key));
					} 
				} else {
					timestamp.put(key, Newtimestamp.get(key));
				}
			}
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
				results.put(user, tweets);
			}
			System.out.println("EVENTUAL RESULTS: " + results);
			System.out.println("EVENTUAL TIMESTAMP:" + timestamp);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				results.put(user, tweets);
			}
			System.out.println("RESULTS: " + results);
			System.out.println("TIMESTAMP:" + timestamp);
//			System.out.println("ACCOUNT: " + account);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * store log
	 */
	public void storeLog(String ip) {
		log.put(ip, results);
		System.out.println("LOG: " + log);
	}
	
	public void deleteAll(String user) {
		if(results.containsKey(user)) {
			results.remove(user);
		}
		System.out.println("After DELETEALL" + results);
	}
	
	public void delete(String user, String term) {
		if(results.containsKey(user)) {
			if(results.get(user).containsKey(term)) {
				results.get(user).remove(term);
			}
		}
		System.out.println("After DELETE" + results);
	}
}

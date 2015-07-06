import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
HTTPRequestLine is a data structure that stores a Java representation of the parsed Request-Line.
 **/
public class HTTPRequestLine {

	private HTTPConstants.HTTPMethod method;
	private String uripath;
	private HashMap<String, String> parameters;
	private String httpversion;
	
    /*
      You are expected to add appropriate constructors/getters/setters to access and update the data in this class.
     */
	
	public HTTPRequestLine() {
		parameters = new HashMap<String, String>();
	}
	
	public void setMethod(HTTPConstants.HTTPMethod method) {
		this.method = method;
	}
	
	public HTTPConstants.HTTPMethod getMethod() {
		return this.method;
	}
	
	public void setUripath(String uripath) {
		this.uripath = uripath;
	}
	
	public String getUripath() {
		return this.uripath;
	}
	
	
	public void addParameter(String key, String value) {
		parameters.put(key, value);	
	}
	
	public HashMap<String, String> getHashMap() {
		return parameters;
	}
	
	public void setHttpversion(String httpversion) {
		this.httpversion = httpversion;
	}
	
	public String getHttpversion() {
		return this.httpversion;
	}
	
	/**
	 * FrontEnd: get the text
	 */
	public String getText(String body) throws ParseException {
		JSONParser parser = new JSONParser();
		Object bodyObj = parser.parse(body);
		JSONObject jObject = (JSONObject) bodyObj;
		String tweet = (String)jObject.get("text");
		return tweet;
	}
	
	/**
	 * FrontEnd: Analysis the text and get a json
	 * e.g. {"tweet": "#hello i am a #tweet", "hashtags":["hello", "tweet"]}
	 */
	@SuppressWarnings("unchecked")
	public JSONObject textAnalysis(String tweet) {
		JSONObject addtweetJson = new JSONObject();
		JSONArray hashtagsArray = new JSONArray();
		addtweetJson.put("tweet", tweet);
		if(!tweet.contains("#")) {
			return null;
		}
		String[] words = tweet.split(" ");
		for(int i = 0; i < words.length; i++) {
			
			if(words[i].contains("#")) {
				String word = words[i].substring(1, words[i].length());
				
				if(word.equals("")) {
					return null;
				}
				hashtagsArray.add(word);
			} 
			
		}
		addtweetJson.put("hashtags", hashtagsArray);
		
		return addtweetJson;
	}
	
//	public String getBody(String body) {
//		
//		return body;
//	}
}

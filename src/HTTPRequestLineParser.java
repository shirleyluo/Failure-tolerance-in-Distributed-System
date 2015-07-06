import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HTTPRequestLineParser {

	/**
	 * This method takes as input the Request-Line exactly as it is read from the socket.
	 * It returns a Java object of type HTTPRequestLine containing a Java representation of
	 * the line.
	 *
	 * The signature of this method may be modified to throw exceptions you feel are appropriate.
	 * The parameters and return type may not be modified.
	 *
	 * 
	 * @param line
	 * @return
	 */
	public static boolean flag = true;
	

	public static HTTPRequestLine parse(String line) {
		
	    //A Request-Line is a METHOD followed by SPACE followed by URI followed by SPACE followed by VERSION
	    //A VERSION is 'HTTP/' followed by 1.0 or 1.1
	    //A URI is a '/' followed by PATH followed by optional '?' PARAMS 
	    //PARAMS are of the form key'='value'&'
		
		HTTPRequestLine requestLine = new HTTPRequestLine();
		
		
		String[] myarrays;
		String[] parameters;
		myarrays = line.split(" ");
		if(myarrays.length != 3) {
			System.out.println("Invalid Request: Invalid length");
			return null;
		}
		try {
			requestLine.setMethod(HTTPConstants.HTTPMethod.valueOf(myarrays[0]));
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
			return null;
		}
			
		requestLine.setHttpversion(myarrays[2]);
		if(!(requestLine.getHttpversion().equals("HTTP/1.1") ||
				requestLine.getHttpversion().equals("HTTP/1.0"))) {
			System.out.println("Invalid Request: Invalid HTTP version");
			return null;
		} 
		
		
		requestLine.setUripath(myarrays[1]);
		//System.out.println("URI: " + myarrays[1]);
		String url = null;
		try {
			url = URLDecoder.decode(myarrays[1], "UTF-8");
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if(url.endsWith("?") || url.endsWith("&")) {
			System.out.println("Invalid Request: More than one '?' or No parameters");
			return null;
			
		} 
		if(url.contains("?")) {
		
			String[] array = url.split("\\?");
			requestLine.setUripath(array[0]);
			if(array.length != 2) {
				System.out.println("Invalid Request: More than one '?'");
				return null;
			}
			
			Pattern pattern = Pattern.compile("/[^?]+");
			Matcher matcher = pattern.matcher(array[0]);
			if(!matcher.matches()) {
				System.out.println("Invalid Request: Invalid uri path");
				return null;
			}
			String[] pair = array[1].split("&");
			for(int i = 0; i < pair.length; i++) {

				if(!pair[i].contains("=")) {
					System.out.println("Invalid Request: No '='");
					return null;
				}
				parameters = pair[i].split("=");
				if(parameters.length == 1 || parameters.length == 0) {
					System.out.println("Invalid Request: Missing query");
					return null;		
				} 
				if(parameters[0].equals("")) {
					System.out.println("Invalid Request: Missing query");
					return null;
				}
				if(requestLine.getHashMap().containsKey(parameters[0])) {
					System.out.println("Invalid Request: Same key names");
					return null;
				} 
				requestLine.addParameter(parameters[0], parameters[1]);

				
			}
			

		} else if(url.contains("=")){
			System.out.println("Invalid Request: Only parameters but no '?'");
			return null;
		}

		if(flag) {
			//System.out.println(requestLine.getHashMap());
		}
			
		return requestLine;
	}
	

		
}

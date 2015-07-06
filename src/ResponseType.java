import java.util.ArrayList;



public class ResponseType {
	
	public static String responsebody = "";
	public static String responseheaders = "";
	
	public ArrayList<String> BadRequest() {
		
		ArrayList<String> responseArray = new ArrayList<String>();
		
		String responsebody = "<html><body>Sorry! It's a Bad Request</body></html>\r\n";
		String responseheaders = "HTTP/1.1 400 Bad Request\n" +
						"Content-Length: " + responsebody.getBytes().length + "\n\n";
		
		responseArray.add(responsebody);
		responseArray.add(responseheaders);
		
		return responseArray;
		
		
	}
	
	public ArrayList<String> NotFound() {
		
		ArrayList<String> responseArray = new ArrayList<String>();
		String responsebody = "<html><body>Not Found!!</body></html>\r\n";
		
		String responseheaders = "HTTP/1.1 404 Not Found\n" +
						"Content-Length: " + responsebody.getBytes().length + "\n\n";
		
		responseArray.add(responsebody);
		responseArray.add(responseheaders);
		
		return responseArray;
	}
	
	public ArrayList<String> MethodNotAllowed() {
		
		ArrayList<String> responseArray = new ArrayList<String>();
		
		String responsebody = "<html><body>Sorry! The method is not allowed</body></html>\r\n";
		String responseheaders = "HTTP/1.1 405 MethodNotAllowed\n" +
						"Content-Length: " + responsebody.getBytes().length + "\n\n";
		
		responseArray.add(responsebody);
		responseArray.add(responseheaders);
		
		return responseArray;
		
		
	}
	
	public ArrayList<String> Valid() {
		
		ArrayList<String> responseArray = new ArrayList<String>();
		
		String responsebody = "<html><body>Yes!!</body></html>\r\n";
		String responseheaders = "HTTP/1.1 200 OK\n" +
						"Content-Length: " + responsebody.getBytes().length + "\n\n";
		
		responseArray.add(responsebody);
		responseArray.add(responseheaders);
		
		return responseArray;
		
		
	}
	
	public ArrayList<String> ServiceUnavailable() {
		
		ArrayList<String> responseArray = new ArrayList<String>();
		
		String responsebody = "<html><body>Sorry! DataServer is unavailable</body></html>\r\n";
		String responseheaders = "HTTP/1.1 503 Service Unavailable\n" +
						"Content-Length: " + responsebody.getBytes().length + "\n\n";
		
		responseArray.add(responsebody);
		responseArray.add(responseheaders);
		
		return responseArray;
		
		
	}
	public ArrayList<String> Created() {
		
		ArrayList<String> responseArray = new ArrayList<String>();
		
		String responsebody = "<html><body> Created successful!! </body></html>\r\n";
		String responseheaders = "HTTP/1.1 201 Created\n" +
						"Content-Length: " + responsebody.getBytes().length + "\n\n";
		
		responseArray.add(responsebody);
		responseArray.add(responseheaders);
		
		return responseArray;
		
		
	}

}

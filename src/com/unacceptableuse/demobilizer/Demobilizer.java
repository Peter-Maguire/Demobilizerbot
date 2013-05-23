package com.unacceptableuse.demobilizer;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

import sun.net.www.protocol.http.HttpURLConnection;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Demobilizer extends JFrame{

	
	
	private JsonObject rData;
	private String modhash;
	private CookieManager cookieManager ;
	private CookieStore cookieJar;
	
	private String[][] replacements = {
			{"y", "wikipedia.org"},
			{"m.reddit.com", "reddit.com"},
			{"m.facebook.com", "facebook.com"}
			};
	
	public Demobilizer()
	{
		
		cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);
		try {
		modhash = login("LinkDemobilizerBot", "******");
		} catch (IOException e) {
			e.printStackTrace();
		}
		scanSubreddit("todayilearned", 20);
	}
	
	
	public static void main(String[] args)
	{
/*		System.out.println("Attempting to login ");
		t*/
		
		new Demobilizer();
		
	}
	
	public String login(String user, String pw) throws IOException
	{
		URL url = null;
		try {
			 url = new URL("http://www.reddit.com/api/login/"+user+"?api_type=json&user="+user+"&passwd="+pw);
		} catch (MalformedURLException e) {} catch (IOException e) {
			System.err.println("Unable to login:");
			e.printStackTrace();
		}
	    HttpURLConnection ycConnection = null;
	    ycConnection = (HttpURLConnection) url.openConnection();
	    ycConnection.setRequestMethod("POST");
	    ycConnection.setDoOutput(true);
	    ycConnection.setUseCaches (false);
	    ycConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
	    
	    PrintWriter out = new PrintWriter(ycConnection.getOutputStream());
	    out.close();

	    BufferedReader in = new BufferedReader(new InputStreamReader(ycConnection.getInputStream()));
	    String response = in.readLine();
	    System.out.println(response);  
	    JsonParser parser = new JsonParser();
	  
	    cookieJar = cookieManager.getCookieStore();
	    List<HttpCookie> cookies = cookieJar.getCookies();
	    for(HttpCookie cookie : cookies)
	    {
	    	System.out.println("Received cookie "+cookie.getName()+" - "+cookie.getValue());
	    }
	    
	
	    
	    
	  String modhash = parser.parse(response).getAsJsonObject().get("json").getAsJsonObject().get("data").getAsJsonObject().get("modhash").getAsString().replace("\"", "");
	  return modhash;
	}
	
	public void scanSubreddit(String subreddit, int posts)
	{
		try {
			System.out.println("Attempting to connect to reddit "+subreddit);
			JsonParser parser = new JsonParser();
			URL redditlink = new URL("http://reddit.com/r/"+subreddit+".json");
			HttpURLConnection connection = (HttpURLConnection) redditlink.openConnection();
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			System.out.println("Successful! Parsing data...");
			
			JsonElement je = parser.parse(br);
			rData = je.getAsJsonObject();
			
			for(int x = 0; x < posts; x++)
			{
				System.out.println("Parsed data, grabbing listing "+x);
				JsonObject postData = getListing(x).getAsJsonObject();
				JsonObject post = postData.get("data").getAsJsonObject();
				
				int commentAmount = Integer.parseInt(post.get("num_comments").toString());
				System.out.println("Listing has "+commentAmount+" comments.");

				if(commentAmount != 0)
				{
					is.close();
					br.close();
					System.out.println("Connecting to comments section "+post.get("permalink"));

					URL commentsLink = new URL("http://reddit.com/"+post.get("permalink").getAsString().replace("\"", "")+".json");
					connection = (HttpURLConnection) commentsLink.openConnection();
					is = connection.getInputStream();
					br = new BufferedReader(new InputStreamReader(is));
					
					System.out.println("Successful! Parsing data");
							
					je = parser.parse(br);

					
					for(int i = 0; i < je.getAsJsonArray().get(1).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().size()-1; i++)
					{
						System.out.println("Processing comment #"+(i+1));
						String comment = je.getAsJsonArray().get(1).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(i).getAsJsonObject().get("data").getAsJsonObject().get("body").getAsString();
						for(int j = 0; j < replacements.length; j++)
						{
							if(comment.contains(replacements[j][0]))
							{
								String reply = "(http://"+replacements[j][1]+"/"+comment.replace(replacements[j][0].replace("(", "").replace(")",""), "")+")";
								System.out.println("Found match of "+replacements[j][0]);
								String commentID  = je.getAsJsonArray().get(1).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(i).getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString().replace("\"", "");
								System.out.println(commentID);
								System.out.println("Posting comment...");
								System.out.println("Reply: "+reply.replace(" ", "%20"));
								
								if(!comment(commentID, reply))System.err.println("Unable to post comment");
				
							}
							
						}
					}

				}
			}
			
			
			
			
		
			
			
		} catch (MalformedURLException e) {} catch (IOException e) {
			System.out.println("Could not connect to reddit.com");
			e.printStackTrace();

		}
	}
	
	public boolean comment(String thing_id, String text)
	{
		try {
			URL comment = new URL("http://www.reddit.com/api/comment?thing_id="+thing_id+"&text="+text+"&uh="+modhash);
			System.out.println("Opening connection...");
			HttpURLConnection connection = (HttpURLConnection) comment.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Host","");
			connection.setRequestProperty("Set-Cookie", cookieJar.get(new URI("http://reddit.com")).get(0).getValue());
			connection.setRequestProperty("Length", "0");
			connection.setRequestProperty("X-Target-URI","http://www.reddit.com");
			System.out.println("Connected, reading reply...");
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			System.out.println(br.readLine());
			is.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public JsonElement getListing(int listingNum)
	{
		return rData.get("data").getAsJsonObject().get("children").getAsJsonArray().get(listingNum);
	}

}

package com.unacceptableuse.demobilizer;

import java.io.BufferedReader;
import java.io.File;
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
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import sun.net.www.protocol.http.HttpURLConnection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Demobilizer{

	
	
	private JsonObject rData;
	private String modhash;
	private CookieManager cookieManager ;
	private CookieStore cookieJar;
	private String sqlpass = "-";
	private ArrayList<String> postedThings = new ArrayList<String>();
	private int posts = 0, errors = 0, scanned = 0, dupes = 0;
	
	private String[][] replacements = {
			{"en.m.wikipedia.org", "en.wikipedia.org"},
			{"m.wikipedia.org", "wikipedia.org"},
			{"m.reddit.com", "reddit.com"},
			{"m.youtube.com", "youtube.com"},
			{"mobile.slate.com", "slate.com"},
			{"mobile.theverge.com", "theverge.com"},
			{"m.theatlantic.com", "theatlantic.com"},
			{"m.ign.com", "ign.com"},
			{"m.guardian.com", "guardian.com"},
			{"m.facebook.com", "facebook.com"},
			{"m.wolframalpha.com", "wolframalpha.com"},
			{"m.flickr.com", "flickr.com"},
			{"m.twitter.com", "twitter.com"},
			{"mobile.myspace.com", "myspace.com"},
			{"download.com/Palm-OS/", "download.com"}
			};
	
	public Demobilizer()
	{
		
		
		if(new File(FileSaver.getCleanPath()+"//postcheck.dat").exists())
		{
			postedThings = (ArrayList<String>) FileSaver.load(FileSaver.getCleanPath()+"//postcheck.dat");
			System.out.println(postedThings.size()+" links corrected.");
		}else
		{
			try {
				new File(FileSaver.getCleanPath()+"//postcheck.dat").createNewFile();
				System.out.println("Created file");

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);
		try {
		modhash = login("LinkDemobilizerBot", "-");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> subreddits;
		try {
			subreddits = getSubscribedReddits();
			for(String subreddit : subreddits)
			{
			scanSubreddit(subreddit, 25);
			}
			
				
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	
		
		System.out.println("Saving...");
		FileSaver.save(postedThings, FileSaver.getCleanPath()+"//postcheck.dat");
		System.out.println("Done! "+posts+" posts out of "+scanned+" demobilized. "+errors+" errors occurred and "+dupes+" dupes were stopped.");
		
		
	}
	
	public ArrayList<String> getSubscribedReddits() throws MalformedURLException
	{
		URL comment = new URL("http://www.reddit.com/reddits/mine.json");
		System.out.println("Opening connection...");
		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) comment.openConnection();
		    connection.setDoOutput(true);
		    connection.setUseCaches (false);
		    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		    connection.setRequestProperty("User-Agent", "Link demobilizer bot by /u/UnacceptableUse");
	 
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			JsonParser p = new JsonParser();
			JsonElement e1 = p.parse(br); 
			JsonObject o1 = e1.getAsJsonObject();
			JsonObject o2 = o1.get("data").getAsJsonObject();

			JsonArray reddits = o2.get("children").getAsJsonArray();

			
			
			
			
			 ArrayList<String> sr = new ArrayList<String>();
			for(int i = 0; i < reddits.size(); i++)
			{
				sr.add(reddits.get(i).getAsJsonObject().get("data").getAsJsonObject().get("url").getAsString().replace("/r/", "").replace("/", ""));
			
			}
			System.out.println(sr.size()+" subreddits to scan!");

			is.close();
			br.close();
			return sr;
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		
		
		return null;
	}
	
	
	public static void main(String[] args)
	{
/*		System.out.println("Attempting to login ");
		t*/
		System.setProperty("http.agent", "");
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
	    ycConnection.setRequestProperty("User-Agent", "Link demobilizer bot by /u/UnacceptableUse");

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
			connection.setRequestProperty("User-Agent", "Link demobilizer bot by /u/UnacceptableUse");
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			System.out.println("Successful! Parsing data...");
			
			String line = br.readLine();
			JsonElement je = parser.parse(line);
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
					connection.setRequestProperty("User-Agent", "Link demobilizer bot by /u/UnacceptableUse");
					is = connection.getInputStream();
					br = new BufferedReader(new InputStreamReader(is));
					
					System.out.println("Successful! Parsing data");
							
					je = parser.parse(br);

					
					for(int i = 0; i < je.getAsJsonArray().get(1).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().size()-1; i++)
					{
						scanned++;
						System.out.println("Processing comment #"+(i+1));
						String comment = je.getAsJsonArray().get(1).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(i).getAsJsonObject().get("data").getAsJsonObject().get("body").getAsString();
						String commentID  = je.getAsJsonArray().get(1).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(i).getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString().replace("\"", "");
						for(int j = 0; j < replacements.length; j++)
						{
							if(comment.contains(replacements[j][0]))
							{
								String reply = "**For non mobile users:**  \n "+comment.replace(replacements[j][0], replacements[j][1])+"\n  \n*[Did I get it wrong?](http://reddit.com/r/LinkDemobilizerBot)*";
								System.out.println("Found match of "+replacements[j][0]);
								System.out.println("Posting comment...");
								System.out.println("Reply: "+reply);
								
								
								boolean canPost = true;
								for(String s : postedThings)
								{
									if(je.getAsJsonArray().get(1).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(i).getAsJsonObject().get("data").getAsJsonObject().toString().contains(s))
									{
										canPost = false;
									}
								}
								if(canPost)
								{
				
									comment(commentID, reply);
									postedThings.add(commentID);
									FileSaver.save(postedThings, FileSaver.getCleanPath()+"//postcheck.dat");
									System.out.println("Saved file");
									System.out.println("Comment posted succesfully");
								}
								else
								{
									System.out.println("Comment was already posted");
									dupes++;
								}
							}
							
						}
					
						
					}
					
					System.out.println("Processing title...");
					scanned++;
					for(int j = 0; j < replacements.length; j++)
					{
						String postLink = je.getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("url").getAsString();
						if(postLink.contains(replacements[j][0]))
						{
							String reply = "**For non mobile users:**  \n"+postLink.replace(replacements[j][0], replacements[j][1])+"\n  \n*[Did I get it wrong?](http://reddit.com/r/LinkDemobilizerBot)*";
							System.out.println("Found match of (intitle)"+replacements[j][0]);
							String commentID  = je.getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString().replace("\"", "");
							System.out.println(commentID);
							System.out.println("Posting comment...");
							System.out.println("Reply: "+reply);
						
							boolean canPost = true;
							for(String s : postedThings)
							{
								if(je.getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().toString().contains(s));
								{
									canPost = false;
								}	
							}
							if(canPost)
							{
								comment(commentID, reply);
								postedThings.add(commentID);
								FileSaver.save(postedThings, FileSaver.getCleanPath()+"//postcheck.dat");
								System.out.println("Comment posted succesfully");
								
							}
							else
							{
								System.out.println("Comment was already posted");
								dupes++;
							}
							
						}
					
					}
					

				}
			}		
		} catch (MalformedURLException e) {} catch (IOException e) {
			errors++;
			System.out.println("Could not connect to reddit.com");
			e.printStackTrace();
		}
		catch(Exception e)
		{
			errors++;
			e.printStackTrace();
			System.err.println("An error occurred: "+e.getMessage());
			System.err.println("Nevermind... continuing");
		}
	}
	
	public static int countOccurrences(String haystack, char needle)
	{
	    int count = 0;
	    for (int i=0; i < haystack.length(); i++)
	    {
	        if (haystack.charAt(i) == needle)
	        {
	             count++;
	        }
	    }
	    return count;
	}
	
	public boolean comment(String thing_id, String text)
	{
		try {
			URL comment = new URL("http://www.reddit.com/api/comment?thing_id="+thing_id+"&text="+URLEncoder.encode(text, "UTF-8")+"&uh="+modhash);
			System.out.println("Opening connection...");
			HttpURLConnection connection = (HttpURLConnection) comment.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("User-Agent", "Link demobilizer bot by /u/UnacceptableUse");
			connection.setRequestProperty("Host","");
			connection.setRequestProperty("Length", "0");
			connection.setRequestProperty("X-Target-URI","http://www.reddit.com");
			System.out.println("Connected, posting to output stream...");

		    PrintWriter out = new PrintWriter(connection.getOutputStream()); 
		    out.close();
			
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			posts++;
			is.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			errors++;
			return false;
		}
		return true;
	}
	
	public JsonElement getListing(int listingNum)
	{
		return rData.get("data").getAsJsonObject().get("children").getAsJsonArray().get(listingNum);
	}

}

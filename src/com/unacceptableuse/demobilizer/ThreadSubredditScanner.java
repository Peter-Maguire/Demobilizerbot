/*package com.unacceptableuse.demobilizer;

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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sun.net.www.protocol.http.HttpURLConnection;

public class ThreadSubredditScanner implements Runnable{

	private String[][] replacements = {
			{"en.m.wikipedia.org", "en.wikipedia.org"},
			{"m.wikipedia.org", "wikipedia.org"},
			{"m.reddit.com", "reddit.com"},
			{"m.facebook.com", "facebook.com"},
			{"m.wolframalpha.com", "wolframalpha.com"},
			{"m.flickr.com", "flickr.com"},
			{"m.twitter.com", "twitter.com"},
			{"mobile.myspace.com", "myspace.com"},
			{"download.com/Palm-OS/", "download.com"}
			};
	
	private JsonObject rData;
	private String modhash;
	private CookieManager cookieManager ;
	private CookieStore cookieJar;
	
	private ArrayList<String> postedThings = new ArrayList<String>();
	private int posts = 0, errors = 0;
	
	@Override
	public void run() {
		cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(cookieManager);
		try {
		modhash = login("LinkDemobilizerBot", "01189998819991197253");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		log("Starting...");
		scanSubreddit("todayilearned", 25);
		scanSubreddit("minecraft", 25);
		scanSubreddit("HIMYM", 25);
		scanSubreddit("abandonedporn", 25);
		scanSubreddit("android", 25);
		scanSubreddit("pics", 25);
		scanSubreddit("askReddit", 25);
		scanSubreddit("wikipedia", 25);
		log("Done! "+posts+" posts demobilized. "+errors+" errors occurred");
	
		
	}
	
	public String login(String user, String pw) throws IOException
	{
		URL url = null;
		try {
			 url = new URL("http://www.reddit.com/api/login/"+user+"?api_type=json&user="+user+"&passwd="+pw);
		} catch (MalformedURLException e) {} catch (IOException e) {
			log("Unable to login:");
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
	    log(response);  
	    JsonParser parser = new JsonParser();
	  
	    cookieJar = cookieManager.getCookieStore();
	    List<HttpCookie> cookies = cookieJar.getCookies();
	    for(HttpCookie cookie : cookies)
	    {
	    	log("Received cookie "+cookie.getName()+" - "+cookie.getValue());
	    }
	    
	
	    
	    
	  String modhash = parser.parse(response).getAsJsonObject().get("json").getAsJsonObject().get("data").getAsJsonObject().get("modhash").getAsString().replace("\"", "");
	  return modhash;
	}
	
	public boolean comment(String thing_id, String text)
	{
		try {
			URL comment = new URL("http://www.reddit.com/api/comment?thing_id="+thing_id+"&text="+text+"&uh="+modhash);
			log("Opening connection...");
			HttpURLConnection connection = (HttpURLConnection) comment.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("User-Agent", "Link demobilizer bot by /u/UnacceptableUse");
			connection.setRequestProperty("Host","");
			connection.setRequestProperty("Set-Cookie", cookieJar.get(new URI("http://reddit.com")).get(0).getValue());
			connection.setRequestProperty("Length", "0");
			connection.setRequestProperty("X-Target-URI","http://www.reddit.com");
			log("Connected, posting to output stream...");

		    PrintWriter out = new PrintWriter(connection.getOutputStream()); 
		    out.close();
			
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			log(br.readLine());
			is.close();
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void scanSubreddit(String subreddit, int posts)
	{
		try {
			log("Attempting to connect to reddit "+subreddit);
			JsonParser parser = new JsonParser();
			URL redditlink = new URL("http://reddit.com/r/"+subreddit+".json");
			HttpURLConnection connection = (HttpURLConnection) redditlink.openConnection();
			connection.setRequestProperty("User-Agent", "Link demobilizer bot by /u/UnacceptableUse");
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			log("Successful! Parsing data...");
			
			JsonElement je = parser.parse(br);
			rData = je.getAsJsonObject();
			progbar.setMaximum(posts);
			for(int x = 0; x < posts; x++)
			{
				progbar.setValue(x);
				revalidate();
				repaint();
				log("Parsed data, grabbing listing "+x);
				JsonObject postData = getListing(x).getAsJsonObject();
				JsonObject post = postData.get("data").getAsJsonObject();
				
				int commentAmount = Integer.parseInt(post.get("num_comments").toString());
				log("Listing has "+commentAmount+" comments.");

				if(commentAmount != 0)
				{
					is.close();
					br.close();
					log("Connecting to comments section "+post.get("permalink"));

					URL commentsLink = new URL("http://reddit.com/"+post.get("permalink").getAsString().replace("\"", "")+".json");
					connection = (HttpURLConnection) commentsLink.openConnection();
					connection.setRequestProperty("User-Agent", "Link demobilizer bot by /u/UnacceptableUse");
					is = connection.getInputStream();
					br = new BufferedReader(new InputStreamReader(is));
					
					log("Successful! Parsing data");
							
					je = parser.parse(br);

					
					for(int i = 0; i < je.getAsJsonArray().get(1).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().size()-1; i++)
					{
						log("Processing comment #"+(i+1));
						String comment = je.getAsJsonArray().get(1).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(i).getAsJsonObject().get("data").getAsJsonObject().get("body").getAsString();
						for(int j = 0; j < replacements.length; j++)
						{
							if(comment.contains(replacements[j][0]))
							{
								String reply = "**For non mobile users[:](http://reddit.com/r/LinkDemobilizerBot)**  \n "+comment.replace(replacements[j][0], replacements[j][1]);
								log("Found match of "+replacements[j][0]);
								String commentID  = je.getAsJsonArray().get(1).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(i).getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString().replace("\"", "");
								log("Posting comment...");
								log("Reply: "+reply);
								
								
								boolean canPost = true;
								for(String s : postedThings)
								{
									if(s == commentID)
										canPost = false;
								}
								if(canPost)
								{
									posts++;
									comment(commentID, reply);
									postedThings.add(commentID);
									log("Comment posted succesfully");
								}
								else
								{
									log("Comment was already posted");
								}
							}
							
						}
					}
					
					log("Processing title...");
					for(int j = 0; j < replacements.length; j++)
					{
						repaint();
						revalidate();
						String postLink = je.getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("url").getAsString();
						if(postLink.contains(replacements[j][0]))
						{
							String reply = "**For non mobile users[:](http://reddit.com/r/LinkDemobilizerBot)**  \n"+postLink.replace(replacements[j][0], replacements[j][1]);
							log("Found match of (intitle)"+replacements[j][0]);
							String commentID  = je.getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("children").getAsJsonArray().get(0).getAsJsonObject().get("data").getAsJsonObject().get("name").getAsString().replace("\"", "");
							log("Posting comment...");
							log("Reply: "+reply);
						
							boolean canPost = true;
							for(String s : postedThings)
							{
								if(s == commentID)
									canPost = false;
							}
							if(canPost)
							{
								posts++;
								comment(commentID, reply);
								postedThings.add(commentID);
								log("Comment posted succesfully");
							}
							else
							{
								log("Comment was already posted");
							}
						}
					}	
				}
			}		
		} catch (MalformedURLException e) {} catch (IOException e) {
			log("Could not connect to reddit.com");
			e.printStackTrace();
			errors++;
		}
		catch(Exception e)
		{
			errors++;
			e.printStackTrace();
			log("An error occurred: "+e.getMessage());
			log("Nevermind... continuing");
		}
	}
	
	public void log(String s)
	{
		Demobilizer.log(s);
	}

}
*/
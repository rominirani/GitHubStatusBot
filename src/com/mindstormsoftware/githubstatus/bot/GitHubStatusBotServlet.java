package com.mindstormsoftware.githubstatus.bot;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.search.Util;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;

@SuppressWarnings("serial")
public class GitHubStatusBotServlet extends HttpServlet {
	public static final Logger _log = Logger.getLogger(GitHubStatusBotServlet.class.getName());

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String strCallResult="";
		resp.setContentType("text/plain");
		XMPPService xmpp = null;
		JID fromJid = null;
		try {
	
			//STEP 1 - Extract out the message and the Jabber Id of the user sending us the message via the Google Talk client
			xmpp = XMPPServiceFactory.getXMPPService();
			Message msg = xmpp.parseMessage(req);

			fromJid = msg.getFromJid();
			//String emailAddress = fromJid.getId();
			String emailAddress = fromJid.getId().substring(0,fromJid.getId().indexOf("/"));
			String msgBody = msg.getBody();
			
			String strCommand = msgBody;
			
			//Do validations here. Only basic ones i.e. cannot be null/empty
			if (strCommand == null) throw new Exception("You must give a command.");
			
			//Trim the stuff
			strCommand = strCommand.trim();
			if (strCommand.length() == 0) throw new Exception("You must give a command.");
			
			String[] words = strCommand.split(" ");
			if (words.length == 1) {
				//This command will print the list of commands that the Bot understands
				if (words[0].equalsIgnoreCase("help")) {
					//Print out help
					StringBuffer SB = new StringBuffer();
					SB.append("***** Welcome to GitHub Status Helper Bot *****");
					SB.append("\r\nI understand the following commands:");
					SB.append("\r\n1. Type help to get the list of commands.");
					SB.append("\r\n2. Type status to get the current system status (one of good, minor, or major) and timestamp.");
					SB.append("\r\n3. Type last-message to get the last human communication, status, and timestamp.");
					SB.append("\r\n4. Type messages to get the most recent human communications with status and timestamp.");
					SB.append("\r\n5. Type subscribe to get notified via Google Talk if GitHub is down. Hourly updates only.");
					SB.append("\r\n6. Type unsubscribe to remove yourself from any 'GitHub is down' status updates.");
					SB.append("\r\n7. Type about to get more information about this Agent.");
					strCallResult = SB.toString();
				}
				//This command will print out a brief message about the Bot
				else if (words[0].equalsIgnoreCase("about")) {
					strCallResult = "Hello! I am the GitHub Status Helper Bot version 1.0"+"\r\n"+"Developer: Romin Irani"+"\r\n" + "Blog: http://www.rominirani.com" + "\r\n";
				}
				//This command provides the current Github status
				else if (words[0].equalsIgnoreCase("status")) {
					strCallResult = MemcacheServiceFactory.getMemcacheService().get("status").toString();
				}
				//This command provides the last human communication with status and timestamp
				else if (words[0].equalsIgnoreCase("last-message")) {
					strCallResult = MemcacheServiceFactory.getMemcacheService().get("last-message").toString();
				}
				//This command provides the most recent human communications with status and timestamp
				else if (words[0].equalsIgnoreCase("messages")) {
					strCallResult = MemcacheServiceFactory.getMemcacheService().get("messages").toString();
				}
				//This command will subscribe a particular GTalk user to minor and major status of GitHub
				else if (words[0].equalsIgnoreCase("subscribe")) {
					if (GitHubStatusSubscriberService.getInstance().addSubscriber(emailAddress).equals("success")) {
						strCallResult = "Thank You! You have been subscribed successfully to the GitHub Status Bot. You will be notified if there are any GitHub status that are of minor or major type.";
					}
					else {
						strCallResult = "Sorry! There was an internal error and you could not be subscribed. Please try again in a while or contact the Author of the Bot.";
					}
				}
				//This command will unsubscribe a particular GTalk user to minor and major status of GitHub
				else if (words[0].equalsIgnoreCase("unsubscribe")) {
					if (GitHubStatusSubscriberService.getInstance().updateSubscriberStatus(emailAddress,"INACTIVE").equals("success")) {
						strCallResult = "Thank You! You have been unsubscribed successfully to the GitHub Status Bot. You will be no longer receive any notifications.";
					}
					else {
						strCallResult = "Sorry! There was an internal error and you could not be unsubscribed. Please try again in a while or contact the Author of the Bot.";
					}
				}
			}
			else {
				strCallResult = "Sorry! Could not understand your command.";
			}
			
			//Send out the Response message on the same XMPP channel. This will be delivered to the user via the Google Talk client.
	        Message replyMessage = new MessageBuilder().withRecipientJids(fromJid).withBody(strCallResult).build();
                
	        boolean messageSent = false;
	        //if (xmpp.getPresence(fromJid).isAvailable()) {
	        SendResponse status = xmpp.sendMessage(replyMessage);
	        messageSent = (status.getStatusMap().get(fromJid) == SendResponse.Status.SUCCESS);
	        //}
		}
		catch (Exception ex) {
			
			//If there is an exception then we send back a generic message to the client i.e. GitHub Status Bot could not understand your command. Please
			//try again. We log the exception internally.
			_log.info("Something went wrong. Please try again!" + ex.getMessage());
	        Message replyMessage = new MessageBuilder()
            .withRecipientJids(fromJid)
            .withBody("GitHub Status Bot could not understand your command. Please try again.")
            .build();
                
	        boolean messageSent = false;
	        //The condition is commented out so that it can work over non Google Talk XMPP providers also.
	        //if (xmpp.getPresence(fromJid).isAvailable()) {
	        SendResponse status = xmpp.sendMessage(replyMessage);
	        messageSent = (status.getStatusMap().get(fromJid) == SendResponse.Status.SUCCESS);
	        //}
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doGet(req, resp);
	}
	
	
}

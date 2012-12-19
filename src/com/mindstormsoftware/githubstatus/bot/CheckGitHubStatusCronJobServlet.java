package com.mindstormsoftware.githubstatus.bot;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

@SuppressWarnings("serial")
public class CheckGitHubStatusCronJobServlet extends HttpServlet {
 private static final Logger _logger = Logger.getLogger(CheckGitHubStatusCronJobServlet.class.getName());
 public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	 try {
		 try {
			    String strCallResult = "";
			    MemcacheService _CacheService = MemcacheServiceFactory.getMemcacheService();
			    //GitHub Status API : status.json
				String strAPICallResult = Utils.makeHTTPGETCall(Constants.STATUS_URL);
				JSONObject responseJSON = new JSONObject(strAPICallResult);
				String status = responseJSON.getString("status");
				String last_updated = responseJSON.getString("last_updated");
				strCallResult = "Current Status : " + status + " Last Updated on " + last_updated;
				_CacheService.put("status", strCallResult);
				
				//GitHub Status API : last-message.json
				strAPICallResult = Utils.makeHTTPGETCall(Constants.LAST_MESSAGE_URL);
				responseJSON = new JSONObject(strAPICallResult);
				status = responseJSON.getString("status");
				String body   = responseJSON.getString("body");
				String created_on = responseJSON.getString("created_on");
				strCallResult = "Last Status : " + status + "\r\n" + "Last Message : " + body + "\r\n" + "Last Update Time : " + created_on;
				_CacheService.put("last-message",strCallResult);
				
				//GitHub Status API : messages.json
				StringBuffer SBResult = new StringBuffer();
				SBResult.append("Last few status messages from GitHub: " + "\r\n");
				SBResult.append("--------------------------------------" + "\r\n");
				strAPICallResult = Utils.makeHTTPGETCall(Constants.MESSAGES_URL);
				JSONArray responseJSONArray = new JSONArray(strAPICallResult);
				if (responseJSONArray.length() > 0) {
					for (int i = 0; i < responseJSONArray.length(); i++) {
						JSONObject statusObject = (JSONObject) responseJSONArray.get(i);
						status = statusObject.getString("status");
						body   = statusObject.getString("body");
						created_on = statusObject.getString("created_on");
						SBResult.append("Last Status : " + status + "\r\n" + "Last Message : " + body + "\r\n" + "Last Update Time : " + created_on);
					}
				}
				else {
					SBResult.append("No messages at the moment");
				}
				_CacheService.put("messages",SBResult.toString());
			}
			catch (Exception ex) {
				_logger.warning("Exception in getting making Github call: " + ex.getMessage());
			}
		 _logger.info("GitHub Status Cron Job has been executed");
	 }
	 catch (Exception ex) {
		 //Log any exceptions in your Cron Job
		 _logger.info("Error in executing GitHub Status Cron Job : " + ex.getMessage());
	 }
 }

 @Override
 public void doPost(HttpServletRequest req, HttpServletResponse resp)
 throws ServletException, IOException {
 doGet(req, resp);
 }
}

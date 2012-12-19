package com.mindstormsoftware.githubstatus.bot;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class SendGitHubStatusCronJobServlet extends HttpServlet {
 private static final Logger _logger = Logger.getLogger(SendGitHubStatusCronJobServlet.class.getName());
 public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	 try {
		 GitHubStatusSubscriberService.getInstance().sendNotifications();
	 }
	 catch (Exception ex) {
		 //Log any exceptions in your Cron Job
		 _logger.info("Error in executing Send GitHub Status Cron Job : " + ex.getMessage());
	 }
 }

 @Override
 public void doPost(HttpServletRequest req, HttpServletResponse resp)
 throws ServletException, IOException {
 doGet(req, resp);
 }
}

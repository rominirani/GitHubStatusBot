package com.mindstormsoftware.githubstatus.bot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.xmpp.JID;
import com.google.appengine.api.xmpp.Message;
import com.google.appengine.api.xmpp.MessageBuilder;
import com.google.appengine.api.xmpp.SendResponse;
import com.google.appengine.api.xmpp.XMPPService;
import com.google.appengine.api.xmpp.XMPPServiceFactory;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;

public class GitHubStatusSubscriberService {
	public static final Logger _logger = Logger.getLogger(GitHubStatusSubscriberService.class.toString());

	private static GitHubStatusSubscriberService _self = null;

	private GitHubStatusSubscriberService() {
	}

	public static GitHubStatusSubscriberService getInstance() {
		if (_self == null) {
			_self = new GitHubStatusSubscriberService();
			ObjectifyService.register(GitHubStatusSubscriber.class);
		}
		return _self;
	}
	
	public String addSubscriber(String emailAddress) throws Exception {
		Objectify obj = ObjectifyService.begin();
		GitHubStatusSubscriber _record = new GitHubStatusSubscriber(emailAddress,"ACTIVE");
		obj.put(_record);
		return "success";
	}
	
	public String updateSubscriberStatus(String emailAddress,String status) throws Exception {
		Objectify obj = ObjectifyService.begin();
		GitHubStatusSubscriber _record = findSubscriberByID(emailAddress);
		_record.setStatus(status);
		obj.put(_record);
		return "success";
	}
	
	public GitHubStatusSubscriber findSubscriberByID(String emailAddress) {
		try {
			Objectify obj = ObjectifyService.begin();
			GitHubStatusSubscriber r = obj.query(GitHubStatusSubscriber.class).filter("emailAddress",emailAddress).get();
			if (r != null)
				return r;
			return null;
		} catch (Exception ex) {
			return null;
		}
	}
	
	public List<GitHubStatusSubscriber> getAllSubscribers(String status) throws Exception {
		List<GitHubStatusSubscriber> _results = new ArrayList<GitHubStatusSubscriber>();
		Objectify obj = ObjectifyService.begin();
		_results = obj.query(GitHubStatusSubscriber.class).filter("status",status).list();
		return _results;
	}

	public void sendNotifications() {
		//For every Subscriber that is ACTIVE
		try {
			//Check MemCache status
			String strStatus = MemcacheServiceFactory.getMemcacheService().get("status").toString();
			if (strStatus != null) {
				if ((strStatus.indexOf("major") != -1) || (strStatus.indexOf("minor") != -1)) {
					List<GitHubStatusSubscriber> _notifications = getAllSubscribers("ACTIVE");
					_logger.info("Need to send GitHub Status : " + strStatus + " to " + _notifications.size() + " subscribers.");
					Iterator<GitHubStatusSubscriber> it = _notifications.iterator();
					while (it.hasNext()) {
						GitHubStatusSubscriber R = (GitHubStatusSubscriber)it.next();
						try {
							//Send Message
							sendIM(R.getEmailAddress(), strStatus);
						}
						catch(Exception ex) {
							_logger.warning("Error in sending IM to " + R.getEmailAddress() + ". Reason = " + ex.getMessage());
						}
					}
				}
			}
		}
		catch (Exception ex) {
			_logger.info("Error in sendNotifications() Job : " + ex.getMessage());
		}
	}
	
	/**
	 * 
	 * @param JabberId The JabberId of the user to send out the XMPP message to
	 * @param msg The message i.e. the text that we need to send out in the XMPP message.
	 * @throws Exception
	 */
	private void sendIM(String JabberId, String msg) throws Exception {
		XMPPService xmpp = null;
		JID fromJid = new JID(JabberId);
		xmpp = XMPPServiceFactory.getXMPPService();
		Message replyMessage = new MessageBuilder()
        .withRecipientJids(fromJid)
        .withBody(msg)
        .build();
        boolean messageSent = false;
        //The condition is commented out so that it can work over non Google Talk XMPP providers also.
        //if (xmpp.getPresence(fromJid).isAvailable()) {  
        SendResponse status = xmpp.sendMessage(replyMessage);
        messageSent = (status.getStatusMap().get(fromJid) == SendResponse.Status.SUCCESS);
        //}
        if (messageSent) {
        	_logger.info("Message has been sent successfully");
        }
        else {
        	_logger.info("Message could not be sent");
        }
	}
}

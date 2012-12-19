package com.mindstormsoftware.githubstatus.bot;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Entity;

@Entity
public class GitHubStatusSubscriber {
	@Id
	String emailAddress;
	String status;

	private GitHubStatusSubscriber() {}

	public GitHubStatusSubscriber(String emailAddress, String status) {
		this.emailAddress = emailAddress;
		this.status = status;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
}

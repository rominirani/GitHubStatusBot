package com.mindstormsoftware.githubstatus.bot;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.esxx.js.protocol.GAEConnectionManager;

public class Utils {

	/**
	 * Utility HTTP POST Method that makes a POST call for the URL provided. It abstracts out the HTTPClient functioning
	 * @param url
	 * @return String response of the HTTP call
	 * @throws Exception
	 */
	public static String makeHTTPPOSTCall(String url) throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient(
				new GAEConnectionManager());
		HttpPost postRequest = new HttpPost(url);

		HttpResponse response = httpClient.execute(postRequest);

		if ((response.getStatusLine().getStatusCode() < 200)
				&& (response.getStatusLine().getStatusCode() >= 300)) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(response.getEntity().getContent())));

		StringBuilder SB = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
			SB.append(output);
		}

		httpClient.getConnectionManager().shutdown();
		return SB.toString();
	}

	/**
	 * Utility HTTP GET Method that makes a GET call for the URL provided. It abstracts out the HTTPClient functioning
	 * @param url
	 * @return String response of the HTTP call
	 * @throws Exception
	 */
	public static String makeHTTPGETCall(String url) throws Exception {
		DefaultHttpClient httpClient = new DefaultHttpClient(
				new GAEConnectionManager());
		HttpGet getRequest = new HttpGet(url);

		HttpResponse response = httpClient.execute(getRequest);

		if ((response.getStatusLine().getStatusCode() < 200)
				&& (response.getStatusLine().getStatusCode() >= 300)) {
			throw new RuntimeException("Failed : HTTP error code : " +  response.getStatusLine().getStatusCode());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(response.getEntity().getContent())));

		StringBuilder SB = new StringBuilder();
		String output;
		while ((output = br.readLine()) != null) {
			SB.append(output);
		}

		httpClient.getConnectionManager().shutdown();
		return SB.toString();
	}
}

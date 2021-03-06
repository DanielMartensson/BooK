package se.danielmartensson.book.tools.http;

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import cz.msebera.android.httpclient.ParseException;
import cz.msebera.android.httpclient.auth.AuthScope;
import cz.msebera.android.httpclient.auth.UsernamePasswordCredentials;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.CloseableHttpResponse;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.BasicCredentialsProvider;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.util.EntityUtils;

public class HTTPClient {
	

	public static String ADDRESS = "localhost"; // Default

	public static int PORT = 8080; // Default
	
	public static String USERNAME; // This is actully email in practice because you login with your email
	

	private CloseableHttpClient httpclient;
	
	/**
	 * Login
	 * @param address
	 * @param port
	 * @param username
	 * @param password
	 * @return 
	 */
	public HTTPMessage login(String username, String password) {		
		// Set the timeout
		int timeout = 30; // seconds
		RequestConfig config = RequestConfig.custom()
		  .setConnectTimeout(timeout * 1000)
		  .setConnectionRequestTimeout(timeout * 1000)
		  .setSocketTimeout(timeout * 1000).build();
		
	    BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(ADDRESS, PORT), new UsernamePasswordCredentials(username, password));
		httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setDefaultRequestConfig(config).build();
		HTTPClient.USERNAME = username; // Save
		return sendPost("http://" + ADDRESS + ":" + PORT + "/user/login");
	}

	/**
	 * Send a url command and get HTTPMessage object
	 * @param url String of our url
	 * @return HTTPMessage
	 */
	public HTTPMessage sendPost(String url) {
		try {
			// Get the response
			HttpPost httppost = new HttpPost(url);
			CloseableHttpResponse response = httpclient.execute(httppost);

			// Get the HTTPMessage object
			String json = EntityUtils.toString(response.getEntity());
			Type type = new TypeToken<HTTPMessage>() {}.getType();
			HTTPMessage hTTPMessage = new Gson().fromJson(json, type);
			hTTPMessage.setConnectionStatus(response.getStatusLine().getStatusCode());

			// Return the object
			return hTTPMessage;
		} catch (IOException | RuntimeException e) {

			return null;
		}

	}

	/**
	 * Send a url command and get HTTPMessage object
	 * @param url String of our url
	 * @return HTTPMessage
	 */
	public HTTPMessage sendGet(String url) {
		try {
			// Get the response
			HttpGet httpget = new HttpGet(url);
			CloseableHttpResponse response = httpclient.execute(httpget);

			// Get the HTTPMessage object
			String json = EntityUtils.toString(response.getEntity());
			Type type = new TypeToken<HTTPMessage>() {}.getType();
			HTTPMessage hTTPMessage = new Gson().fromJson(json, type);
			hTTPMessage.setConnectionStatus(response.getStatusLine().getStatusCode());

			// Return the object
			return hTTPMessage;
		} catch (IOException | RuntimeException e) {

			return null;
		}

	}

	/**
	 * Send a object and get the HTTPMessage object
	 * @param url String of our url
	 * @param obj Object we want to send
	 * @return HTTPMessage
	 */
	public <T> HTTPMessage postObject(String url, T obj) {
		try {
			// Get the response
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(new StringEntity(new Gson().toJson(obj)));
			httppost.setHeader("Content-type", "application/json");
			CloseableHttpResponse response = httpclient.execute(httppost);
			
			// Get the HTTPMessage object
			String json = EntityUtils.toString(response.getEntity());
			Type type = new TypeToken<HTTPMessage>() {}.getType();
			HTTPMessage hTTPMessage = new Gson().fromJson(json, type);
			hTTPMessage.setConnectionStatus(response.getStatusLine().getStatusCode());
			
			// Return the object
			return hTTPMessage;
		} catch (IOException | ParseException e) {
			return null;
		}
	}

}

package se.danielmartensson.tools.http;

import java.io.IOException;
import java.lang.reflect.Type;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;



public class HTTPClient {
	

	public static final String ADDRESS = "yourCompany.com"; // You need to set this address

	public static final int PORT = 80; // Leave this port to 80 = Internet. Not 8080 = For localhost
	
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
	    BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(ADDRESS, PORT), new UsernamePasswordCredentials(username, password.toCharArray()));
		httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
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
			hTTPMessage.setConnectionStatus(response.getCode());

			// Return the object
			return hTTPMessage;
		} catch (IOException | RuntimeException | ParseException e) {

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
			hTTPMessage.setConnectionStatus(response.getCode());

			// Return the object
			return hTTPMessage;
		} catch (IOException | RuntimeException | ParseException e) {

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
			hTTPMessage.setConnectionStatus(response.getCode());
			
			// Return the object
			return hTTPMessage;
		} catch (IOException | ParseException e) {
			return null;
		}
	}

}

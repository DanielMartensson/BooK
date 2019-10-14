package se.danielmartensson.tools.http;

import java.io.IOException;
import java.lang.reflect.Type;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;



public class HTTPClient {
	

	public static final String ADDRESS = "spektrakon.se";

	public static final int PORT = 80;
	
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
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(ADDRESS, PORT), new UsernamePasswordCredentials(username, password));
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
		} catch (IOException e) {
			return null;
		}
	}

}

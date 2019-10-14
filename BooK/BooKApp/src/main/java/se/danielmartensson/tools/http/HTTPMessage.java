package se.danielmartensson.tools.http;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HTTPMessage {
	// Message for the user
	String message; // Can also contain the json message of a json message
	int messageStatusCode;

	// Connection status
	int connectionStatus;

	public HTTPMessage(String message, int messageStatusCode) {
		this.message = message;
		this.messageStatusCode = messageStatusCode;
	}

	public <T> void setJsonMessage(T object) {
		Type type = new TypeToken<T>() {}.getType();
		message = new Gson().toJson(object, type);
	}

}

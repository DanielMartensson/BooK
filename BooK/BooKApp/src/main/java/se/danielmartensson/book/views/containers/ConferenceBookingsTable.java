package se.danielmartensson.book.views.containers;

import javafx.beans.property.SimpleStringProperty;
import lombok.Setter;

@Setter
public class ConferenceBookingsTable {
	
	private SimpleStringProperty from;
	private SimpleStringProperty to;
	private SimpleStringProperty email;
	
	public ConferenceBookingsTable(String from, String to, String email) {
		this.from = new SimpleStringProperty(from);
		this.to = new SimpleStringProperty(to);
		this.email = new SimpleStringProperty(email);
	}
	
	public String getFrom() {
		return from.get();
	}
	
	public String getTo() {
		return to.get();
	}
	
	public String getEmail() {
		return email.get();
	}
}

package se.danielmartensson.views.containers;

import javafx.beans.property.SimpleStringProperty;
import lombok.Setter;

@Setter
public class UserTable {
	
	private SimpleStringProperty username;
	private SimpleStringProperty company;
	private SimpleStringProperty email;
	
	public UserTable(String username, String company, String email) {
		this.username = new SimpleStringProperty(username);
		this.company = new SimpleStringProperty(company);
		this.email = new SimpleStringProperty(email);
	}
	
	public String getUsername() {
		return username.get();
	}
	
	public String getCompany() {
		return company.get();
	}
	
	public String getEmail() {
		return email.get();
	}
}

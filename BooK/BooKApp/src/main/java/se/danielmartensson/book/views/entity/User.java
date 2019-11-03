package se.danielmartensson.book.views.entity;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
	
	private long user_id;

	private String username;

	private String password;

	private String email;

	private Set<Role> roles;

}
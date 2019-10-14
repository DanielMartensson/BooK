package se.danielmartensson.BooKServer.entity;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Company {
	@Id
	private long id;
	private String username;
	private String company;
	private String email;

}

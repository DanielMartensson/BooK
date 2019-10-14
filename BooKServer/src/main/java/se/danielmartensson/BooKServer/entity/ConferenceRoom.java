package se.danielmartensson.BooKServer.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class ConferenceRoom {
	@Id
	@GeneratedValue
	private Long id;
	private long start;
	private long end;
	private String email; // The owner who created the meeting
	private String members = ""; // e.g myMember@host1.com;mySecondMember@host2.com
}

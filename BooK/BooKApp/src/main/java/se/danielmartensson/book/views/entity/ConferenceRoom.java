package se.danielmartensson.book.views.entity;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ConferenceRoom {
	private Long id;
	private long start;
	private long end;
	private String email; // The owner who created the meeting
	private String members = ""; // e.g myMember@host1.com;mySecondMember@host2.com
}

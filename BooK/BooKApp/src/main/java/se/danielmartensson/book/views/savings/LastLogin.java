package se.danielmartensson.book.views.savings;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LastLogin {
	private String email;
	private String serverAddress;
	private String port;
}
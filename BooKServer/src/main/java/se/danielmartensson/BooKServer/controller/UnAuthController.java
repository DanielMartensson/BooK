package se.danielmartensson.BooKServer.controller;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.danielmartensson.BooKServer.controller.messages.HTTPMessage;
import se.danielmartensson.BooKServer.entity.Company;
import se.danielmartensson.BooKServer.entity.Role;
import se.danielmartensson.BooKServer.entity.User;
import se.danielmartensson.BooKServer.repository.CompanyRepository;
import se.danielmartensson.BooKServer.repository.UserRepository;


@RestController
@RequestMapping("/unauth")
public class UnAuthController {
	
	private static int PASSWORD_LENGTH = 3;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private CompanyRepository companyRepository;
	
	@PostMapping("/createnewuser")
	public HTTPMessage createNewUser(@RequestBody User user) {
		// Search first for the user if it's null = not exist
		if (userRepository.findByUsername(user.getUsername()) == null) {
			
			// Check if user.getUsername() is valid - It's our email
			boolean valid = EmailValidator.getInstance().isValid(user.getUsername());
			if(valid == false)
				return new HTTPMessage("Email: " + user.getUsername() + " is not valid", HttpStatus.NOT_ACCEPTABLE.value());
			
			// Check if password is longer than PASSWORD_LENGTH letters
			if(user.getPassword().length() <= PASSWORD_LENGTH) 
				return new HTTPMessage("Password need to be longer than " + PASSWORD_LENGTH + " letters", HttpStatus.NOT_ACCEPTABLE.value());
			
			// For user
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user.setUser_id(userRepository.count()+1); // Important with + 1

			// For company
			Company company = new Company();
			company.setId(user.getUser_id());
			company.setUsername(user.getEmail().split("@")[0]); // Will be our username
			company.setCompany(user.getEmail().split("@")[1].split("\\.")[0]); // From "myUser@MyCompany.com" to "MyCompany"
			company.setEmail(user.getEmail());

			// For role
			for (Role role : user.getRoles()) {
				role.setRole_id(user.getUser_id()); // We need to set role ID too
			}
			
			// Save
			userRepository.save(user);
			companyRepository.save(company);
			return new HTTPMessage("User: " + user.getUsername() + " added", HttpStatus.OK.value());
		} else {
			return new HTTPMessage("User: " + user.getUsername() + " already exist", HttpStatus.FORBIDDEN.value());
		}

	}
	
}

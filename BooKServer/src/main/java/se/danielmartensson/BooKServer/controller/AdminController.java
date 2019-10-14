package se.danielmartensson.BooKServer.controller;

import java.util.List;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.danielmartensson.BooKServer.controller.messages.HTTPMessage;
import se.danielmartensson.BooKServer.entity.Company;
import se.danielmartensson.BooKServer.entity.ConferenceRoom;
import se.danielmartensson.BooKServer.entity.Role;
import se.danielmartensson.BooKServer.entity.User;
import se.danielmartensson.BooKServer.repository.CompanyRepository;
import se.danielmartensson.BooKServer.repository.ConferenceRoomRepository;
import se.danielmartensson.BooKServer.repository.RoleRepository;
import se.danielmartensson.BooKServer.repository.UserRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private CompanyRepository companyRepository;
	
	@Autowired
	private ConferenceRoomRepository conferenceRoomRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;


	/**
	 * Delete user by sending the user name of the user
	 * 
	 * @param username Name if the user
	 * @return JSON String object
	 */
	@PostMapping("/deleteuser")
	public HTTPMessage deleteUser(@RequestParam("username") String username) {
		// Check if user exist
		User user = userRepository.findByUsername(username);
		if (user != null) {
			// Get roles and delete them
			Set<Role> roles = user.getRoles();
			for (Role role : roles) {
				roleRepository.delete(role);
			}
			
			// Delete all the meetings from username
			List<ConferenceRoom> conferenceRooms = conferenceRoomRepository.findByEmail(user.getUsername());
			for(ConferenceRoom conferenceRoom : conferenceRooms ) {
				conferenceRoomRepository.delete(conferenceRoom);
			}
			
			// Delete the user and the online
			companyRepository.deleteById(user.getUser_id());
			userRepository.deleteById(user.getUser_id());
			return new HTTPMessage("User: " + username + " updated", HttpStatus.OK.value());
		} else {
			return new HTTPMessage("User: " + username + " does not exist", HttpStatus.NOT_FOUND.value());
		}

	}

	/**
	 * Update user by sending the user object and a boolean variable if password
	 * should be updated or not
	 * 
	 * @param user           Object user
	 * @param updatepassword Boolean if we want to update the user or not
	 * @return JSON String object
	 */
	@PostMapping("/updateuser")
	public HTTPMessage uppdateUser(@RequestBody User user, @RequestParam("updatepassword") boolean updatepassword) {
		// Check if user exist
		if (userRepository.findById(user.getUser_id()) != null) {
			
			// Check if our email/username is right
			boolean valid = EmailValidator.getInstance().isValid(user.getUsername());
			if(valid == false)
				return new HTTPMessage("Email: " + user.getUsername() + " is not valid", HttpStatus.NOT_ACCEPTABLE.value());
		
			// Update the password if we say that it should be updated
			if (updatepassword == true)
				user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			// Change all conference meetings authors
			User userConference = userRepository.findById(user.getUser_id()).get(); // This is the old user
			List<ConferenceRoom> conferenceRooms = conferenceRoomRepository.findByEmail(userConference.getUsername());
			for(ConferenceRoom conferenceRoom : conferenceRooms ) {
				conferenceRoom.setEmail(user.getEmail()); // Replace email
				conferenceRoomRepository.saveAndFlush(conferenceRoom);
			}
			
			// Save for user
			userRepository.saveAndFlush(user);
			
			// Save for role
			for(Role role : user.getRoles()) {
				roleRepository.saveAndFlush(role);
			}
			

			Company saveCompany = companyRepository.findById(user.getUser_id()).get();
			//saveCompany.setId(rows);
			saveCompany.setUsername(user.getEmail().split("@")[0]);
			saveCompany.setCompany(user.getEmail().split("@")[1].split("\\.")[0]); // From "myUser@MyCompany.com" to "MyCompany"
			saveCompany.setEmail(user.getEmail());
			companyRepository.saveAndFlush(saveCompany);
			return new HTTPMessage("User: " + user.getUsername() + " is updated", HttpStatus.OK.value());

		} else {
			return new HTTPMessage("User: " + user.getUsername() + " does not exist", HttpStatus.NOT_FOUND.value());
		}
	}

	/**
	 * Sends back the JSON string of user object.
	 * 
	 * @param username Name if the user
	 * @return JSON String object
	 */
	@GetMapping("/getuser")
	public HTTPMessage getUser(@RequestParam("username") String username) {
		// Send the Online entity in form of a JSON file
		User user = userRepository.findByUsername(username);
		HTTPMessage hTTPMessage = new HTTPMessage();
		System.out.println(username);
		if (user != null) {
			hTTPMessage.setJsonMessage(user);
			hTTPMessage.setMessageStatusCode(HttpStatus.OK.value());
		} else {
			hTTPMessage.setMessage("User: " + username + " not found");
			hTTPMessage.setMessageStatusCode(HttpStatus.NOT_FOUND.value());
		}
		return hTTPMessage;
	}
}

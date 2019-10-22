package se.danielmartensson.BooKServer.controller;



import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.mail.AuthenticationFailedException;
import javax.mail.internet.AddressException;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.danielmartensson.BooKServer.controller.messages.HTTPMessage;
import se.danielmartensson.BooKServer.entity.ConferenceRoom;
import se.danielmartensson.BooKServer.entity.User;
import se.danielmartensson.BooKServer.repository.CompanyRepository;
import se.danielmartensson.BooKServer.repository.ConferenceRoomRepository;
import se.danielmartensson.BooKServer.repository.UserRepository;

@RestController
@RequestMapping("/user")
public class UserController {
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ConferenceRoomRepository conferenceRoomRepository;
	
	@Autowired
    public JavaMailSender emailSender;

	@GetMapping("/getconferenceroombookings")
	public HTTPMessage getConferenceRoomBookings() {
		HTTPMessage hTTPMessage = new HTTPMessage();
		if (conferenceRoomRepository.count() > 0) {
			hTTPMessage.setMessageStatusCode(HttpStatus.OK.value());
			hTTPMessage.setJsonMessage(conferenceRoomRepository.findAll());
		} else {
			hTTPMessage.setMessageStatusCode(HttpStatus.NOT_FOUND.value()); // No data
			hTTPMessage.setJsonMessage(conferenceRoomRepository.findAll());
		}
		return hTTPMessage;
	}
	
	@PostMapping("/removeconferenceroombooking")
	public HTTPMessage removeConferenceRoomBooking(@RequestBody ConferenceRoom conferenceRoom) {
		
		// Get the conference room meeting and delete it
		List<ConferenceRoom> selectedConferences = conferenceRoomRepository.findByStartAndEndAndEmail(conferenceRoom.getStart(), conferenceRoom.getEnd(), conferenceRoom.getEmail());
		HTTPMessage hTTPMessage = new HTTPMessage();
		if (selectedConferences != null) {
			// If we got more than two meetings that have the SAME start, end and email
			for(ConferenceRoom selectedConference : selectedConferences) {
				// Set in the members from database
				conferenceRoom.setMembers(selectedConference.getMembers());
				
				// Delete now
				conferenceRoomRepository.delete(selectedConference);
				hTTPMessage.setMessageStatusCode(HttpStatus.OK.value());
				hTTPMessage.setMessage("Meeting deleted");
			}
			
			// Send EMAIL to members
			try {
				sendEmail(conferenceRoom, conferenceRoom.getStart(), conferenceRoom.getEnd(), "Meeting canceled in the conference room: ", "Meeting canceled - Conference Room");
			} catch (AddressException | AuthenticationFailedException e) {
				hTTPMessage.setMessage(e.getMessage());
			}
			logger.info("Sending message to: " + conferenceRoom.getMembers());
			
		} else {
			hTTPMessage.setMessageStatusCode(HttpStatus.NOT_FOUND.value()); // No data
			hTTPMessage.setMessage("Meeting not exist");
		}
		return hTTPMessage;
		
	}
	
	@PostMapping("/getselectedmembers")
	public HTTPMessage getSelectedMembers(@RequestBody ConferenceRoom conferenceRoom) {
		List<ConferenceRoom> selectedConferences = conferenceRoomRepository.findByStartAndEndAndEmail(conferenceRoom.getStart(), conferenceRoom.getEnd(), conferenceRoom.getEmail());
		HTTPMessage hTTPMessage = new HTTPMessage();
		if (selectedConferences != null) {
			// If we got more than two meetings that have the SAME start, end and email
			for(ConferenceRoom selectedConference : selectedConferences) {
				hTTPMessage.setMessageStatusCode(HttpStatus.OK.value());
				hTTPMessage.setMessage(selectedConference.getMembers()); // They are seperated with ";"
			}
		} else {
			hTTPMessage.setMessageStatusCode(HttpStatus.NOT_FOUND.value()); // No data
			hTTPMessage.setMessage("Members not exist");
		}
		return hTTPMessage;	
	}

	@PostMapping("/setconferenceroombooking")
	public HTTPMessage setConferenceRoomBooking(@RequestBody ConferenceRoom conferenceRoom) {
		
		// Get the meeting time
		long s = conferenceRoom.getStart();  
		long t = conferenceRoom.getEnd();
		
		HTTPMessage hTTPmessageResponse = new HTTPMessage();
		
		// Check if s > t
		if(s > t) {
			hTTPmessageResponse.setMessage("Start cannot be before end");
			logger.info("Start cannot be before end");
			hTTPmessageResponse.setMessageStatusCode(HttpStatus.BAD_REQUEST.value());
			
			// Send back our message
			return hTTPmessageResponse;
		}
		
		// Empty database
		if(conferenceRoomRepository.count() == 0) {
			hTTPmessageResponse.setMessage("Added new meeting");
			logger.info("Added new meeting");
			hTTPmessageResponse.setMessageStatusCode(HttpStatus.OK.value());
			conferenceRoomRepository.save(conferenceRoom); // Email will be saved here
			
			try {
				sendEmail(conferenceRoom, conferenceRoom.getStart(), conferenceRoom.getEnd(), "Meeting booked in the conference room: ", "Meeting booked - Conference Room");
			} catch (AddressException | AuthenticationFailedException e) {
				hTTPmessageResponse.setMessage(e.getMessage());
			}
			logger.info("Sending message to: " + conferenceRoom.getMembers());
			
			// Send back our message
			return hTTPmessageResponse;
		}
		
		// Loop all booked meetings
		for (ConferenceRoom conference : conferenceRoomRepository.findAll()) {
			// Get the book stamp from database
			long s0 = conference.getStart(); // Start
			long t0 = conference.getEnd(); // Stop

			// Check if we can place our meeting
			if (s < s0 && s < t0 && t > s0 && t > t0) {
				// Overlapped completely
				hTTPmessageResponse.setMessage("Overlapped completely");
				logger.info("Overlapped completely");
				hTTPmessageResponse.setMessageStatusCode(HttpStatus.BAD_REQUEST.value());
			} else if (s > s0 && s < t0 && t > s0 && t < t0) {
				// Underlapped competly
				hTTPmessageResponse.setMessage("Underlapped competly");
				logger.info("Underlapped competly");
				hTTPmessageResponse.setMessageStatusCode(HttpStatus.BAD_REQUEST.value());
			} else if (s >= s0 && s < t0 && t > s0 && t > t0) {
				// Overlapped at the end
				hTTPmessageResponse.setMessage("Overlapped at the end");
				logger.info("Overlapped at the end");
				hTTPmessageResponse.setMessageStatusCode(HttpStatus.BAD_REQUEST.value());
			} else if (s < s0 && s < t0 && t > s0 && t <= t0) {
				// Overlapped at the beginning
				hTTPmessageResponse.setMessage("Overlapped at the beginning");
				logger.info("Overlapped at the beginning");
				hTTPmessageResponse.setMessageStatusCode(HttpStatus.BAD_REQUEST.value());
			} else if (s >= s0 && t <= t0) {
				// Exactly at the same meeting
				hTTPmessageResponse.setMessage("Overlapped meeting");
				logger.info("Overlapped meeting");
				hTTPmessageResponse.setMessageStatusCode(HttpStatus.BAD_REQUEST.value());
			} else {
				// Here we can place our new meeting
				hTTPmessageResponse.setMessage("Added new meeting");
				logger.info("Added new meeting");
				hTTPmessageResponse.setMessageStatusCode(HttpStatus.OK.value());
				conferenceRoomRepository.save(conferenceRoom); // Email will be saved here
				
				// Send EMAIL to members
				try {
					sendEmail(conferenceRoom, s0, t0, "Meeting booked in the conference room: ", "Meeting booked - Conference Room");
				} catch (AddressException | AuthenticationFailedException e) {
					hTTPmessageResponse.setMessage(e.getMessage());
				}
				logger.info("Sending message to: " + conferenceRoom.getMembers());
			}
		}
		
		// Send back our message
		return hTTPmessageResponse;
		
	}

	/**
	 * This will display the message for the members 
	 * @param conferenceRoom
	 * @param stop 
	 * @param start 
	 * @param messageForMembers
	 * @param subject 
	 */
	private void sendEmail(ConferenceRoom conferenceRoom, long start, long stop, String messageForMembers, String subject) throws AddressException, AuthenticationFailedException {
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		System.out.println("Start = " + start);
		System.out.println("Stop = " + stop);
		String dateStart = simpleDateFormat.format(new Date(start));
		String dateStop =  simpleDateFormat.format(new Date(stop));
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage(); 
		for(String member : conferenceRoom.getMembers().split(";")) {
			System.out.println(member);
			boolean valid = EmailValidator.getInstance().isValid(member);
			if(valid == true) {
				simpleMailMessage.setTo(member); // This is the email address 
				simpleMailMessage.setSubject(subject); 
		        String message = messageForMembers + conferenceRoom.getMembers() + "\n\n.Time: " + dateStart  + " - " + dateStop;
		        simpleMailMessage.setText(message);
		        emailSender.send(simpleMailMessage);
			}
		}
        
	}



	/**
	 * Search for users
	 * 
	 * @return JSON String object
	 */
	@GetMapping("/searchcompanyusers")
	public HTTPMessage searchCompanyUsers() {
		// Send the Online entity in form of a JSON file
		HTTPMessage hTTPMessage = new HTTPMessage();
		if (companyRepository.count() > 0) {
			hTTPMessage.setMessageStatusCode(HttpStatus.OK.value());
			hTTPMessage.setJsonMessage(companyRepository.findAll());
		} else {
			hTTPMessage.setMessageStatusCode(HttpStatus.NOT_FOUND.value()); // No data
			hTTPMessage.setJsonMessage(companyRepository.findAll());
		}
		return hTTPMessage;
	}

	/**
	 * Check if user exist
	 * 
	 * @param username
	 * @return
	 */
	@GetMapping("/exist")
	public HTTPMessage exist(@RequestParam("username") String username) {
		User user = userRepository.findByUsername(username);
		HTTPMessage hTTPMessage = new HTTPMessage();
		if (user != null) {
			hTTPMessage.setMessageStatusCode(HttpStatus.OK.value());
			hTTPMessage.setMessage("User: " + username + " exist");
		} else {
			hTTPMessage.setMessageStatusCode(HttpStatus.NOT_FOUND.value()); // No data
			hTTPMessage.setMessage("User: " + username + " does not exist");
		}
		return hTTPMessage;
	}

	/**
	 * If this can be called, then you have logged in
	 * 
	 * @return
	 */
	@PostMapping("/login")
	public HTTPMessage login() {
		HTTPMessage hTTPMessage = new HTTPMessage();
		hTTPMessage.setMessageStatusCode(HttpStatus.OK.value());
		hTTPMessage.setMessage("Logged in");
		return hTTPMessage;
	}

}

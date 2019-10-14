package se.danielmartensson.BooKServer.component;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import se.danielmartensson.BooKServer.entity.ConferenceRoom;
import se.danielmartensson.BooKServer.repository.ConferenceRoomRepository;

@Component
@PropertySource("classpath:schedule.properties")
public class Schedule {
	
	@Value("${schedule.deleteMeetingAfterMilliseconds}")
    private long deleteMeetingAfterMilliseconds;
	
	@Autowired
	private ConferenceRoomRepository conferenceRoomRepository;

	/**
	 * This function will scan ALL the booked meetings and check if the column "end" is older than a week = Remove row
	 */
	@Scheduled(cron = "0 0 12 * * FRI") // this method will be executed as 12:00:00 AM of every friday
    public void removeOldBookedMeeting() {
		List<ConferenceRoom> conferencerooms = conferenceRoomRepository.findAll();
		Date currentDate = new Date();
		long currentTime = currentDate.getTime();
		for(ConferenceRoom conferenceRoom : conferencerooms) {
			if((currentTime - conferenceRoom.getEnd()) >= deleteMeetingAfterMilliseconds) {
				conferenceRoomRepository.deleteById(conferenceRoom.getId()); // Delete
			}
		}
    }
}

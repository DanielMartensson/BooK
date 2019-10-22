package se.danielmartensson.BooKServer.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.danielmartensson.BooKServer.entity.ConferenceRoom;

@Repository
public interface ConferenceRoomRepository extends JpaRepository<ConferenceRoom, Long>{
	
	List<ConferenceRoom> findByEmail(String email);
	List<ConferenceRoom> findByStartAndEndAndEmail(long start, long end, String email);
}

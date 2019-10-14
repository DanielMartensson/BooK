package se.danielmartensson.BooKServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.danielmartensson.BooKServer.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	void deleteById(long id);
	User findByUsername(String username);

}
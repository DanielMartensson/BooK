package se.danielmartensson.BooKServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.danielmartensson.BooKServer.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	void deleteById(long id);
}
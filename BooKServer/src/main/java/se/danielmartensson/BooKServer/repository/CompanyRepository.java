package se.danielmartensson.BooKServer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.danielmartensson.BooKServer.entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

	Company findByUsername(String username);
}
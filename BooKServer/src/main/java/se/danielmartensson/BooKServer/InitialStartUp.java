package se.danielmartensson.BooKServer;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import se.danielmartensson.BooKServer.entity.Company;
import se.danielmartensson.BooKServer.entity.Role;
import se.danielmartensson.BooKServer.entity.User;
import se.danielmartensson.BooKServer.repository.CompanyRepository;
import se.danielmartensson.BooKServer.repository.UserRepository;

@Component
public class InitialStartUp implements ApplicationListener<ContextRefreshedEvent> {

	private static final String ADMIN = "admin";
	
	private static final String EMAIL = ADMIN + "@" + ADMIN + ".com";

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private UserRepository userRepository;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// Set the default standard admin and password as admin, if not exist
		User user = userRepository.findByUsername(EMAIL);
		if (user == null) {
			user = new User();
			user.setUser_id(1); // Must be 1 to start with
			user.setUsername(EMAIL);
			user.setPassword(passwordEncoder().encode(ADMIN));
			user.setEmail(EMAIL);
			Role role = new Role();
			role.setRole(ADMIN.toUpperCase());
			role.setRole_id(1); // Must be 1 to start with
			Set<Role> roles = new HashSet<Role>();
			roles.add(role);
			user.setRoles(roles);
			userRepository.save(user);
			
			Company company = new Company();
			company.setId(user.getUser_id());
			company.setCompany(ADMIN);
			company.setUsername(ADMIN);
			company.setEmail(EMAIL);
			companyRepository.save(company);
			logger.info("No user named '" + EMAIL + "' in entity 'user'. Creating user named '" + EMAIL + "' with password '" + ADMIN + "' with role '" + ADMIN.toUpperCase() + "'. Please change the password later.");
		}
		
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
}
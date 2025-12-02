package edu.final_project.hot_properties.initializers;

import edu.final_project.hot_properties.entities.Role;
import edu.final_project.hot_properties.entities.RoleEnum;
import edu.final_project.hot_properties.entities.User;
import edu.final_project.hot_properties.repositories.RoleRepository;
import edu.final_project.hot_properties.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UsersDataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsersDataInitializer(UserRepository userRepository, RoleRepository roleRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        if (userRepository.count() == 0 && roleRepository.count() == 0) {
            Role roleBuyer = new Role(RoleEnum.BUYER.name());
            Role roleAgent = new Role(RoleEnum.AGENT.name());
            Role roleAdmin = new Role(RoleEnum.ADMIN.name());

//            Role roleBuyer = new Role("ROLE_BUYER");
//            Role roleAgent = new Role("ROLE_AGENT");
//            Role roleAdmin = new Role("ROLE_ADMIN");

            roleRepository.save(roleBuyer);
            roleRepository.save(roleAgent);
            roleRepository.save(roleAdmin);

            User buyer1 = new User("john",
                    "buyer",
                    "buyer1@mail.com",
                    passwordEncoder.encode("12345"),
                    roleBuyer);

            User buyer2 = new User("bob",
                    "buyer",
                    "buyer2@mail.com",
                    passwordEncoder.encode("12345"),
                    roleBuyer);

            User agent1 = new User("john",
                    "agent",
                    "agent1@mail.com",
                    passwordEncoder.encode("12345"),
                    roleAgent);

            User agent2 = new User("bob",
                    "agent",
                    "agent2@mail.com",
                    passwordEncoder.encode("12345"),
                    roleAgent);

            User admin1 = new User("john",
                    "admin",
                    "admin1@mail.com",
                    passwordEncoder.encode("12345"),
                    roleAdmin);

            User admin2 = new User("bob",
                    "admin",
                    "admin2@mail.com",
                    passwordEncoder.encode("12345"),
                    roleAdmin);

            userRepository.save(buyer1);
            userRepository.save(buyer2);
            userRepository.save(agent1);
            userRepository.save(agent2);
            userRepository.save(admin1);
            userRepository.save(admin2);

            System.out.println("Initial users and roles inserted.");
        } else {

            System.out.println("Users and roles already exist, skipping initialization.");
        }
    }
}

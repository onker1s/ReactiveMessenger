package server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
//import org.springframework.security.crypto.password.PasswordEncoder;
import server.data.UserRepository;

@SpringBootApplication
public class ServerApplication {
    @Bean
    public CommandLineRunner dataLoader(UserRepository userRepo) {
        return args -> {
            // Создание тестового пользователя
            String testUsername = "testUser";
            String testPassword = "testPassword"; // Лучше использовать безопасный пароль
            //String encodedPassword = encoder.encode(testPassword);

            User testUser = new User(testUsername, testPassword);

            // Сохранение пользователя в базе данных
            userRepo.save(testUser).doOnSuccess(user -> {
                System.out.println("Test user created: " + user.getUsername());
            }).subscribe();
        };
    }
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
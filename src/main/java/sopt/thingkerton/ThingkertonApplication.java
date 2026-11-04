package sopt.thingkerton;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class ThingkertonApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThingkertonApplication.class, args);
    }

}

package room_resevation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ResevationApplication {

	public static void main(String[] args) {
		System.out.println(">>> MONGODB_URI = " + System.getenv("MONGODB_URI"));
		SpringApplication.run(ResevationApplication.class, args);
	}

}

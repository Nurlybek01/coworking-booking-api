package kz.nurlybek.coworking_booking_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CoworkingBookingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoworkingBookingApiApplication.class, args);
	}

}

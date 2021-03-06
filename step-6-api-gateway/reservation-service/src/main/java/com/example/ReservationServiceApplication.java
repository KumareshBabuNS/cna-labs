package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.stream.Stream;

@EnableDiscoveryClient
@EnableBinding(Sink.class)
@SpringBootApplication
public class ReservationServiceApplication implements CommandLineRunner{

	@Bean
	public Sampler defaultSampler() {
		return new AlwaysSampler();
	}

	@Autowired
	ReservationResource reservationResource;

	public void run(String... args){
		Stream.of("peter","john","leo")
				.forEach(name -> reservationResource.save(new Reservation(name)));
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}

@Entity
class Reservation{
	@Id
	@GeneratedValue
	private Long id;
	private String name;

	public Reservation(String name) {
		this.name = name;
	}

	public Reservation() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

@RestResource
interface ReservationResource extends JpaRepository<Reservation, Long>{

}

@Component
@RefreshScope
class Greeting{
	@Value("${greeting.message}")
	String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}

@RestController
class GreetingController{
	@Autowired
	private Greeting greeting;

	@RequestMapping("/greeting")
	public String greeting(){
		return greeting.getMessage();
	}
}


@MessageEndpoint
class ReservationServiceActivator {

	@Autowired
	private ReservationResource reservationResource;

	@ServiceActivator(inputChannel = Sink.INPUT)
	public String accept(String reservationName) {
		this.reservationResource.save(new Reservation(reservationName));
		return "ok";
	}
}

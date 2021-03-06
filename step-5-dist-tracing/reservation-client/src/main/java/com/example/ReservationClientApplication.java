package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableFeignClients
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableBinding(Source.class)
@SpringBootApplication
public class ReservationClientApplication {

	@Bean
	public Sampler defaultSampler() {
		return new AlwaysSampler();
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
}

class Reservation {
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}

@FeignClient(name = "${service.reservation}", fallback = ReservationServiceFallback.class)
interface ReservationService {
	@RequestMapping("/reservations")
	PagedResources<Reservation> getReservations();
}

@Service
class ReservationServiceFallback implements ReservationService{
	public PagedResources<Reservation> getReservations(){
		return null;
	}
}

@RestController
class ReservationController{

	@Autowired
	ReservationService reservationService;
	@Autowired
	ReservationServiceFallback reservationServiceFallback;

	@Autowired
	@Output(Source.OUTPUT)
	private MessageChannel out;

	@RequestMapping("/names")
	public List<String> getNames(){
		return reservationService
				.getReservations()
				.getContent()
				.stream()
				.map(r -> r.getName())
				.collect(Collectors.toList());
	}

	@RequestMapping(value = "/reserve/{name}")
	public void reserve(@PathVariable String name){
		out.send(MessageBuilder.withPayload(name).build());
	}
}
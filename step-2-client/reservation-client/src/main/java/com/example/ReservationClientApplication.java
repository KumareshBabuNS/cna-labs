package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@EnableFeignClients
@SpringBootApplication
public class ReservationClientApplication {

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

@FeignClient(name = "reservation-service", url = "http://localhost:8080")
interface ReservationService {
	@RequestMapping("/reservations")
	PagedResources<Reservation> getReservations();
}

@RestController
class ReservationController{


	ReservationService reservationService;

	@Autowired
	public void setReservationService(ReservationService reservationService){
		this.reservationService=reservationService;
	}

	@RequestMapping("/names")
	private List<String> getNames(){
		return reservationService.getReservations().getContent().stream().map(r -> r.getName()).collect(Collectors.toList());
	}
}
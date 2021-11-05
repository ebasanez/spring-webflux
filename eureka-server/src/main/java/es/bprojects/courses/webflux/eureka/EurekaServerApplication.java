package es.bprojects.courses.webflux.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author ebasanez
 * @since 2021-11-05
 */
@EnableEurekaServer
@org.springframework.boot.autoconfigure.SpringBootApplication
public class EurekaServerApplication {

	public static void main(String [] args){
		SpringApplication.run(EurekaServerApplication.class, args);
	}

}

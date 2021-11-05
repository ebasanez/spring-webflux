package es.bprojects.courses.webflux;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.blockhound.BlockHound;

/**
 * @author ebasanez
 * @since 2021-05-26
 */
@Slf4j
@EnableEurekaClient
@SpringBootApplication
@RequiredArgsConstructor
public class ClientApplication implements CommandLineRunner {

	public static void main(String[] args) {
		// Blockhound throws exception with thymeleaf, as template resolve is blocking (access file), so additional configuration is needed:
		BlockHound.builder()
				.allowBlockingCallsInside("org.thymeleaf.util.ClassLoaderUtils", "loadResourceAsStream")
				.install();
		SpringApplication.run(ClientApplication.class, args);
	}

	@Override
	public void run(String... args) {
	}

}

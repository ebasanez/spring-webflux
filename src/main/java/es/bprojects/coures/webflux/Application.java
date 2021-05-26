package es.bprojects.coures.webflux;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import es.bprojects.coures.webflux.domain.User;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-05-26
 */
@Slf4j
@SpringBootApplication
public class Application implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		// Example of flux that throws error when processing one of their elements.
		List<String> names = List.of("Andrés Guzman", "Pedro Almodobar", "Alfonso Lopez", "Juan Salva", "pedro Sanchez");
		Flux.fromIterable(names)
				.map(name -> new User(name.split(" ")[0], name.split(" ")[1]))
				// Transform flux to list
				.collectList()
				.subscribe(e -> log.info("Items: " + e));
	}
}

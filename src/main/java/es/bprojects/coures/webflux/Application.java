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
		Flux<User> flux = Flux.fromIterable(names)
				.map(name -> new User(name.split(" ")[0], name.split(" ")[1]))
				.filter(u -> "Pedro".equalsIgnoreCase(u.getName()))
				// Otra forma de filtar, con flatMap:
				/*
				.flatMap(u -> {
					if ("Pedro".equalsIgnoreCase(u.getName())) {
						return Mono.just(u);
					} else {
						return Mono.empty();
					}
				})
				*/
				.doOnNext(e -> {
					if (e.getName().isEmpty()) {
						throw new RuntimeException("Element is empty");
					} else {
						System.out.println(e);
					}
				});

		flux.subscribe(e -> log.info("User: " + e),
				// Handles error and interrupts flux processing:
				error -> log.error("Error: empty name"),
				// Executes wjhen flux is closed. Wont be invoked if there has been any error.
				() -> log.info("Flux processing finished")
		);
	}
}

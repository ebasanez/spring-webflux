package es.bprojects.coures.webflux;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

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
		Flux<String> nombres = Flux.just("Andrés", "Pedro", "", "Juan")
				.map(String::toUpperCase)
				.doOnNext(e -> {
					if (e.isEmpty()) {
						throw new RuntimeException("Element is empty");
					} else {
						System.out.println(e);
					}
				});

		// Handles error and interrupts flux processing:
		nombres.subscribe(e-> log.info(e),
				error -> log.error("Error: empty name"));

		// No se llama si ha habido algún error.
		nombres.doOnComplete(new Runnable() {
			@Override
			public void run() {
				log.info("Flux processing finished");
			}
		});
	}
}

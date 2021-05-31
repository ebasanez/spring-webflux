package es.bprojects.coures.webflux;

import java.time.Duration;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import es.bprojects.coures.webflux.domain.Comments;
import es.bprojects.coures.webflux.domain.User;
import es.bprojects.coures.webflux.domain.UserComments;
import es.bprojects.coures.webflux.model.Product;
import es.bprojects.coures.webflux.persistence.ProductsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-05-26
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class Application implements CommandLineRunner {

	private final ProductsRepository productsRepository;
	private final ReactiveMongoTemplate mongoTemplate;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Start main");
		deleteAllProducts();
		insertProducts();
		log.info("End main");
	}

	private void insertProducts() {
		final Flux<Product> products = Flux.just(
				new Product("Product1", 100.0f),
				new Product("Product2", 200.0f),
				new Product("Product3", 300.0f),
				new Product("Product4", 400.0f),
				new Product("Product5", 500.0f),
				new Product("Product6", 600.0f),
				new Product("Product7", 700.0f),
				new Product("Product8", 800.0f),
				new Product("Product9", 900.0f)
		);
		products.flatMap(p -> {
			p.setCreatedAt(new Date());
			return productsRepository.save(p);
		})
				.subscribe(p -> log.info(p.toString()));
	}

	private void deleteAllProducts() {
		mongoTemplate.dropCollection(Product.class).subscribe(e -> log.info("All products deleted"));
	}

}

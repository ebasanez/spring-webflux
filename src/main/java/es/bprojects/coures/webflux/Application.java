package es.bprojects.coures.webflux;

import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import es.bprojects.coures.webflux.infrastructure.persistence.CategoryRepository;
import es.bprojects.coures.webflux.infrastructure.persistence.ProductsRepository;
import es.bprojects.coures.webflux.infrastructure.persistence.model.Category;
import es.bprojects.coures.webflux.infrastructure.persistence.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;

/**
 * @author ebasanez
 * @since 2021-05-26
 */
@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class Application implements CommandLineRunner {

	private final ProductsRepository productsRepository;
	private final CategoryRepository categoryRepository;
	private final ReactiveMongoTemplate mongoTemplate;

	public static void main(String[] args) {
		// Blockhound throws exception with non rest endpoints, as template resolve is blocking (access file)
		// BlockHound.install();
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("Start main");
		deleteAll();
		insertProducts();
		log.info("End main");
	}

	private void insertProducts() {

		Category electronics = new Category("electronics");
		Category sports = new Category("sports");
		Category computer = new Category("computer");
		Category furniture = new Category("furniture");

		final Flux<Category> categories = Flux.just(
				electronics, sports, computer, furniture);

		categories.flatMap(p -> categoryRepository.save(p))
				.doOnNext(c -> log.info(c.toString()))
				// After persisting categories, persist products
				.thenMany(
						Flux.just(
								new Product("TV Panasonic", 100.0f, electronics),
								new Product("Sony Camera", 200.0f, electronics),
								new Product("Apple iPod", 300.0f, electronics),
								new Product("Sony Notebook", 400.0f, computer),
								new Product("HP Multifunctional", 500.0f, computer),
								new Product("Mica bedtable", 600.0f, furniture),
								new Product("Bike", 700.0f, sports),
								new Product("balloon", 800.0f, sports),
								new Product("Golf stick", 900.0f, sports)
						)
				)
				.flatMap(p -> {
			p.setCreatedAt(new Date());
			return productsRepository.save(p);
		})
				.subscribe(p -> log.info(p.toString()));

	}

	private void deleteAll() {
		mongoTemplate.dropCollection(Product.class).subscribe(e -> log.info("All products deleted"));
		mongoTemplate.dropCollection(Category.class).subscribe(e -> log.info("All categories deleted"));
	}


}

package es.bprojects.courses.webflux.infrastructure.persistence;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import es.bprojects.courses.webflux.infrastructure.persistence.model.Product;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-05-30
 */
public interface ProductsRepository extends ReactiveMongoRepository<Product, String> {

	Mono<Product> findByName(String name);

	// To retrieve using @Query
	@Query("{ 'name': ?0 }")
	Mono<Product> obtenerPorNombre(String name);

}

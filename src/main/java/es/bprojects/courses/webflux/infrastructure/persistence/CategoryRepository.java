package es.bprojects.courses.webflux.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import es.bprojects.courses.webflux.infrastructure.persistence.model.Category;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-06-07
 */
public interface CategoryRepository extends ReactiveMongoRepository<Category, String> {

	Mono<Category> findByName(String name);

}

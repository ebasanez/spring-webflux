package es.bprojects.coures.webflux.infrastructure.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import es.bprojects.coures.webflux.infrastructure.persistence.model.Category;

/**
 * @author ebasanez
 * @since 2021-06-07
 */
public interface CategoryRepository extends ReactiveMongoRepository<Category,String> {

}

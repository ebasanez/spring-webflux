package es.bprojects.coures.webflux.persistence;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import es.bprojects.coures.webflux.model.Product;

/**
 * @author ebasanez
 * @since 2021-05-30
 */
public interface ProductsRepository extends ReactiveMongoRepository<Product, String> {
}

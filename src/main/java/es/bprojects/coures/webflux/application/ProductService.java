package es.bprojects.coures.webflux.application;

import es.bprojects.coures.webflux.domain.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-06-02
 */
public interface ProductService {

	Flux<Product> findAll();

	Mono<Product> findById(String id);

	Mono<Product> insert(Product product);

	Mono<Product> update(Product product);

	Mono<Void> delete(String id);

}

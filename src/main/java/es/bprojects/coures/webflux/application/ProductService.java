package es.bprojects.coures.webflux.application;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.codec.multipart.FilePart;

import es.bprojects.coures.webflux.domain.Category;
import es.bprojects.coures.webflux.domain.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-06-02
 */
public interface ProductService {

	/*
	Product methods
	 */
	Flux<Product> findAll();

	Mono<Product> findById(String id);

	Mono<Void> insert(
			@NotNull Product product,
			FilePart file);

	Mono<Void> update(Product product,
			FilePart file);

	Mono<Void> deleteProduct(String id);
	/*
	Category methods
	 */

	Flux<Category> findAllCategories();

	Mono<Category> findCategoryById(@NotNull String id);

	Mono<Category> insert(@NotNull @Valid Category product);

}

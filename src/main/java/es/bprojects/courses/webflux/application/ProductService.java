package es.bprojects.courses.webflux.application;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.codec.multipart.FilePart;

import es.bprojects.courses.webflux.domain.Category;
import es.bprojects.courses.webflux.domain.Product;
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

	Mono<Product> findById(
			@NotNull String id);

	Mono<Product> findByName(
			@NotNull String name);

	Mono<Product> insert(
			@NotNull Product product,
			FilePart file);

	Mono<Product> update(
			@NotNull Product product,
			FilePart file);

	Mono<Void> deleteProduct(
			@NotNull String id);
	/*
	Category methods
	 */

	Flux<Category> findAllCategories();

	Mono<Category> findCategoryById(@NotNull String id);

	Mono<Category> findCategoryByName(@NotNull String name);

	Mono<Category> insert(@NotNull @Valid Category product);

}

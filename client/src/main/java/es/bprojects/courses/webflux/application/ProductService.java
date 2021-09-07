package es.bprojects.courses.webflux.application;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.codec.multipart.FilePart;

import es.bprojects.courses.webflux.domain.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-08-08
 */
public interface ProductService {

	Flux<Product> findAll();

	Mono<Product> findById(
			@NotNull String id);

	Mono<Product> insert(
			@NotNull @Valid Product product);

	Mono<Product> update(
			@NotNull @Valid Product product);

	Mono<Void> delete(
			@NotNull String id);

	Mono<Product> upload(
			@NotNull String id,
			@NotNull FilePart file);

}

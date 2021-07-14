package es.bprojects.coures.webflux.infrastructure.handler;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import es.bprojects.coures.webflux.application.ProductService;
import es.bprojects.coures.webflux.domain.Product;
import es.bprojects.coures.webflux.infrastructure.controller.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-07-13
 */
@Component
@RequiredArgsConstructor
public class ProductHandler {


	private final ProductService productService;

	public Mono<ServerResponse> list(ServerRequest request) {
		return ServerResponse.ok().body(productService.findAll(), ProductDto.class);
	}

	public Mono<ServerResponse> get(ServerRequest request) {
		final String id = request.pathVariable("id");
		return productService.findById(id)
				.flatMap(p -> ServerResponse.ok().bodyValue(p))
				.switchIfEmpty(ServerResponse.notFound().build());

	}

	public Mono<ServerResponse> create(ServerRequest request) {
		return request.bodyToMono(ProductDto.class)
				.map(this::toDomain)
				.flatMap(p -> productService.insert(p, null))
				.flatMap(p -> ServerResponse
						.created(URI.create("/api/v2/products/" + p.getId()))
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(p));
	}

	public Mono<ServerResponse> update(ServerRequest request) {
		final String id = request.pathVariable("id");
		return request.bodyToMono(ProductDto.class).map(p ->
				Product.builder()
						.id(id)
						.name(p.getName())
						.price(p.getPrice())
						.category(p.getCategory())
						.build()
		).flatMap(p ->
				productService.update(p, null))
				.flatMap(p ->
						ServerResponse
								.ok()
								.contentType(MediaType.APPLICATION_JSON)
								.bodyValue(p));


	}

	private Product toDomain(ProductDto dto) {
		return Product.builder()
				.name(dto.getName())
				.price(dto.getPrice())
				.category(dto.getCategory())
				.photo(dto.getPhoto())
				.build();
	}

	public Mono<ServerResponse> delete(ServerRequest serverRequest) {
		final String id = serverRequest.pathVariable("id");
		return productService.findById(id).map(Product::getId)
				.flatMap(p -> productService.deleteProduct(p)
						.then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> upload(ServerRequest serverRequest) {
		final String id = serverRequest.pathVariable("id");
		return serverRequest.multipartData()
				.map(m ->
						m.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file ->
						productService.findById(id).flatMap(p ->
								productService.update(p, file)))
				.flatMap(p -> ServerResponse.ok().bodyValue(p))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

}

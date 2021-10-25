package es.bprojects.courses.webflux.infrastructure.handler;

import java.net.URI;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import es.bprojects.courses.webflux.application.ProductService;
import es.bprojects.courses.webflux.domain.Product;
import es.bprojects.courses.webflux.infrastructure.controller.dto.ProductDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-07-13
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductHandler {

	private final ProductService productService;
	private final Validator validator;

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


		return request
				.bodyToMono(ProductDto.class)

				.flatMap(p -> {
					log.info(p.toString());
					Errors errors = new BeanPropertyBindingResult(p, ProductDto.class.getName());
					validator.validate(p, errors);

					if (errors.hasErrors()) {
						return Flux.fromIterable(errors.getFieldErrors())
								.map(err -> "Field " + err.getField())
								.collectList()
								.flatMap(errList -> ServerResponse.badRequest().bodyValue(errList));
					} else {
						return productService.insert(toDomain(p), null)
								.flatMap(pCreated -> ServerResponse
										.created(URI.create("/api/v2/products/" + pCreated.getId()))
										.contentType(MediaType.APPLICATION_JSON)
										.bodyValue(pCreated))
								.onErrorResume(e -> ServerResponse.badRequest().bodyValue(e.toString()));
					}

				});

	}

	public Mono<ServerResponse> update(ServerRequest request) {
		final String id = request.pathVariable("id");
		return request
				.bodyToMono(ProductDto.class)
				.map(p ->
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
								.bodyValue(p)
				)
				.switchIfEmpty(ServerResponse.notFound().build())
				.onErrorResume(e -> ServerResponse.badRequest().bodyValue(e.toString()));

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
		return productService.findById(id)
				.map(Product::getId)
				.flatMap(p -> productService.deleteProduct(p))
				.then(ServerResponse.noContent().build())
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
				.flatMap(p -> ServerResponse
						.created(URI.create("/api/v2/products/".concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(p))
				.switchIfEmpty(ServerResponse.notFound().build());
	}


	public Mono<ServerResponse> createWithPhoto(ServerRequest request) {

		Mono<Product> product = request
				.multipartData()
				.map(multipart -> {
							Map<String, Part> map = multipart.toSingleValueMap();
							Product p = Product.builder()
									.name(((FormFieldPart) map.get("name")).value())
									.price(Float.parseFloat(((FormFieldPart) map.get("price")).value()))
									.category(((FormFieldPart) map.get("category")).value())
									.build();
							return p;
						}
				);

		return request
				.multipartData()
				.map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> product.flatMap(p -> productService.insert(p, file)))
				.flatMap(p -> ServerResponse
						.created(URI.create("/api/v2/products/" + p.getId()))
						.contentType(MediaType.APPLICATION_JSON)
						.bodyValue(p));
	}


}

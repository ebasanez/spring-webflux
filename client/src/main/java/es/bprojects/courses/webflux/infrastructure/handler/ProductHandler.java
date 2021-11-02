package es.bprojects.courses.webflux.infrastructure.handler;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import es.bprojects.courses.webflux.application.ProductService;
import es.bprojects.courses.webflux.domain.Product;
import es.bprojects.courses.webflux.infrastructure.handler.dto.ProductDto;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-07-13
 */
@Slf4j
@Component
public class ProductHandler {


	private final String baseUrl;
	private final ProductService productService;
	private final Validator validator;

	public ProductHandler(@Value("${product.handler.baseUrl}") String baseUrl, ProductService productService, Validator validator) {
		this.baseUrl = baseUrl;
		this.productService = productService;
		this.validator = validator;
	}

	public Mono<ServerResponse> list() {
		return ServerResponse.ok().body(productService.findAll(), Product.class);
	}

	public Mono<ServerResponse> get(ServerRequest request) {
		final String id = request.pathVariable("id");
		return handleError(productService.findById(id)
				.flatMap(p -> ServerResponse.ok().bodyValue(p)));

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
						return handleError(productService.insert(toDomain(p))
								.flatMap(pCreated -> ServerResponse
										.created(URI.create(baseUrl + "/" + pCreated.getId()))
										.contentType(APPLICATION_JSON)
										.bodyValue(pCreated)));
					}

				});

	}

	public Mono<ServerResponse> update(ServerRequest request) {
		final String id = request.pathVariable("id");
		final Mono<Product> product = request
				.bodyToMono(ProductDto.class)
				.map(p ->
						Product.builder()
								.id(id)
								.name(p.getName())
								.price(p.getPrice())
								.category(p.getCategory())
								.build()
				);
		return handleError(product
				.flatMap(productService::update)
				.flatMap(p ->
						ServerResponse
								.created(URI.create("/api/client".concat(id)))
								.contentType(APPLICATION_JSON)
								.bodyValue(p)));
	}

	public Mono<ServerResponse> delete(ServerRequest serverRequest) {
		final String id = serverRequest.pathVariable("id");
		return handleError(productService.findById(id)
				.map(Product::getId)
				.flatMap(productService::delete)
				.then(ServerResponse.noContent().build())
				.switchIfEmpty(ServerResponse.notFound().build()));
	}

	public Mono<ServerResponse> uploadPhoto(ServerRequest serverRequest) {
		final String id = serverRequest.pathVariable("id");
		return handleError(serverRequest.multipartData()
				.map(m ->
						m.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file ->
						productService.upload(id, file))
				.flatMap(p -> ServerResponse.ok()
						.contentType(APPLICATION_JSON)
						.bodyValue(p))
				.switchIfEmpty(ServerResponse.notFound().build()));
	}

	public Mono<ServerResponse> createWithPhoto(ServerRequest request) {
		Mono<Product> product = request
				.multipartData()
				.map(multipart -> {
							Map<String, Part> map = multipart.toSingleValueMap();
							return Product.builder()
									.name(((FormFieldPart) map.get("name")).value())
									.price(Float.parseFloat(((FormFieldPart) map.get("price")).value()))
									.category(((FormFieldPart) map.get("category")).value())
									.build();
						}
				);

		return handleError(request
				.multipartData()
				.map(multipart -> multipart.toSingleValueMap().get("file"))
				.cast(FilePart.class)
				.flatMap(file -> product.flatMap(productService::insert)
						.flatMap(p -> ServerResponse
								.created(URI.create(baseUrl + "/" + p.getId()))
								.contentType(APPLICATION_JSON)
								.bodyValue(p))));
	}

	private Product toDomain(ProductDto dto) {
		return Product.builder()
				.name(dto.getName())
				.price(dto.getPrice())
				.category(dto.getCategory())
				.photo(dto.getPhoto())
				.build();

	}

	private Mono<ServerResponse> handleError(Mono<ServerResponse> response) {
		return response.onErrorResume(e -> {
			WebClientResponseException errorResponse = (WebClientResponseException) e;
			switch (errorResponse.getStatusCode()) {
				case NOT_FOUND:
					Map<String, Object> errorBody = new HashMap();
					errorBody.put("error", "Product not found");
					errorBody.put("timestamp", Instant.now());
					return ServerResponse
							.status(NOT_FOUND)
							.bodyValue(errorBody);
				case BAD_REQUEST:
					return ServerResponse
							.badRequest()
							.contentType(APPLICATION_JSON)
							.bodyValue(e.toString());
				default:
					return Mono.error(errorResponse);
			}
		});
	}
}

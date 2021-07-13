package es.bprojects.coures.webflux.infrastructure.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import es.bprojects.coures.webflux.application.ProductService;
import es.bprojects.coures.webflux.domain.Product;
import es.bprojects.coures.webflux.infrastructure.controller.dto.ProductDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-05-31
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductRestController {

	private final ProductService productService;


	@GetMapping
	// We can also response Mono<Product> without wrapping it in response body
	public Mono<ResponseEntity<Flux<Product>>> list() {
		// Un poco más verbose, por si quisieramos personalizar la respuesta:
		return Mono.just(ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(productService.findAll()));
	}

	@GetMapping("/{id}")
	// We can also response Mono<Product> without wrapping it in response body
	public Mono<ResponseEntity<Product>> get(@PathVariable String id) {
		return productService.findById(id)
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> create(
			@Valid @RequestBody Mono<ProductDto> productDto) {
		final Map<String, Object> response = new HashMap<>();
		return productDto.flatMap(product ->
				productService.insert(toDomain(product), null)
						.map(p -> {
							response.put("product", p);
							return ResponseEntity
									.created(URI.create("/api/products/" + p.getId()))
									.contentType(MediaType.APPLICATION_JSON)
									.body(response);
						})
		).onErrorResume(WebExchangeBindException.class, t -> {
			System.out.print("Error");
			return Mono.just(t)
					.cast(WebExchangeBindException.class)
					.map(WebExchangeBindException::getFieldErrors)
					.flatMapMany(Flux::fromIterable)
					.map(fieldError -> "Error in field " + fieldError.getField() + " " + fieldError.getDefaultMessage())
					.collectList()
					.map(list -> {
								System.out.println(list);
								response.put("errors", list);
								return ResponseEntity.badRequest().body(response);
							}
					);
		});
	}

	@PutMapping("/{id}")
	public Mono<ResponseEntity<Product>> update(
			@PathVariable String id,
			@RequestBody ProductDto productDto) {
		final Product product = Product.builder()
				.id(id)
				.name(productDto.getName())
				.price(productDto.getPrice())
				.category(productDto.getCategory())
				.build();
		return productService.update(product, null)
				.doOnNext(p -> log.info("Product {} updated", p.getName()))
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping("/{id}/photo")
	public Mono<ResponseEntity<Product>> uploadPhoto(
			@PathVariable String id,
			@RequestPart FilePart file) {
		return productService
				.findById(id)
				.flatMap(p -> productService.update(p, file))
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> delete(
			@PathVariable String id) {
		// Retrieve product to be able to return 404 if it does not exist for argument id
		return productService.findById(id).map(Product::getId)
				.flatMap(p -> productService.deleteProduct(p)
						.then(Mono.just(ResponseEntity.noContent().build())))
				.defaultIfEmpty(new ResponseEntity(HttpStatus.NOT_FOUND));
	}


	private Product toDomain(ProductDto dto) {
		return Product.builder()
				.name(dto.getName())
				.price(dto.getPrice())
				.category(dto.getCategory())
				.photo(dto.getPhoto())
				.build();
	}

}

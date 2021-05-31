package es.bprojects.coures.webflux.controller;

import java.util.Locale;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.bprojects.coures.webflux.dto.ProductDto;
import es.bprojects.coures.webflux.model.Product;
import es.bprojects.coures.webflux.persistence.ProductsRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ebasanez
 * @since 2021-05-31
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductRestController {

	private final ProductsRepository productsRepository;


	@GetMapping
	public Flux<ProductDto> list() {
		return productsRepository.findAll().map(this::toDto);
	}

	@GetMapping("/{id}")
	public Mono<ProductDto> get(@PathVariable String id) {
		return productsRepository.findById(id).map(this::toDto);
	}


	private final ProductDto toDto(Product entity) {
		return ProductDto.builder()
				.id(entity.getId())
				.name(entity.getName().toUpperCase(Locale.ROOT))
				.price(entity.getPrice())
				.createdAt(entity.getCreatedAt())
				.build();
	}
}

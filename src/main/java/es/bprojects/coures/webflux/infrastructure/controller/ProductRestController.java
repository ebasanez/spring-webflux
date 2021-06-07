package es.bprojects.coures.webflux.infrastructure.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.bprojects.coures.webflux.application.ProductService;
import es.bprojects.coures.webflux.domain.Product;
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

	private final ProductService productService;


	@GetMapping
	public Flux<Product> list() {
		return productService.findAll();
	}

	@GetMapping("/{id}")
	public Mono<Product> get(@PathVariable String id) {
		return productService.findById(id);
	}


}
